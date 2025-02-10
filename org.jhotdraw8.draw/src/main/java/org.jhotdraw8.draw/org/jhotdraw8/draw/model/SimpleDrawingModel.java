/*
 * @(#)SimpleDrawingModel.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.transform.Transform;
import org.jhotdraw8.base.event.Listener;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.draw.css.value.CssPoint2D;
import org.jhotdraw8.draw.figure.ChildLayoutingFigure;
import org.jhotdraw8.draw.figure.Drawing;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.draw.figure.FigurePropertyChangeEvent;
import org.jhotdraw8.draw.figure.Layer;
import org.jhotdraw8.draw.figure.TransformableFigure;
import org.jhotdraw8.draw.render.RenderContext;
import org.jhotdraw8.draw.render.SimpleRenderContext;
import org.jhotdraw8.fxbase.tree.TreeModelEvent;
import org.jhotdraw8.fxcollection.typesafekey.Key;
import org.jhotdraw8.fxcollection.typesafekey.MapAccessor;
import org.jhotdraw8.fxcollection.typesafekey.NonNullMapAccessor;
import org.jhotdraw8.graph.SimpleMutableDirectedGraph;
import org.jhotdraw8.graph.algo.TopologicalSortAlgo;
import org.jspecify.annotations.Nullable;

import java.util.AbstractMap;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * A DrawingModel for drawings which can handle {@link TransformableFigure}s,
 * {@link ChildLayoutingFigure} and layout observing figures,
 * like {@code LineConnectionFigure}.
 *
 */
public class SimpleDrawingModel extends AbstractDrawingModel {

    public SimpleDrawingModel() {
        this.listenOnDrawing = true;
    }

    public SimpleDrawingModel(boolean listenOnDrawing) {
        this.listenOnDrawing = listenOnDrawing;
    }


    private static class MapProxy extends AbstractMap<Key<?>, Object> {

        private @Nullable Map<Key<?>, Object> target = null;
        private @Nullable Figure figure = null;

        @Override
        public Set<Entry<Key<?>, Object>> entrySet() {
            // FIXME should listen on changes of the entry set!
            return target == null ? Collections.emptySet() : target.entrySet();
        }

        public @Nullable Figure getFigure() {
            return figure;
        }

        public void setFigure(@Nullable Figure figure) {
            this.figure = figure;
        }

        public @Nullable Map<Key<?>, Object> getTarget() {
            return target;
        }

        public void setTarget(@Nullable Map<Key<?>, Object> target) {
            this.target = target;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Object put(Key<?> key, Object newValue) {
            if (target != null) {
                final boolean wasAdded = !target.containsKey(key);
                Object oldValue = target.put(key, newValue);
                // if (figure != null) {
                //     onPropertyChanged(figure, (Key<Object>) key, oldValue, newValue, wasAdded, false);
                // }
                return oldValue;
            } else {
                return newValue;
            }
        }

    }

    private final MapProxy mapProxy = new MapProxy();


    private boolean valid = true;
    private final Set<Figure> dirties = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Listener<FigurePropertyChangeEvent> propertyChangeHandler = this::onPropertyChanged;

    private final ObjectProperty<Drawing> root = new SimpleObjectProperty<>(this, ROOT_PROPERTY) {
        @Override
        public void set(@Nullable Drawing newValue) {
            Drawing oldValue = get();
            if (newValue == null && oldValue != null) {
                throw new IllegalArgumentException("null");
            }
            super.set(newValue);
            onRootChanged(oldValue, newValue);
        }
    };
    private final BiFunction<? super DirtyMask, ? super DirtyMask, ? extends DirtyMask> mergeDirtyMask
            = DirtyMask::add;

    private void invalidate() {
        if (valid) {
            valid = false;
            fireDrawingModelInvalidated();
        }
    }

    private final boolean listenOnDrawing;

    private void onRootChanged(@Nullable Drawing oldValue, @Nullable Drawing newValue) {
        if (listenOnDrawing) {
            if (oldValue != null) {
                oldValue.getPropertyChangeListeners().remove(propertyChangeHandler);
            }
            if (newValue != null) {
                newValue.getPropertyChangeListeners().add(propertyChangeHandler);
            }
        }
        fireTreeModelEvent(TreeModelEvent.rootChanged(this, oldValue, newValue));
    }

    private void onPropertyChanged(FigurePropertyChangeEvent event) {
            fireDrawingModelEvent(DrawingModelEvent.propertyValueChanged(this, event.getSource(),
                    event.getKey(), event.getOldValue(),
                    event.getNewValue(), event.wasAdded(), event.wasRemoved()));
            fireTreeModelEvent(TreeModelEvent.nodeChanged(this, event.getSource()));
    }

    private void markDirty(Figure figure) {
        dirties.add(figure);
    }


    private void removeDirty(Figure figure) {
        dirties.remove(figure);
    }

    @Override
    public ObjectProperty<Drawing> drawingProperty() {
        return root;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ObjectProperty<Figure> rootProperty() {
        return (ObjectProperty<Figure>) (ObjectProperty<?>) root;
    }

    @Override
    public void removeFromParent(Figure child) {
        final Figure oldRoot = child.getRoot();
        for (Figure f : child.preorderIterable()) {
            fireTreeModelEvent(TreeModelEvent.nodeRemovedFromTree(this, oldRoot, f));
        }
        Figure parent = child.getParent();
        if (parent != null) {
            int index = parent.getChildren().indexOf(child);
            if (index != -1) {
                parent.getChildren().remove(index);
                fireTreeModelEvent(TreeModelEvent.nodeRemovedFromParent(this, child, parent, index));
                fireTreeModelEvent(TreeModelEvent.nodeChanged(this, parent));
            }
        }
    }

    @Override
    public Figure removeFromParent(Figure parent, int index) {
        Figure child = parent.getChild(index);
        final Figure oldRoot = child.getRoot();
        for (Figure f : child.preorderIterable()) {
            fireTreeModelEvent(TreeModelEvent.nodeRemovedFromTree(this, oldRoot, f));
        }
        parent.getChildren().remove(index);
        fireTreeModelEvent(TreeModelEvent.nodeRemovedFromParent(this, child, parent, index));
        fireTreeModelEvent(TreeModelEvent.nodeChanged(this, parent));
        return child;
    }

    @Override
    public void insertChildAt(Figure child, Figure parent, int index) {
        if (!parent.isSuitableChild(child) || !child.isSuitableParent(parent)) {
            return;
        }
        Figure oldRoot = child.getRoot();
        Figure oldParent = child.getParent();
        if (oldParent != null) {
            int oldChildIndex = oldParent.getChildren().indexOf(child);
            oldParent.removeChild(child);
            fireTreeModelEvent(TreeModelEvent.nodeRemovedFromParent(this, child, oldParent, oldChildIndex));
            fireTreeModelEvent(TreeModelEvent.nodeChanged(this, oldParent));
        }
        parent.getChildren().add(Math.clamp(index, 0, parent.getChildren().size()), child);
        Figure newRoot = child.getRoot();
        if (oldRoot != newRoot) {
            if (oldRoot != null) {
                for (Figure f : child.preorderIterable()) {
                    fireTreeModelEvent(TreeModelEvent.nodeRemovedFromTree(this, oldRoot, f));
                }
            }
            if (newRoot == getRoot()) {
                for (Figure f : child.preorderIterable()) {
                    fireTreeModelEvent(TreeModelEvent.nodeAddedToTree(this, newRoot, f));
                }
            }
        }
        fireTreeModelEvent(TreeModelEvent.nodeAddedToParent(this, child, parent, index));
        fireTreeModelEvent(TreeModelEvent.nodeChanged(this, parent));
    }

    @Override
    public <T> T setNonNull(Figure figure, NonNullMapAccessor<T> key, T newValue) {
        T v = set(figure, key, newValue);
        return Objects.requireNonNull(v, "oldValue");
    }

    @Override
    public <T> T set(Figure figure, MapAccessor<T> key, @Nullable T newValue) {
        if (key instanceof Key<?>) {
            return figure.put(key, newValue);
        } else {
            mapProxy.setFigure(figure);
            mapProxy.setTarget(figure.getProperties());
            T oldValue = key.put(mapProxy, newValue);
            // event will be fired by mapProxy
            mapProxy.setFigure(null);
            mapProxy.setTarget(null);
            return oldValue;
        }

    }

    @Override
    public <T> T remove(Figure figure, MapAccessor<T> key) {
        if (key instanceof Key<?>) {
            boolean wasRemoved = figure.getProperties().containsKey(key);
            T oldValue = figure.remove((Key<T>) key);
            // event will be fired by method handlePropertyChanged if newValue differs from oldValue
            @SuppressWarnings({"unchecked", "RedundantSuppression"})
            Key<Object> keyObject = (Key<Object>) key;
            //onPropertyChanged(figure, keyObject, oldValue, null, false, wasRemoved);
            return oldValue;
        } else {
            mapProxy.setFigure(figure);
            mapProxy.setTarget(figure.getProperties());
            T oldValue = key.remove(mapProxy);
            // event will be fired by mapProxy
            mapProxy.setFigure(null);
            mapProxy.setTarget(null);
            return oldValue;
        }

    }

    @Override
    public <T> T remove(Figure figure, Key<T> key) {
        boolean wasRemoved = figure.getProperties().containsKey(key);
        T oldValue = figure.remove(key);
        // event will be fired by method handlePropertyChanged
        //onPropertyChanged(figure, key, oldValue, key.getDefaultValue(), false, wasRemoved);
        return oldValue;
    }

    @Override
    public <T> Property<T> propertyAt(Figure f, Key<T> key) {
        return new DrawingModelFigureProperty<>(this, f, key);
    }

    @Override
    public void reshapeInLocal(Figure f, Transform transform) {
        f.reshapeInLocal(transform);
        fireDrawingModelEvent(DrawingModelEvent.layoutChanged(this, f));
        fireTreeModelEvent(TreeModelEvent.nodeChanged(this, f));
    }

    @Override
    public void reshapeInParent(Figure f, Transform transform) {
        f.reshapeInParent(transform);
        fireDrawingModelEvent(DrawingModelEvent.layoutChanged(this, f));
        fireTreeModelEvent(TreeModelEvent.nodeChanged(this, f));
    }

    @Override
    public void translateInParent(Figure f, CssPoint2D delta) {
        f.translateInParent(delta);
        fireDrawingModelEvent(DrawingModelEvent.layoutChanged(this, f));
        fireTreeModelEvent(TreeModelEvent.nodeChanged(this, f));
    }

    @Override
    public void transformInParent(Figure f, Transform transform) {
        f.transformInParent(transform);
        fireDrawingModelEvent(DrawingModelEvent.transformChanged(this, f));
        fireTreeModelEvent(TreeModelEvent.nodeChanged(this, f));
    }

    @Override
    public void transformInLocal(Figure f, Transform transform) {
        f.transformInLocal(transform);
        fireDrawingModelEvent(DrawingModelEvent.transformChanged(this, f));
        fireTreeModelEvent(TreeModelEvent.nodeChanged(this, f));
    }

    @Override
    public void reshapeInLocal(Figure f, double x, double y, double width, double height) {
        f.reshapeInLocal(x, y, width, height);
        fireDrawingModelEvent(DrawingModelEvent.layoutChanged(this, f));
        fireTreeModelEvent(TreeModelEvent.nodeChanged(this, f));
    }

    @Override
    public void reshapeInLocal(Figure f, CssSize x, CssSize y, CssSize width, CssSize height) {
        f.reshapeInLocal(x, y, width, height);
        fireDrawingModelEvent(DrawingModelEvent.layoutChanged(this, f));
        fireTreeModelEvent(TreeModelEvent.nodeChanged(this, f));
    }

    @Override
    public void layout(Figure f, RenderContext ctx) {
        validating.set(true);
        f.layoutChanged(ctx);
        fireDrawingModelEvent(DrawingModelEvent.layoutChanged(this, f));
        fireTreeModelEvent(TreeModelEvent.nodeChanged(this, f));
        validating.set(false);
    }

    private final ReadOnlyBooleanWrapper validating = new ReadOnlyBooleanWrapper(this, "layoutIsInProgress");

    public boolean isValidating() {
        return validating.get();
    }

    public ReadOnlyBooleanProperty validatingProperty() {
        return validating.getReadOnlyProperty();
    }

    @Override
    public void disconnect(Figure f) {
        f.disconnect();
    }

    @Override
    public void updateCss(Figure figure) {
        figure.stylesheetChanged(new SimpleRenderContext());
    }

    @Override
    public void validate(RenderContext ctx) {
        if (!valid) {
            validating.set(true);

            // all dirty figures: invoke layoutObserverChanged/layoutSubjectChangedNotify
            for (Figure f : dirties) {
                f.layoutSubjectChanged();
                f.layoutObserverChanged();
            }

            // collect all dirty figures and their subtrees
            final Set<Figure> subtrees = Collections.newSetFromMap(new IdentityHashMap<>(dirties.size() * 2));
            for (Figure f : dirties) {
                if (subtrees.add(f) && !(f instanceof Layer)) {
                   f.preorderSpliterator().forEachRemaining(subtrees::add);
                }
            }

            // build a graph which includes all dirty subtrees and
            // 1) all their ancestors that have the LayoutsChildrenFigure marker interface
            // 2) all their layout observers transitively
            final Set<Figure> visited = Collections.newSetFromMap(new IdentityHashMap<>(subtrees.size() * 2));
            SimpleMutableDirectedGraph<Figure, Figure> graphBuilder = new SimpleMutableDirectedGraph<>();
            ArrayDeque<Figure> queue = new ArrayDeque<>(subtrees);
            while (!queue.isEmpty()) {
                Figure f = queue.removeFirst();
                if (visited.add(f)) {
                    graphBuilder.addVertex(f);

                    // 1)
                    Iterator<Figure> it = f.ancestorIterable().iterator();
                    Figure layoutRoot = f;//initialize ancestor with f, this helps if f is the root
                    Figure child = it.next();//ancestor iterable starts with f
                    while (it.hasNext()) {
                        Figure parent = it.next();
                        if (parent instanceof ChildLayoutingFigure) {
                            layoutRoot = parent;
                            graphBuilder.addVertex(layoutRoot);
                            graphBuilder.addArrow(child, layoutRoot, null);
                        } else {
                            break;
                        }
                        child = layoutRoot;
                    }

                    // 2)
                    for (Figure obs : f.getReadOnlyLayoutObservers()) {
                        graphBuilder.addVertex(obs);
                        graphBuilder.addArrow(layoutRoot, obs, null);
                        if (!visited.contains(obs)) {
                            queue.add(obs);
                        }
                    }


                }
            }
            if (graphBuilder.getVertexCount() > 0) {
                for (Figure f : new TopologicalSortAlgo().sortTopologically(graphBuilder)) {
                    f.stylesheetChanged(ctx);
                    f.layoutChanged(ctx);
                    f.transformChanged();
                    fireNodeInvalidated(f);
                }
            }

            dirties.clear();
            validating.set(false);
            valid = true;
        }

    }

    @Override
    public void fireDrawingModelEvent(DrawingModelEvent event) {
        super.fireDrawingModelEvent(event);
        onDrawingModelEvent(event);
    }

    @Override
    public void fireTreeModelEvent(TreeModelEvent<Figure> event) {
        super.fireTreeModelEvent(event);
        onTreeModelEvent(event);
    }

    protected void onDrawingModelEvent(DrawingModelEvent event) {
        if (isValidating()) {
            return;
        }

        final Figure figure = event.getNode();

        switch (event.getEventType()) {
            case TRANSFORM_CHANGED, STYLE_CHANGED, LAYOUT_CHANGED:
                markDirty(figure);
                invalidate();
                break;
            case PROPERTY_VALUE_CHANGED: {
                Key<Object> key = event.getKey();
                Object oldValue = event.getOldValue();
                Object newValue = event.getNewValue();
                figure.propertyChanged(key, oldValue, newValue);
                markDirty(figure);
                invalidate();
                break;
            }

            default:
                throw new UnsupportedOperationException(event.getEventType()
                        + "not supported");
        }
    }

    protected void onTreeModelEvent(TreeModelEvent<Figure> event) {
        if (isValidating()) {
            return;
        }

        final Figure figure = event.getNode();

        switch (event.getEventType()) {
            case NODE_ADDED_TO_PARENT:
                markDirty(figure);
                invalidate();
                break;
            case NODE_ADDED_TO_TREE:
                if (event.getRoot() instanceof Drawing) {
                    figure.addedToDrawing((Drawing) event.getRoot());
                }
                break;
            case NODE_REMOVED_FROM_TREE:
                if (event.getRoot() instanceof Drawing) {
                    figure.removedFromDrawing((Drawing) event.getRoot());
                }
                removeDirty(figure);
                break;
            case NODE_REMOVED_FROM_PARENT:
                markDirty(event.getParent());
                invalidate();
                break;
            case NODE_CHANGED:
                markDirty(event.getNode());
                invalidate();
                break;
            case ROOT_CHANGED:
                dirties.clear();
                valid = true;
                break;
            case SUBTREE_NODES_CHANGED:
                break;
            default:
                throw new UnsupportedOperationException(event.getEventType()
                        + "not supported");
        }
    }


}
