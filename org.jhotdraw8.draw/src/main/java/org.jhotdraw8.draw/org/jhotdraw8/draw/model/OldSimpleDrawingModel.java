/*
 * @(#)SimpleDrawingModel.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.transform.Transform;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.event.Listener;
import org.jhotdraw8.collection.enumerator.EnumeratorSpliterator;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.draw.css.value.CssPoint2D;
import org.jhotdraw8.draw.figure.Drawing;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.draw.figure.FigurePropertyChangeEvent;
import org.jhotdraw8.draw.figure.TransformCachingFigure;
import org.jhotdraw8.draw.render.RenderContext;
import org.jhotdraw8.draw.render.SimpleRenderContext;
import org.jhotdraw8.fxbase.tree.TreeModelEvent;
import org.jhotdraw8.fxcollection.typesafekey.Key;
import org.jhotdraw8.fxcollection.typesafekey.MapAccessor;
import org.jhotdraw8.fxcollection.typesafekey.NonNullMapAccessor;
import org.jhotdraw8.graph.SimpleMutableDirectedGraph;
import org.jhotdraw8.graph.algo.TopologicalSortAlgo;

import java.util.AbstractMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * A DrawingModel for drawings which contains {@code TransformableFigure}s and
 * layout observing figures, like {@code LineConnectionFigure}.
 *
 * @author Werner Randelshofer
 */
public class OldSimpleDrawingModel extends AbstractDrawingModel {

    public OldSimpleDrawingModel() {
        this.listenOnDrawing = false;
    }

    public OldSimpleDrawingModel(boolean listenOnDrawing) {
        this.listenOnDrawing = listenOnDrawing;
    }

    @Override
    public void invalidated() {
        // empty
    }

    private class MapProxy extends AbstractMap<Key<?>, Object> {

        private @Nullable Map<Key<?>, Object> target = null;
        private @Nullable Figure figure = null;

        @Override
        public @NonNull Set<Entry<Key<?>, Object>> entrySet() {
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
                Object oldValue = target.put(key, newValue);
                if (figure != null) {
                    onPropertyChanged(figure, (Key<Object>) key, oldValue, newValue);
                }
                return oldValue;
            } else {
                return newValue;
            }
        }

    }

    private final @NonNull MapProxy mapProxy = new MapProxy();

    private boolean isValidating = false;
    private boolean valid = true;
    /**
     * Performance: Every figure has a unique reference. IdentityHashMap is faster than HashMap in this case.
     */
    private final @NonNull Map<Figure, DirtyMask> dirties = new IdentityHashMap<>();
    private final Listener<FigurePropertyChangeEvent> propertyChangeHandler = this::onPropertyChanged;
    private final @NonNull ObjectProperty<Drawing> root = new SimpleObjectProperty<Drawing>(this, ROOT_PROPERTY) {
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
    private final @NonNull BiFunction<? super DirtyMask, ? super DirtyMask, ? extends DirtyMask> mergeDirtyMask
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

    private void onPropertyChanged(@NonNull FigurePropertyChangeEvent event) {
        if (!Objects.equals(event.getOldValue(), event.getNewValue())) {
            fireDrawingModelEvent(DrawingModelEvent.propertyValueChanged(this, event.getSource(),
                    event.getKey(), event.getOldValue(),
                    event.getNewValue()));
            fireTreeModelEvent(TreeModelEvent.nodeChanged(this, event.getSource()));
        }
    }

    private <T> void onPropertyChanged(@NonNull Figure figure, @NonNull Key<T> key, @Nullable T oldValue, @Nullable T newValue) {
        fireDrawingModelEvent(DrawingModelEvent.propertyValueChanged(this, figure,
                key, oldValue, newValue));
        fireTreeModelEvent(TreeModelEvent.nodeChanged(this, figure));
    }

    private void markDirty(@NonNull Figure figure, @NonNull DirtyBits... bits) {
        dirties.merge(figure, DirtyMask.of(bits), mergeDirtyMask);
    }

    private void markDirty(@NonNull Figure figure, @NonNull DirtyMask mask) {
        dirties.merge(figure, mask, mergeDirtyMask);
    }

    private void removeDirty(@NonNull Figure figure) {
        dirties.remove(figure);
    }

    @Override
    public @NonNull ObjectProperty<Drawing> drawingProperty() {
        return root;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NonNull ObjectProperty<Figure> rootProperty() {
        return (ObjectProperty<Figure>) (ObjectProperty<?>) root;
    }

    @Override
    public void removeFromParent(@NonNull Figure child) {
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
    public Figure removeFromParent(@NonNull Figure parent, int index) {
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
    public void insertChildAt(@NonNull Figure child, @NonNull Figure parent, int index) {
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
        parent.getChildren().add(index, child);
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
    public @NonNull <T> T setNonNull(@NonNull Figure figure, @NonNull NonNullMapAccessor<T> key, @NonNull T newValue) {
        T v = set(figure, key, newValue);
        return Objects.requireNonNull(v, "oldValue");
    }

    @Override
    public <T> T set(@NonNull Figure figure, @NonNull MapAccessor<T> key, @Nullable T newValue) {
        if (key instanceof Key<?>) {
            T oldValue = figure.put(key, newValue);
            // event will be fired by method handlePropertyChanged if newValue differs from oldValue
            @SuppressWarnings({"unchecked", "RedundantSuppression"})
            Key<Object> keyObject = (Key<Object>) key;
            onPropertyChanged(figure, keyObject, oldValue, newValue);
            return oldValue;
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
    public <T> T remove(@NonNull Figure figure, @NonNull MapAccessor<T> key) {
        if (key instanceof Key<?>) {
            T oldValue = figure.remove((Key<T>) key);
            // event will be fired by method handlePropertyChanged if newValue differs from oldValue
            @SuppressWarnings({"unchecked", "RedundantSuppression"})
            Key<Object> keyObject = (Key<Object>) key;
            onPropertyChanged(figure, keyObject, oldValue, null);
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
    public <T> T remove(@NonNull Figure figure, @NonNull Key<T> key) {
        T oldValue = figure.remove(key);
        // event will be fired by method handlePropertyChanged
        onPropertyChanged(figure, key, oldValue, key.getDefaultValue());
        return oldValue;
    }

    @Override
    public @NonNull <T> Property<T> propertyAt(Figure f, Key<T> key) {
        return new DrawingModelFigureProperty<>(this, f, key);
    }

    @Override
    public void reshapeInLocal(@NonNull Figure f, Transform transform) {
        f.reshapeInLocal(transform);
        fireDrawingModelEvent(DrawingModelEvent.layoutChanged(this, f));
        fireTreeModelEvent(TreeModelEvent.nodeChanged(this, f));
    }

    @Override
    public void reshapeInParent(@NonNull Figure f, Transform transform) {
        f.reshapeInParent(transform);
        fireDrawingModelEvent(DrawingModelEvent.layoutChanged(this, f));
        fireTreeModelEvent(TreeModelEvent.nodeChanged(this, f));
    }

    @Override
    public void translateInParent(@NonNull Figure f, CssPoint2D delta) {
        f.translateInParent(delta);
        fireDrawingModelEvent(DrawingModelEvent.layoutChanged(this, f));
        fireTreeModelEvent(TreeModelEvent.nodeChanged(this, f));
    }

    @Override
    public void transformInParent(@NonNull Figure f, Transform transform) {
        f.transformInParent(transform);
        fireDrawingModelEvent(DrawingModelEvent.transformChanged(this, f));
        fireTreeModelEvent(TreeModelEvent.nodeChanged(this, f));
    }

    @Override
    public void transformInLocal(@NonNull Figure f, Transform transform) {
        f.transformInLocal(transform);
        fireDrawingModelEvent(DrawingModelEvent.transformChanged(this, f));
        fireTreeModelEvent(TreeModelEvent.nodeChanged(this, f));
    }

    @Override
    public void reshapeInLocal(@NonNull Figure f, double x, double y, double width, double height) {
        f.reshapeInLocal(x, y, width, height);
        fireDrawingModelEvent(DrawingModelEvent.layoutChanged(this, f));
        fireTreeModelEvent(TreeModelEvent.nodeChanged(this, f));
    }

    @Override
    public void reshapeInLocal(@NonNull Figure f, @NonNull CssSize x, @NonNull CssSize y, @NonNull CssSize width, @NonNull CssSize height) {
        f.reshapeInLocal(x, y, width, height);
        fireDrawingModelEvent(DrawingModelEvent.layoutChanged(this, f));
        fireTreeModelEvent(TreeModelEvent.nodeChanged(this, f));
    }

    @Override
    public void layout(@NonNull Figure f, @NonNull RenderContext ctx) {
        f.layoutChanged(ctx);
        fireDrawingModelEvent(DrawingModelEvent.layoutChanged(this, f));
        fireTreeModelEvent(TreeModelEvent.nodeChanged(this, f));
    }

    @Override
    public void disconnect(@NonNull Figure f) {
        f.disconnect();
    }

    @Override
    public void updateCss(@NonNull Figure figure) {
        figure.stylesheetChanged(new SimpleRenderContext());
    }

    @Override
    public void validate(@NonNull RenderContext ctx) {
        if (!valid) {
            isValidating = true;

            // all figures with dirty bit LAYOUT_SUBJECT
            // invoke layoutSubjectChangedNotify
            // all figures with dirty bit LAYOUT_OBSERVERS
            // invoke layoutSubjectChangedNotify
            DirtyMask dmLayoutSubject = DirtyMask.of(DirtyBits.LAYOUT_SUBJECT);
            DirtyMask dmLayoutObserversAddRemove = DirtyMask.of(DirtyBits.LAYOUT_OBSERVERS_ADDED_OR_REMOVED);
            for (Map.Entry<Figure, DirtyMask> entry : new ArrayList<>(dirties.entrySet())) {
                DirtyMask dm = entry.getValue();
                if (dm.intersects(dmLayoutSubject)) {
                    Figure f = entry.getKey();
                    f.layoutSubjectChanged();
                }
                if (dm.intersects(dmLayoutObserversAddRemove)) {
                    Figure f = entry.getKey();
                    f.layoutObserverChanged();
                }
            }

            // all figures with dirty bit "STYLE"
            // invoke stylesheetNotify
            // induce a dirty bit "TRANSFORM", "NODE" and "LAYOUT
            // Performance: Every figure has a unique reference. IdentityHashMap is faster than HashMap in this case.
            final Set<Figure> visited = Collections.newSetFromMap(new IdentityHashMap<>(dirties.size() * 2));
            DirtyMask dmStyle = DirtyMask.of(DirtyBits.STYLE);
            for (Map.Entry<Figure, DirtyMask> entry : new ArrayList<>(dirties.entrySet())) {
                DirtyMask dm = entry.getValue();
                Figure f = entry.getKey();
                if (dm.intersects(dmStyle) && visited.add(f)) {
                    f.stylesheetChanged(ctx);
                    markDirty(f, DirtyBits.NODE, DirtyBits.TRANSFORM, DirtyBits.LAYOUT);
                }
            }

            // all figures with dirty bit "TRANSFORM"
            // induce dirty bits "TRANSFORM" and "LAYOUT_OBSERVERS" on all descendants which implement the TransformingFigure interface.
            visited.clear();
            DirtyMask dmTransform = DirtyMask.of(DirtyBits.TRANSFORM);
            for (Map.Entry<Figure, DirtyMask> entry : new ArrayList<>(dirties.entrySet())) {
                Figure f = entry.getKey();
                DirtyMask dm = entry.getValue();
                if (dm.intersects(dmTransform) && visited.add(f)) {
                    for (EnumeratorSpliterator<Figure> i = f.preorderEnumerator(); i.moveNext(); ) {
                        final Figure a = i.current();
                        if (visited.add(a)) {
                            if (a instanceof TransformCachingFigure) {
                                markDirty(a, DirtyBits.TRANSFORM, DirtyBits.LAYOUT_OBSERVERS);
                            }
                        }
                    }
                }
            }

            // all figures with dirty bit "TRANSFORM"
            // invoke transformChanged
            for (Map.Entry<Figure, DirtyMask> entry : new ArrayList<>(dirties.entrySet())) {
                Figure f = entry.getKey();
                DirtyMask dm = entry.getValue();
                if (dm.intersects(dmTransform)) {
                    f.transformChanged();
                }
            }

            // for all figures with dirty bit "LAYOUT" we must also update the node of their layoutable parents
            DirtyMask dmLayout = DirtyMask.of(DirtyBits.LAYOUT);
            for (Map.Entry<Figure, DirtyMask> entry : new ArrayList<>(dirties.entrySet())) {
                Figure f = entry.getKey();
                DirtyMask dm = entry.getValue();
                if (dm.intersects(dmLayout)) {
                    for (Figure p : f.ancestorIterable()) {
                        if (p == f) {
                            continue;
                        }
                        if (p.isLayoutable()) {
                            markDirty(p, DirtyBits.LAYOUT, DirtyBits.NODE);
                        } else {
                            break;
                        }
                    }
                }
            }

            // all figures with dirty bit "LAYOUT" must be laid out
            // all observers of figures with dirty bit "LAYOUT_OBBSERVERS" must be laid out.
            // all layoutable parents must be laid out.
            visited.clear();
            DirtyMask dmLayoutObservers = DirtyMask.of(DirtyBits.LAYOUT_OBSERVERS);
            Set<Figure> todo = new LinkedHashSet<>(dirties.size() * 2);
            for (Map.Entry<Figure, DirtyMask> entry : new ArrayList<>(dirties.entrySet())) {
                Figure f = entry.getKey();
                DirtyMask dm = entry.getValue();

                if (visited.add(f)) {
                    if (dm.intersects(dmLayout)) {
                        for (EnumeratorSpliterator<Figure> i = f.preorderEnumerator(); i.moveNext(); ) {
                            todo.add(i.current());
                        }
                    } else if (dm.intersects(dmLayoutObservers)) {
                        for (Figure layoutObserver : f.getReadOnlyLayoutObservers()) {
                            todo.add(layoutObserver);
                        }
                    }
                }
            }
            // build a graph which includes all figures that must be laid out and all their observers
            // transitively
            visited.clear();
            SimpleMutableDirectedGraph<Figure, Figure> graphBuilder = new SimpleMutableDirectedGraph<>();
            ArrayDeque<Figure> queue = new ArrayDeque<>(todo);
            while (!queue.isEmpty()) {
                Figure f = queue.removeFirst();
                if (visited.add(f)) {
                    graphBuilder.addVertex(f);
                    for (Figure obs : f.getReadOnlyLayoutObservers()) {
                        graphBuilder.addVertex(obs);
                        graphBuilder.addArrow(f, obs, f);
                        if (!visited.contains(obs)) {
                            queue.add(obs);
                        }
                    }
                }
            }
            visited.clear();
            if (graphBuilder.getVertexCount() > 0) {
                for (Figure f : new TopologicalSortAlgo().sortTopologically(graphBuilder)) {
                    if (visited.add(f)) {
                        if (!f.getLayoutSubjects().isEmpty()) {
                            // The :leftToRight pseudo class may have changed,
                            // if the layout subject of the label has changed its layout.
                            f.stylesheetChanged(ctx);
                        }
                        f.layoutChanged(ctx);
                        f.transformChanged();
                        markDirty(f, DirtyBits.NODE);
                    }
                }
            }

            // For all figures with dirty flag Node
            // we must fireNodeInvalidated node
            DirtyMask dmNode = DirtyMask.of(DirtyBits.NODE);
            for (Map.Entry<Figure, DirtyMask> entry : dirties.entrySet()) {
                Figure f = entry.getKey();
                DirtyMask dm = entry.getValue();
                if (dm.intersects(dmNode)) {
                    fireNodeInvalidated(f);
                }
            }

            for (Map.Entry<Figure, DirtyMask> entry : dirties.entrySet()) {
                Figure f = entry.getKey();
                DirtyMask dm = entry.getValue();
                if (dm.intersects(dmTransform)) {
                    f.transformChanged();
                }
            }
            dirties.clear();

            isValidating = false;
            valid = true;
        }
    }

    @Override
    public void fireDrawingModelEvent(@NonNull DrawingModelEvent event) {
        super.fireDrawingModelEvent(event);
        onDrawingModelEvent(event);
    }

    @Override
    public void fireTreeModelEvent(@NonNull TreeModelEvent<Figure> event) {
        super.fireTreeModelEvent(event);
        onTreeModelEvent(event);
    }

    protected void onDrawingModelEvent(@NonNull DrawingModelEvent event) {
        if (isValidating) {
            return;
        }

        final Figure figure = event.getNode();

        switch (event.getEventType()) {
            case TRANSFORM_CHANGED:
                markDirty(figure, DirtyBits.TRANSFORM);
                invalidate();
                break;
            case PROPERTY_VALUE_CHANGED: {
                Key<Object> key = event.getKey();
                Object oldValue = event.getOldValue();
                Object newValue = event.getNewValue();
                figure.propertyChanged(key, oldValue, newValue);

                //final DirtyMask dm = fk.getDirtyMask().add(DirtyBits.STYLE);
                final DirtyMask dm = DirtyMask.of(DirtyBits.STYLE,
                        DirtyBits.LAYOUT, DirtyBits.NODE, DirtyBits.TRANSFORM,
                        DirtyBits.LAYOUT_OBSERVERS
                );
                if (!dm.isEmpty()) {
                    markDirty(figure, dm);
                    invalidate();
                }

                break;
            }
            case LAYOUT_CHANGED:
                // A layout change also changes the transform of the figure, because its center may have moved
                markDirty(figure, DirtyBits.LAYOUT, DirtyBits.TRANSFORM);
                invalidate();
                break;
            case STYLE_CHANGED:
                markDirty(figure, DirtyBits.STYLE);
                invalidate();
                break;

            default:
                throw new UnsupportedOperationException(event.getEventType()
                        + "not supported");
        }
    }

    protected void onTreeModelEvent(@NonNull TreeModelEvent<Figure> event) {
        if (isValidating) {
            return;
        }

        final Figure figure = event.getNode();

        switch (event.getEventType()) {
            case NODE_ADDED_TO_PARENT:
                markDirty(figure, DirtyBits.LAYOUT, DirtyBits.STYLE);
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
                markDirty(event.getParent(), DirtyBits.LAYOUT_OBSERVERS, DirtyBits.NODE);
                invalidate();
                break;
            case NODE_CHANGED:
                markDirty(event.getNode(), DirtyBits.TRANSFORM, DirtyBits.NODE);
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
