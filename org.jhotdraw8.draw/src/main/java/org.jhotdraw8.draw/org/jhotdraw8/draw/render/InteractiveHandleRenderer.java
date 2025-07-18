/*
 * @(#)InteractiveHandleRenderer.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.draw.render;

import javafx.animation.Transition;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlySetProperty;
import javafx.beans.property.ReadOnlySetWrapper;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.util.Duration;
import org.jhotdraw8.base.event.Listener;
import org.jhotdraw8.draw.DrawingEditor;
import org.jhotdraw8.draw.DrawingView;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.draw.handle.Handle;
import org.jhotdraw8.draw.handle.HandleType;
import org.jhotdraw8.draw.model.DrawingModel;
import org.jhotdraw8.draw.model.SimpleDrawingModel;
import org.jhotdraw8.fxbase.beans.NonNullObjectProperty;
import org.jhotdraw8.fxbase.tree.TreeModelEvent;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SequencedMap;
import java.util.Set;

public class InteractiveHandleRenderer {
    private static final String DRAWING_VIEW = "drawingView";
    private final Group handlesPane = new Group() {
        @Override
        protected void layoutChildren() {
            validateHandles();
        }
    };
    private final ObjectProperty<DrawingView> drawingView = new SimpleObjectProperty<>(this, DRAWING_VIEW);
    /**
     * This is the set of handles which are out of sync with their JavaFX node.
     */
    private final Set<Figure> dirtyHandles = new HashSet<>();
    /**
     * The selectedFiguresProperty holds the list of selected figures in the
     * sequence they were selected by the user.
     */
    private final SetProperty<Figure> selectedFigures = new SimpleSetProperty<>(this, DrawingView.SELECTED_FIGURES_PROPERTY, FXCollections.observableSet(new LinkedHashSet<>()));
    private final ObjectProperty<DrawingEditor> editor = new SimpleObjectProperty<>(this, DrawingView.EDITOR_PROPERTY, null);
    /**
     * Maps each JavaFX node to a handle in the drawing view.
     */
    private final SequencedMap<Node, Handle> nodeToHandleMap = new LinkedHashMap<>();
    private final Listener<TreeModelEvent<Figure>> treeModelListener = this::onTreeModelEvent;
    /**
     * The set of all handles which were produced by selected figures.
     */
    private final SequencedMap<Figure, List<Handle>> handles = new LinkedHashMap<>();
    private final ObservableSet<Handle> handlesView = FXCollections.observableSet(new LinkedHashSet<>());

    /**
     * Provides a read-only view on the current set of handles.
     */
    private final ReadOnlySetWrapper<Handle> handlesProperty = new ReadOnlySetWrapper<>(this, "handles", FXCollections.unmodifiableObservableSet(handlesView));

    /**
     * The set of all secondary handles. One handle at a time may create
     * secondary handles.
     */
    private final ArrayList<Handle> secondaryHandles = new ArrayList<>();
    private final ObjectProperty<Bounds> clipBounds = new SimpleObjectProperty<>(this, "clipBounds",
            new BoundingBox(0, 0, 800, 600));
    private final NonNullObjectProperty<DrawingModel> model //
            = new NonNullObjectProperty<>(this, "model", new SimpleDrawingModel());
    private boolean recreateHandles;
    private boolean handlesAreValid;

    public InteractiveHandleRenderer() {
        handlesPane.setManaged(false);
        handlesPane.setAutoSizeChildren(false);
        model.addListener(this::onDrawingModelChanged);
        clipBounds.addListener(this::onClipBoundsChanged);
        selectedFigures.addListener((SetChangeListener<Figure>) change -> recreateHandles());

    }

    private void onDrawingModelChanged(Observable o, @Nullable DrawingModel oldValue, @Nullable DrawingModel newValue) {
        if (oldValue != null) {
            oldValue.removeTreeModelListener(treeModelListener);
        }
        if (newValue != null) {
            newValue.addTreeModelListener(treeModelListener);
            onRootChanged();
        }
    }


    /**
     * Creates selection handles and adds them to the provided list.
     *
     * @param handles The provided list
     */
    protected void createHandles(Map<Figure, List<Handle>> handles) {
        List<Figure> selection = new ArrayList<>(getSelectedFigures());
        if (selection.size() > 1) {
            if (getEditor().getAnchorHandleType() != null) {
                Figure anchor = selection.getFirst();
                List<Handle> list = handles.computeIfAbsent(anchor, k -> new ArrayList<>());
                anchor.createHandles(getEditor().getAnchorHandleType(), list);
            }
            if (getEditor().getLeadHandleType() != null) {
                Figure anchor = selection.getLast();
                List<Handle> list = handles.computeIfAbsent(anchor, k -> new ArrayList<>());
                anchor.createHandles(getEditor().getLeadHandleType(), list);
            }
        }
        HandleType handleType = getEditor().getHandleType();
        if (handleType != null) {
            ArrayList<Handle> list = new ArrayList<>();
            for (Figure figure : selection) {
                figure.createHandles(handleType, list);
            }
            for (Handle h : list) {
                Figure figure = h.getOwner();
                handles.computeIfAbsent(figure, k -> new ArrayList<>()).add(h);
            }
        }
    }


    public ObjectProperty<DrawingEditor> editorProperty() {
        return editor;
    }


    @SuppressWarnings("unused")
    public ObjectProperty<DrawingView> drawingViewProperty() {
        return drawingView;
    }

    public @Nullable Handle findHandle(double vx, double vy) {
        if (recreateHandles) {
            return null;
        }
        final double tolerance = getEditor().getTolerance();
        ArrayList<Map.Entry<Node, Handle>> entries = new ArrayList<>(nodeToHandleMap.entrySet());
        for (int i = entries.size() - 1; i >= 0; i--) {
            Map.Entry<Node, Handle> e = entries.get(i);
            final Handle handle = e.getValue();
            if (!handle.isSelectable()) {
                continue;
            }
            if (handle.contains(getDrawingViewNonNull(), vx, vy, tolerance)) {
                return handle;
            }
        }
        return null;
    }

    private DrawingView getDrawingViewNonNull() {
        return Objects.requireNonNull(drawingView.get(), "drawingView");
    }

    @Nullable
    DrawingEditor getEditor() {
        return editorProperty().get();
    }

    public Set<Figure> getFiguresWithCompatibleHandle(Collection<Figure> figures, Handle master) {
        validateHandles();
        Map<Figure, Figure> result = new HashMap<>();
        for (Map.Entry<Figure, List<Handle>> entry : handles.entrySet()) {
            if (figures.contains(entry.getKey())) {
                for (Handle h : entry.getValue()) {
                    if (h.isCompatible(master)) {
                        result.put(entry.getKey(), null);
                        break;
                    }
                }
            }
        }
        return result.keySet();
    }

    public Node getNode() {
        return handlesPane;
    }

    ObservableSet<Figure> getSelectedFigures() {
        return selectedFiguresProperty().get();
    }

    public void invalidateHandleNodes() {
        handlesAreValid = false;
        dirtyHandles.addAll(handles.keySet());
    }

    public void invalidateHandles() {
        handlesAreValid = false;
    }

    public void revalidateHandles() {
        invalidateHandles();
        repaint();
    }

    public void jiggleHandles() {
        validateHandles();
        List<Handle> copiedList = handles.values().stream().flatMap(List::stream).toList();

        // We scale the handles back and forth.
        double amount = 0.1;
        Transition flash = new Transition() {
            {
                setCycleDuration(Duration.millis(100));
                setCycleCount(2);
                setAutoReverse(true);
            }

            @Override
            protected void interpolate(double frac) {
                for (Handle h : copiedList) {
                    Node node = h.getNode(getDrawingViewNonNull());
                    node.setScaleX(1 + frac * amount);
                    node.setScaleY(1 + frac * amount);
                }
            }
        };
        flash.play();
    }

    public Property<DrawingModel> modelProperty() {
        return model;
    }

    private void onClipBoundsChanged(Observable observable) {
        invalidateHandles();
        repaint();
    }

    private void onFigureRemoved(Figure figure) {
        invalidateHandles();
    }

    private void onRootChanged() {
        //clearSelection() // is performed by DrawingView
        repaint();
    }

    private void onSubtreeNodesChanged(Figure figure) {
        for (Figure f : figure.preorderIterable()) {
            dirtyHandles.add(f);
        }
    }

    private void onTreeModelEvent(TreeModelEvent<Figure> event) {
        Figure f = event.getNode();
        switch (event.getEventType()) {
            case NODE_ADDED_TO_PARENT, NODE_REMOVED_FROM_TREE, NODE_ADDED_TO_TREE:
                break;
            case NODE_REMOVED_FROM_PARENT:
                onFigureRemoved(f);
                break;
            case NODE_CHANGED:
                onNodeChanged(f);
                break;
            case ROOT_CHANGED:
                onRootChanged();
                break;
            case SUBTREE_NODES_CHANGED:
                onSubtreeNodesChanged(f);
                break;
            default:
                throw new UnsupportedOperationException(event.getEventType()
                        + " not supported");
        }
    }

    private void onNodeChanged(Figure f) {
        if (selectedFigures.contains(f)) {
            dirtyHandles.add(f);
            revalidateHandles();
        }
    }

    public void recreateHandles() {
        handlesAreValid = false;
        recreateHandles = true;
        repaint();
    }

    public void repaint() {
        handlesPane.requestLayout();
    }

    /**
     * The selected figures.
     * <p>
     * Note: The selection is represented by a {@code SequencedSet} because the
     * sequence of the selection is important.
     *
     * @return a list of the selected figures
     */
    ReadOnlySetProperty<Figure> selectedFiguresProperty() {
        return selectedFigures;
    }

    public void setDrawingView(DrawingView newValue) {
        drawingView.set(newValue);
    }

    private void updateHandles() {
        DrawingView drawingViewNonNull = getDrawingViewNonNull();
        if (recreateHandles) {
            for (Map.Entry<Figure, List<Handle>> entry : handles.entrySet()) {
                for (Handle h : entry.getValue()) {
                    h.dispose();
                }
            }
            nodeToHandleMap.clear();
            handles.clear();
            handlesPane.getChildren().clear();
            dirtyHandles.clear();

            createHandles(handles);
            handlesView.clear();
            for (List<Handle> value : handles.values()) {
                handlesView.addAll(value);
            }

            recreateHandles = false;


            // Bounds visibleRect = getVisibleRect();

            for (Map.Entry<Figure, List<Handle>> entry : handles.entrySet()) {
                for (Handle handle : entry.getValue()) {
                    Node n = handle.getNode(drawingViewNonNull);
                    if (nodeToHandleMap.put(n, handle) == null) {
                        handlesPane.getChildren().add(n);
                        n.applyCss();
                    }
                    handle.updateNode(drawingViewNonNull);
                }
            }
        } else {
            Figure[] copyOfDirtyHandles = dirtyHandles.toArray(new Figure[0]);
            dirtyHandles.clear();
            for (Figure f : copyOfDirtyHandles) {
                List<Handle> hh = handles.get(f);
                if (hh != null) {
                    for (Handle h : hh) {
                        h.updateNode(drawingViewNonNull);
                    }
                }
            }
        }

        for (Handle h : secondaryHandles) {
            h.updateNode(drawingViewNonNull);
        }
    }

    /**
     * Validates the handles.
     */
    private void validateHandles() {
        // Validate handles only, if they are invalid/*, and if
        // the DrawingView has a DrawingEditor.*/
        if (!handlesAreValid) {
            handlesAreValid = true;
            updateHandles();
        }
    }

    public void setSelectedFigures(ObservableSet<Figure> selectedFigures) {
        this.selectedFigures.set(selectedFigures);
    }

    public ReadOnlySetProperty<Handle> handlesProperty() {
        return handlesProperty.getReadOnlyProperty();
    }
}
