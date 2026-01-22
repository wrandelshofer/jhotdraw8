/*
 * @(#)SimpleDrawingView.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.draw;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.*;
import javafx.collections.ObservableSet;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.transform.Transform;
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
import org.jspecify.annotations.Nullable;

import java.util.*;
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
    private final ZoomableScrollPane zoomableScrollPane = ZoomableScrollPane.create();
    private final SimpleDrawingViewNode node = new SimpleDrawingViewNode();
    private final NonNullObjectProperty<DrawingModel> model //
            = new NonNullObjectProperty<>(this, MODEL_PROPERTY, new SimpleDrawingModel());
    private final ReadOnlyObjectWrapper<Drawing> drawing = new ReadOnlyObjectWrapper<>(this, DRAWING_PROPERTY);
    private final ObjectProperty<Figure> activeParent = new SimpleObjectProperty<>(this, ACTIVE_PARENT_PROPERTY);
    private final NonNullObjectProperty<Constrainer> constrainer = new NonNullObjectProperty<>(this, CONSTRAINER_PROPERTY, new NullConstrainer());
    private final ReadOnlyBooleanWrapper focused = new ReadOnlyBooleanWrapper(this, FOCUSED_PROPERTY);
    private final Region background = new Region();
    private final StackPane foreground = new StackPane();
    private final InteractiveDrawingRenderer drawingRenderer = new InteractiveDrawingRenderer();
    private final InteractiveHandleRenderer handleRenderer = new InteractiveHandleRenderer();
    private boolean constrainerNodeValid;
    private boolean isLayoutValid = true;
    private @Nullable Runnable repainter = null;
    private final Listener<TreeModelEvent<Figure>> treeModelListener = this::onTreeModelEvent;


    public SimpleDrawingView() {
        initStyle();
        initLayout();
        initBindings();
        initBehavior();
    }

    @Override
    public ObjectProperty<Figure> activeParentProperty() {
        return activeParent;
    }

    public void clearSelection() {
        getSelectedFigures().clear();
    }

    @Override
    public NonNullObjectProperty<Constrainer> constrainerProperty() {
        return constrainer;
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

    @Override
    public ReadOnlyObjectProperty<Drawing> drawingProperty() {
        return drawing.getReadOnlyProperty();
    }

    public void duplicateSelection() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public @Nullable Node findFigureNode(Figure figure, double vx, double vy) {
        return drawingRenderer.findFigureNode(figure, vx, vy);
    }

    @Override
    public List<Map.Entry<Figure, Double>> findFigures(double vx, double vy, boolean decompose, Predicate<Figure> predicate) {
        return drawingRenderer.findFigures(vx, vy, decompose, predicate);
    }

    @Override
    public List<Map.Entry<Figure, Double>> findFiguresInside(double vx, double vy, double vwidth, double vheight, boolean decompose) {
        return drawingRenderer.findFiguresInside(vx, vy, vwidth, vheight, decompose, Figure::isSelectable);
    }

    @Override
    public List<Map.Entry<Figure, Double>> findFiguresIntersecting(double vx, double vy, double vwidth, double vheight, boolean decompose, Predicate<Figure> predicate) {
        return drawingRenderer.findFiguresIntersecting(vx, vy, vwidth, vheight, decompose, predicate);
    }

    @Override
    public @Nullable Handle findHandle(double vx, double vy) {
        return handleRenderer.findHandle(vx, vy);
    }

    @Override
    public ReadOnlyBooleanProperty focusedProperty() {
        return focused.getReadOnlyProperty();
    }

    @Override
    public Set<Figure> getFiguresWithCompatibleHandle(Collection<Figure> figures, Handle handle) {
        return handleRenderer.getFiguresWithCompatibleHandle(figures, handle);
    }

    @Override
    public Node getNode() {
        return node;
    }

    @Override
    public @Nullable Node getNode(Figure f) {
        return drawingRenderer.getNode(f);
    }

    @Override
    public Transform getViewToWorld() {
        return zoomableScrollPane.getViewToContent();
    }

    @Override
    public Bounds getVisibleRect() {
        return worldToView(zoomableScrollPane.getVisibleContentRect());
    }

    @Override
    public Transform getWorldToView() {
        return zoomableScrollPane.getContentToView();
    }

    @Override
    public ReadOnlySetProperty<Handle> handlesProperty() {
        return handleRenderer.handlesProperty();
    }

    protected void initBehavior() {
        drawingRenderer.setRenderContext(this);
    }

    private void initBindings() {
        CustomBinding.bind(drawing, model, DrawingModel::drawingProperty);
        model.addListener(this::onDrawingModelChanged);
        model.get().setRoot(new SimpleDrawing());
        onDrawingModelChanged(model, null, model.getValue());
        drawingRenderer.modelProperty().bind(this.modelProperty());
        drawingRenderer.clipBoundsProperty().bind(zoomableScrollPane.visibleContentRectProperty());
        drawingRenderer.editorProperty().bind(this.editorProperty());
        drawingRenderer.zoomFactorProperty().bind(this.zoomFactorProperty());
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

    protected void initStyle() {
        background.getStyleClass().add(CANVAS_REGION_STYLE_CLASS);
        node.getStyleClass().add(DRAWING_VIEW_STYLE_CLASS);
    }

    private void invalidateConstrainer() {
        constrainerNodeValid = false;
    }

    @Override
    protected void invalidateHandles() {

    }

    @Override
    public void jiggleHandles() {
        handleRenderer.jiggleHandles();
    }

    @Override
    public NonNullObjectProperty<DrawingModel> modelProperty() {
        return model;
    }

    private void onConstrainerChanged(Observable o, @Nullable Constrainer oldValue, @Nullable Constrainer newValue) {
        if (oldValue != null) {
            foreground.getChildren().remove(oldValue.getNode());
            oldValue.removeListener(this::onConstrainerInvalidated);
        }
        if (newValue != null) {
            Node node = newValue.getNode();
            node.setManaged(false);
            foreground.getChildren().addFirst(node);
            node.applyCss();
            newValue.updateNode(this);
            newValue.addListener(this::onConstrainerInvalidated);
            invalidateConstrainer();
            repaint();
        }
    }

    private void onConstrainerInvalidated(Observable o) {
        invalidateConstrainer();
        repaint();
    }

    private void onContentToViewChanged(Observable observable) {
        updateBackgroundNode();
    }

    private void onDrawingChanged() {
    }

    private void onDrawingModelChanged(Observable o, @Nullable DrawingModel oldValue, @Nullable DrawingModel newValue) {
        if (oldValue != null) {
            oldValue.removeTreeModelListener(treeModelListener);
        }
        if (newValue != null) {
            newValue.addTreeModelListener(treeModelListener);
            revalidateLayout();
        }
    }

    private void onNodeChanged(Figure f) {
        if (f == getDrawing()) {
            revalidateLayout();
        }
    }

    private void onNodeRemoved(Figure f) {
        ObservableSet<Figure> selectedFigures = getSelectedFigures();
        for (Figure d : f.preorderIterable()) {
            selectedFigures.remove(d);
        }
        repaint();
    }

    private void onRootChanged() {
        onDrawingChanged();
        clearSelection();

        revalidateLayout();
        repaint();
    }

    private void onSubtreeNodesChanged(Figure f) {
    }

    @Override
    protected void onToolChanged(Observable observable, @Nullable Tool oldValue, @Nullable Tool newValue) {
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

    private void onTreeModelEvent(TreeModelEvent<Figure> event) {
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

    private void onViewRectChanged(Observable observable, @Nullable Bounds oldValue, @Nullable Bounds newValue) {
        revalidateLayout();
    }

    private void onZoomFactorChanged(Observable observable) {
        revalidateLayout();
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

    @Override
    public void recreateHandles() {
        handleRenderer.recreateHandles();
    }

    @Override
    protected void repaint() {
        if (repainter == null) {
            repainter = this::paint;
            Platform.runLater(repainter);
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
    public void scrollRectToVisible(Bounds boundsInView) {
        zoomableScrollPane.scrollViewRectToVisible(boundsInView);
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

    private void updateLayout() {
        Drawing drawing = getDrawing();
        Bounds bounds = drawing == null ? new BoundingBox(0, 0, 10, 10) : drawing.getLayoutBounds();
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
    public DoubleProperty zoomFactorProperty() {
        return zoomableScrollPane.zoomFactorProperty();
    }

    private class SimpleDrawingViewNode extends BorderPane implements EditableComponent {

        public SimpleDrawingViewNode() {
            setFocusTraversable(true);
        }

        @Override
        public void clearSelection() {
            SimpleDrawingView.this.clearSelection();
        }

        @Override
        public void copy() {
            SimpleDrawingView.this.copy();
        }

        @Override
        public void cut() {
            SimpleDrawingView.this.cut();
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
        public void paste() {
            SimpleDrawingView.this.paste();
        }

        @Override
        public void selectAll() {
            SimpleDrawingView.this.selectAll();
        }

        @Override
        public ReadOnlyBooleanProperty selectionEmptyProperty() {
            return SimpleDrawingView.this.selectedFiguresProperty().emptyProperty();
        }
    }
}
