/*
 * @(#)SimpleDrawingView.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.draw;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlySetProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableSet;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.transform.Transform;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.application.EditableComponent;
import org.jhotdraw8.base.event.Listener;
import org.jhotdraw8.draw.constrain.Constrainer;
import org.jhotdraw8.draw.constrain.NullConstrainer;
import org.jhotdraw8.draw.figure.Drawing;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.draw.figure.SimpleDrawing;
import org.jhotdraw8.draw.gui.ZoomableScrollPane;
import org.jhotdraw8.draw.handle.Handle;
import org.jhotdraw8.draw.model.DrawingModel;
import org.jhotdraw8.draw.model.SimpleDrawingModel;
import org.jhotdraw8.draw.render.InteractiveDrawingRenderer;
import org.jhotdraw8.draw.render.InteractiveHandleRenderer;
import org.jhotdraw8.draw.tool.Tool;
import org.jhotdraw8.fxbase.beans.NonNullObjectProperty;
import org.jhotdraw8.fxbase.binding.CustomBinding;
import org.jhotdraw8.fxbase.tree.TreeBreadthFirstSpliterator;
import org.jhotdraw8.fxbase.tree.TreeModelEvent;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

/**
 * A simple implementation of {@link DrawingView}.
 * <p>
 * The SimpleDrawingView has the following scene structure:
 * <ul>
 *   <li>{@value #DRAWING_VIEW_STYLE_CLASS} – {@link BorderPane}<ul>
 *     <li>{@value ZoomableScrollPane#ZOOMABLE_SCROLL_PANE_STYLE_CLASS} – see {@link ZoomableScrollPane}<ul>
 *       <li>{@value ZoomableScrollPane#ZOOMABLE_SCROLL_PANE_VIEWPORT_STYLE_CLASS}<ul>
 *           <li>{@value ZoomableScrollPane#ZOOMABLE_SCROLL_PANE_BACKGROUND_STYLE_CLASS}<ul>
 *               <li>{@value #CANVAS_REGION_STYLE_CLASS} – {@link Region}</li>
 *           </ul></li>
 *           <li>{@value ZoomableScrollPane#ZOOMABLE_SCROLL_PANE_SUBSCENE_STYLE_CLASS}<ul>
 *              <li>content</li>
 *           </ul></li>
 *           <li>{@value ZoomableScrollPane#ZOOMABLE_SCROLL_PANE_FOREGROUND_STYLE_CLASS}</li>
 *       </ul></li>
 *     </ul></li>
 *     </ul></li>
 * </ul>
 * The scene node of the SimpleDrawingView has the following structure and
 * CSS style classes:
 */
public class SimpleDrawingView extends AbstractDrawingView {
    /**
     * The style class of the canvas pane is {@value #CANVAS_REGION_STYLE_CLASS}.
     */
    public static final String CANVAS_REGION_STYLE_CLASS = "jhotdraw8-drawing-view-canvas-region";

    /**
     * The style class of the drawing view is {@value #DRAWING_VIEW_STYLE_CLASS}.
     */
    public static final String DRAWING_VIEW_STYLE_CLASS = "jhotdraw8-drawing-view";
    private final @NonNull ZoomableScrollPane zoomableScrollPane = ZoomableScrollPane.create();
    private final @NonNull SimpleDrawingViewNode node = new SimpleDrawingViewNode();
    private boolean constrainerNodeValid;

    private class SimpleDrawingViewNode extends BorderPane implements EditableComponent {

        public SimpleDrawingViewNode() {
            setFocusTraversable(true);
        }

        @Override
        public void selectAll() {
            SimpleDrawingView.this.selectAll();
        }

        @Override
        public void clearSelection() {
            SimpleDrawingView.this.clearSelection();
        }

        @Override
        public ReadOnlyBooleanProperty selectionEmptyProperty() {
            return SimpleDrawingView.this.selectedFiguresProperty().emptyProperty();
        }

        @Override
        public void deleteSelection() {
            SimpleDrawingView.this.deleteSelection();
        }

        @Override
        public void duplicateSelection() {
            SimpleDrawingView.this.duplicateSelection();
        }

        @Override
        public void cut() {
            SimpleDrawingView.this.cut();
        }

        @Override
        public void copy() {
            SimpleDrawingView.this.copy();
        }

        @Override
        public void paste() {
            SimpleDrawingView.this.paste();
        }
    }

    private final @NonNull NonNullObjectProperty<DrawingModel> model //
            = new NonNullObjectProperty<>(this, MODEL_PROPERTY, new SimpleDrawingModel());
    private final @NonNull ReadOnlyObjectWrapper<Drawing> drawing = new ReadOnlyObjectWrapper<>(this, DRAWING_PROPERTY);
    private final @NonNull ObjectProperty<Figure> activeParent = new SimpleObjectProperty<>(this, ACTIVE_PARENT_PROPERTY);
    private final @NonNull NonNullObjectProperty<Constrainer> constrainer = new NonNullObjectProperty<>(this, CONSTRAINER_PROPERTY, new NullConstrainer());
    private final @NonNull ReadOnlyBooleanWrapper focused = new ReadOnlyBooleanWrapper(this, FOCUSED_PROPERTY);
    private final @NonNull Region background = new Region();
    private final @NonNull StackPane foreground = new StackPane();
    private final @NonNull InteractiveDrawingRenderer drawingRenderer = new InteractiveDrawingRenderer();
    private final @NonNull InteractiveHandleRenderer handleRenderer = new InteractiveHandleRenderer();
    private boolean isLayoutValid = true;
    private final @NonNull Listener<TreeModelEvent<Figure>> treeModelListener = this::onTreeModelEvent;


    public SimpleDrawingView() {
        initStyle();
        initLayout();
        initBindings();
        initBehavior();
    }

    protected void initStyle() {
        background.getStyleClass().add(CANVAS_REGION_STYLE_CLASS);
        node.getStyleClass().add(DRAWING_VIEW_STYLE_CLASS);
    }

    protected void initBehavior() {
        drawingRenderer.setRenderContext(this);
    }

    @Override
    public @NonNull ObjectProperty<Figure> activeParentProperty() {
        return activeParent;
    }

    @Override
    public @NonNull NonNullObjectProperty<Constrainer> constrainerProperty() {
        return constrainer;
    }


    @Override
    public @NonNull ReadOnlyObjectProperty<Drawing> drawingProperty() {
        return drawing.getReadOnlyProperty();
    }

    @Override
    public @Nullable Node findFigureNode(@NonNull Figure figure, double vx, double vy) {
        return drawingRenderer.findFigureNode(figure, vx, vy);
    }

    @Override
    public @NonNull List<Map.Entry<Figure, Double>> findFigures(double vx, double vy, boolean decompose, @NonNull Predicate<Figure> predicate) {
        return drawingRenderer.findFigures(vx, vy, decompose, predicate);
    }

    @Override
    public @NonNull List<Map.Entry<Figure, Double>> findFiguresInside(double vx, double vy, double vwidth, double vheight, boolean decompose) {
        return drawingRenderer.findFiguresInside(vx, vy, vwidth, vheight, decompose, Figure::isSelectable);
    }

    @Override
    public @NonNull List<Map.Entry<Figure, Double>> findFiguresIntersecting(double vx, double vy, double vwidth, double vheight, boolean decompose, Predicate<Figure> predicate) {
        return drawingRenderer.findFiguresIntersecting(vx, vy, vwidth, vheight, decompose, predicate);
    }

    @Override
    public @Nullable Handle findHandle(double vx, double vy) {
        return handleRenderer.findHandle(vx, vy);
    }

    @Override
    public @NonNull ReadOnlyBooleanProperty focusedProperty() {
        return focused.getReadOnlyProperty();
    }

    @Override
    public @NonNull Set<Figure> getFiguresWithCompatibleHandle(Collection<Figure> figures, Handle handle) {
        return handleRenderer.getFiguresWithCompatibleHandle(figures, handle);
    }

    @Override
    public @NonNull Node getNode() {
        return node;
    }

    @Override
    public @Nullable Node getNode(@NonNull Figure f) {
        return drawingRenderer.getNode(f);
    }

    @Override
    public @NonNull Transform getViewToWorld() {
        return zoomableScrollPane.getViewToContent();
    }

    @Override
    public Bounds getVisibleRect() {
        return worldToView(zoomableScrollPane.getVisibleContentRect());
    }

    @Override
    public @NonNull Transform getWorldToView() {
        return zoomableScrollPane.getContentToView();
    }

    private void initBindings() {
        CustomBinding.bind(drawing, model, DrawingModel::drawingProperty);
        model.addListener(this::onDrawingModelChanged);
        model.get().setRoot(new SimpleDrawing());
        onDrawingModelChanged(model, null, model.getValue());
        drawingRenderer.modelProperty().bind(this.modelProperty());
        drawingRenderer.clipBoundsProperty().bind(zoomableScrollPane.visibleContentRectProperty());
        drawingRenderer.editorProperty().bind(this.editorProperty());
        drawingRenderer.setDrawingView(this);
        handleRenderer.modelProperty().bind(this.modelProperty());
        handleRenderer.setSelectedFigures(getSelectedFigures());
        handleRenderer.editorProperty().bind(this.editorProperty());
        handleRenderer.setDrawingView(this);
        zoomFactorProperty().addListener(this::onZoomFactorChanged);
        constrainer.addListener(this::onConstrainerChanged);
        zoomableScrollPane.visibleContentRectProperty().addListener(this::onViewRectChanged);
        zoomableScrollPane.contentToViewProperty().addListener(this::onContentToViewChanged);
        CustomBinding.bind(drawing, model, DrawingModel::drawingProperty);
        CustomBinding.bind(focused, toolProperty(), Tool::focusedProperty);
    }

    private void onContentToViewChanged(@NonNull Observable observable) {
        updateBackgroundNode();
    }

    private void onConstrainerChanged(@NonNull Observable o, @Nullable Constrainer oldValue, @Nullable Constrainer newValue) {
        if (oldValue != null) {
            foreground.getChildren().remove(oldValue.getNode());
            oldValue.removeListener(this::onConstrainerInvalidated);
        }
        if (newValue != null) {
            Node node = newValue.getNode();
            node.setManaged(false);
            foreground.getChildren().add(0, node);
            node.applyCss();
            newValue.updateNode(this);
            newValue.addListener(this::onConstrainerInvalidated);
            invalidateConstrainer();
            repaint();
        }
    }

    private void invalidateConstrainer() {
        constrainerNodeValid = false;
    }

    private void onConstrainerInvalidated(@NonNull Observable o) {
        invalidateConstrainer();
        repaint();
    }

    private void onZoomFactorChanged(@NonNull Observable observable) {
        revalidateLayout();
    }

    private void onViewRectChanged(@NonNull Observable observable, @Nullable Bounds oldValue, @Nullable Bounds newValue) {
        revalidateLayout();
    }

    private void initLayout() {
        node.setCenter(zoomableScrollPane.getNode());
        background.setManaged(false);
        zoomableScrollPane.getContentChildren().add(drawingRenderer.getNode());
        zoomableScrollPane.getBackgroundChildren().add(background);
        zoomableScrollPane.getForegroundChildren().addAll(
                handleRenderer.getNode(),
                foreground);
        foreground.setManaged(false);
    }

    @Override
    protected void invalidateHandles() {

    }

    @Override
    public void jiggleHandles() {
        handleRenderer.jiggleHandles();
    }

    @Override
    public @NonNull NonNullObjectProperty<DrawingModel> modelProperty() {
        return model;
    }

    private void onDrawingChanged() {
    }

    private void onDrawingModelChanged(@NonNull Observable o, @Nullable DrawingModel oldValue, @Nullable DrawingModel newValue) {
        if (oldValue != null) {
            oldValue.removeTreeModelListener(treeModelListener);
        }
        if (newValue != null) {
            newValue.addTreeModelListener(treeModelListener);
            revalidateLayout();
        }
    }


    private void onNodeChanged(@NonNull Figure f) {
        if (f == getDrawing()) {
            revalidateLayout();
        }
    }

    private void onRootChanged() {
        onDrawingChanged();
        clearSelection();

        revalidateLayout();
        repaint();
    }

    private void onSubtreeNodesChanged(@NonNull Figure f) {
    }

    @Override
    protected void onToolChanged(@NonNull Observable observable, @Nullable Tool oldValue, @Nullable Tool newValue) {
        if (oldValue != null) {
            foreground.getChildren().remove(oldValue.getNode());
            oldValue.setDrawingView(null);
        }
        if (newValue != null) {
            Node node = newValue.getNode();
            node.setManaged(true);// we want the tool to fill the view
            foreground.getChildren().add(node);
            newValue.setDrawingView(this);
        }
    }

    private void onTreeModelEvent(@NonNull TreeModelEvent<Figure> event) {
        Figure f = event.getNode();
        switch (event.getEventType()) {
            case NODE_ADDED_TO_PARENT:
            case NODE_REMOVED_FROM_PARENT:
            case NODE_ADDED_TO_TREE:
                break;
            case NODE_REMOVED_FROM_TREE:
                onNodeRemoved(f);
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

    private void onNodeRemoved(@NonNull Figure f) {
        ObservableSet<Figure> selectedFigures = getSelectedFigures();
        for (Figure d : f.preorderIterable()) {
            selectedFigures.remove(d);
        }
        repaint();
    }

    @Override
    public void recreateHandles() {
        handleRenderer.recreateHandles();
    }

    private @Nullable Runnable repainter = null;

    @Override
    protected void repaint() {
        if (repainter == null) {
            repainter = this::paint;
            Platform.runLater(repainter);
        }
    }

    private void paint() {
        repainter = null;
        if (!constrainerNodeValid) {
            updateConstrainerNode();
            constrainerNodeValid = true;
        }
    }

    /**
     * For testing: paints the drawing immediately.
     */
    public void paintImmediately() {
        drawingRenderer.paintImmediately();
        paint();
    }

    private void updateBackgroundNode() {
        Drawing drawing = getDrawing();
        Bounds bounds = drawing == null ? new BoundingBox(0, 0, 10, 10) : drawing.getLayoutBounds();
        Bounds bounds1 = worldToView(bounds);
        double x = bounds1.getMinX();
        double y = bounds1.getMinY();
        double w = bounds1.getWidth();
        double h = bounds1.getHeight();

        double p = 0;
        background.resizeRelocate(x - p, y - p, w + 2 * p, h + 2 * p);
    }

    private void updateConstrainerNode() {
        Constrainer c = getConstrainer();
        if (c != null) {
            c.updateNode(this);
        }
    }

    private void revalidateLayout() {
        if (isLayoutValid) {
            isLayoutValid = false;
            validateLayout();
            //Platform.runLater(this::validateLayout);
        }
    }

    @Override
    public void scrollRectToVisible(@NonNull Bounds boundsInView) {
        zoomableScrollPane.scrollViewRectToVisible(boundsInView);
    }

    private void updateLayout() {
        Drawing drawing = getDrawing();
        Bounds bounds = drawing == null ? new BoundingBox(0, 0, 10, 10) : drawing.getLayoutBounds();
        double f = getZoomFactor();
        double w = bounds.getWidth();
        double h = bounds.getHeight();
        zoomableScrollPane.setContentSize(w, h);
        Bounds vp = zoomableScrollPane.getViewportRect();
        foreground.resize(vp.getWidth(), vp.getHeight());

        handleRenderer.invalidateHandleNodes();
        handleRenderer.repaint();
        updateConstrainerNode();
        updateBackgroundNode();
    }

    private void validateLayout() {
        if (!isLayoutValid) {
            updateLayout();
            isLayoutValid = true;
        }
    }

    @Override
    public @NonNull DoubleProperty zoomFactorProperty() {
        return zoomableScrollPane.zoomFactorProperty();
    }

    /**
     * Selects all enabled and selectable figures in all enabled layers.
     */

    public void selectAll() {
        ArrayList<Figure> figures = new ArrayList<>();
        Drawing d = getDrawing();
        if (d != null) {
            for (Figure layer : d.getChildren()) {
                if (layer.isEditable() && layer.isVisible()) {
                    for (Figure f : layer.getChildren()) {
                        if (f.isSelectable()) {
                            figures.add(f);
                        }
                    }
                }
            }
        }
        getSelectedFigures().clear();
        getSelectedFigures().addAll(figures);
    }

    public void clearSelection() {
        getSelectedFigures().clear();
    }

    public void deleteSelection() {
        ArrayList<Figure> figures = new ArrayList<>(getSelectedFigures());
        DrawingModel model = getModel();

        // Also delete dependent figures.
        Deque<Figure> cascade = new ArrayDeque<>(figures);
        for (Figure f : figures) {
            for (Figure ff : f.preorderIterable()) {
                StreamSupport.stream(new TreeBreadthFirstSpliterator<>(
                                        figure -> () ->
                                                figure.getReadOnlyLayoutObservers().stream()
                                                        .filter(x -> x.getLayoutSubjects().size() == 1).iterator(), ff
                                ),
                                false)
                        .forEach(cascade::addFirst);
            }
        }
        for (Figure f : cascade) {
            if (f.isDeletable()) {
                for (Figure d : f.preorderIterable()) {
                    model.disconnect(d);
                }
                model.removeFromParent(f);
            }
        }
    }

    public void duplicateSelection() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public @NonNull ReadOnlySetProperty<Handle> handlesProperty() {
        return handleRenderer.handlesProperty();
    }
}
