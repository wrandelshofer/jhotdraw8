/*
 * @(#)InteractiveDrawingRenderer.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.draw.render;

import javafx.animation.AnimationTimer;
import javafx.beans.Observable;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.scene.transform.Transform;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.event.Listener;
import org.jhotdraw8.css.DefaultUnitConverter;
import org.jhotdraw8.draw.DrawingEditor;
import org.jhotdraw8.draw.DrawingView;
import org.jhotdraw8.draw.figure.Drawing;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.draw.model.DrawingModel;
import org.jhotdraw8.draw.model.SimpleDrawingModel;
import org.jhotdraw8.fxbase.beans.AbstractPropertyBean;
import org.jhotdraw8.fxbase.beans.NonNullObjectProperty;
import org.jhotdraw8.fxbase.tree.TreeModelEvent;
import org.jhotdraw8.geom.FXTransforms;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;


public class InteractiveDrawingRenderer extends AbstractPropertyBean {
    public static final String RENDER_CONTEXT_PROPERTY = "renderContext";
    public static final String MODEL_PROPERTY = "model";
    public static final String DRAWING_VIEW_PROPERTY = "drawingView";
    private final @NonNull NonNullObjectProperty<WritableRenderContext> renderContext //
            = new NonNullObjectProperty<>(this, RENDER_CONTEXT_PROPERTY, new SimpleRenderContext());
    private final @NonNull NonNullObjectProperty<DrawingModel> model //
            = new NonNullObjectProperty<>(this, MODEL_PROPERTY, new SimpleDrawingModel());

    private final @NonNull Group drawingPane = new Group();
    private final ObjectProperty<Bounds> clipBounds = new SimpleObjectProperty<>(this, "clipBounds",
            new BoundingBox(0, 0, 800, 600));

    /**
     * This must be a linked set, so that figures are updated in first-come
     * first-serve fashion.
     * <p>
     * If many figures change constantly, and {@link #updateLimit} is a small
     * value, then the linked set ensures that all figures are updated eventually.
     */
    private final Set<Figure> dirtyFigureNodes = new LinkedHashSet<>();
    private final DoubleProperty zoomFactor = new SimpleDoubleProperty(this, "zoomFactor", 1.0);
    /**
     * @see #updateLimitProperty()
     */
    private final IntegerProperty updateLimit = new SimpleIntegerProperty(this, "updateLimit", 10_000);
    private final Map<Figure, Node> figureToNodeMap = new IdentityHashMap<>();
    private final Map<Node, Figure> nodeToFigureMap = new IdentityHashMap<>();
    private final @NonNull ObjectProperty<DrawingView> drawingView = new SimpleObjectProperty<>(this, DRAWING_VIEW_PROPERTY);
    private final @NonNull ObjectProperty<DrawingEditor> editor = new SimpleObjectProperty<>(this, DrawingView.EDITOR_PROPERTY, null);
    private final @NonNull Listener<TreeModelEvent<Figure>> treeModelListener = this::onTreeModelEvent;
    private final @NonNull NodeFinder nodeFinder = new NodeFinder();

    private final @NonNull AnimationTimer repainter = new AnimationTimer() {
        @Override
        public void handle(long now) {
            stop();
            paint();
        }
    };

    public InteractiveDrawingRenderer() {
        drawingPane.setManaged(false);
        model.addListener(this::onDrawingModelChanged);
        clipBounds.addListener(this::onClipBoundsChanged);
    }

    public ObjectProperty<Bounds> clipBoundsProperty() {
        return clipBounds;
    }

    public @NonNull ObjectProperty<DrawingView> drawingViewProperty() {
        return drawingView;
    }

    public @NonNull ObjectProperty<DrawingEditor> editorProperty() {
        return editor;
    }

    public DrawingView getDrawingView() {
        return drawingView.get();
    }

    public void setDrawingView(DrawingView drawingView) {
        this.drawingView.set(drawingView);
    }


    /**
     * Given a figure and a point in view coordinates, finds the front-most
     * JavaFX Node of the figure that intersects with the point.
     *
     * @param figure a figure
     * @param vx     x coordinate of a point in view coordinates
     * @param vy     y coordinate of a point in view coordinates
     * @return the front-most JavaFX Node of the figure that intersects with the point
     */
    public @Nullable Node findFigureNode(@NonNull Figure figure, double vx, double vy) {
        Node n = figureToNodeMap.get(figure);
        if (n == null) {
            return null;
        }
        Transform viewToNode = null;
        for (Node p = n; p != null; p = p.getParent()) {
            try {
                viewToNode = FXTransforms.concat(viewToNode, p.getLocalToParentTransform().createInverse());
            } catch (NonInvertibleTransformException e) {
                return null;
            }
            if (p == drawingPane) {
                break;
            }
        }
        Point2D pl = FXTransforms.transform(viewToNode, vx, vy);

        double tolerance = getEditor().getTolerance();
        double radius = FXTransforms.deltaTransform(viewToNode, tolerance, 0).magnitude();
        return nodeFinder.findNodeRecursive(n, pl.getX(), pl.getY(), radius);
    }


    /**
     * Finds figures that intersect with the specified point in view
     * coordinates, or that have a distance that is less than
     * the tolerance of the editor.
     *
     * @param vx        x-coordinate of the point in view coordinates
     * @param vy        y-coordinate of the point in view coordinates
     * @param decompose If true, a figure is decomposed in sub-figures and
     *                  the sub-figure is returned instead of the figure.
     * @param predicate a predicate for selecting figures
     * @return a mutable list of figures with their distance to the point
     */
    public @NonNull List<Map.Entry<Figure, Double>> findFigures(double vx, double vy, boolean decompose, @NonNull Predicate<Figure> predicate) {
        Transform vt = getDrawingView().getViewToWorld();
        Point2D pp = vt.transform(vx, vy);
        List<Map.Entry<Figure, Double>> list = new ArrayList<>();
        double tolerance = getEditor().getTolerance();
        final Parent parent = (Parent) figureToNodeMap.get(getDrawing());
        for (Node child : frontToBack(parent)) {
            findFiguresRecursive(child, child.parentToLocal(pp), list, decompose,
                    predicate,
                    FXTransforms.inverseDeltaTransform(child.getLocalToParentTransform(),
                            tolerance, 0).magnitude());
        }

        return list;
    }

    /**
     * Gets the children of this node in front-to-back order.
     *
     * @param parent a parent node
     * @return the children of the node in front-to-back-order in a new
     * mutable array
     */
    private @NonNull Node[] frontToBack(@Nullable Parent parent) {
        if (parent == null) {
            return new Node[0];
        }
        ObservableList<Node> children = parent.getChildrenUnmodifiable();
        Node[] array = new Node[children.size()];
        for (int i = 0; i < array.length; i++) {
            array[array.length - i - 1] = children.get(i);
        }
        if (array.length > 1) {
            sortByViewOrder(array);
        }
        return array;
    }

    private @Nullable Boolean canSortByViewOrder = null;
    private @Nullable Method getViewOrder = null;

    @SuppressWarnings("unchecked")
    private void sortByViewOrder(Node[] array) {
        if (canSortByViewOrder == null) {
            try {
                getViewOrder = Node.class.getMethod("getViewOrder");
                canSortByViewOrder = true;
            } catch (Throwable e) {
                canSortByViewOrder = false;
            }
        }
        if (getViewOrder != null) {
            Arrays.sort(array, Comparator.comparingDouble(n -> {
                try {
                    return (double) getViewOrder.invoke(n);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    return 0.0;
                }
            }));
        }
    }

    public @NonNull List<Map.Entry<Figure, Double>> findFiguresInside(double vx, double vy, double vwidth, double vheight, boolean decompose, Predicate<Figure> predicate) {
        Transform vt = getDrawingView().getViewToWorld();
        Point2D pxy = vt.transform(vx, vy);
        Point2D pwh = vt.deltaTransform(vwidth, vheight);
        BoundingBox r = new BoundingBox(pxy.getX(), pxy.getY(), pwh.getX(), pwh.getY());
        List<Map.Entry<Figure, Double>> list = new ArrayList<>();

        final Parent parent = (Parent) figureToNodeMap.get(getDrawing());
        for (Node child : frontToBack(parent)) {
            findFiguresInsideRecursive(child, child.parentToLocal(r), list, decompose,
                    predicate);
        }
        return list;
    }

    /**
     * Adds all descendant figures that lie inside the specified bounds to the provided
     * list of found figures.
     *
     * @param node      the node
     * @param pp        the bounds in node coordinates
     * @param found     the list of found figures
     * @param decompose whether to decompose figures
     * @param predicate a predicate for adding figures
     * @return true if one or more figures were found
     */
    private boolean findFiguresInsideRecursive(@NonNull Node node, @NonNull Bounds pp, @NonNull List<Map.Entry<Figure, Double>> found, boolean decompose, Predicate<Figure> predicate) {
        // base case
        // ---------
        if (!node.isVisible()) {
            return false;
        }

        boolean isIntersecting = pp.intersects(node.getBoundsInLocal());
        if (!isIntersecting) {
            return false;
        }
        boolean isInside = pp.contains(node.getBoundsInLocal());

        final Figure figure = nodeToFigureMap.get(node);
        final boolean isWanted = figure != null && predicate.test(figure);
        if (isInside && figure != null && !decompose && isWanted) {
            found.add(new AbstractMap.SimpleImmutableEntry<>(figure, 0.0));
            return true;
        }

        // recursive case
        // --------------
        boolean foundAChildFigure = false;
        if (node instanceof Parent) {
            Parent parent = (Parent) node;
            for (Node child : frontToBack(parent)) {
                foundAChildFigure |= findFiguresInsideRecursive(
                        child,
                        child.parentToLocal(pp),
                        found,
                        decompose,
                        predicate
                );
            }
        }
        if (isInside && !foundAChildFigure && isWanted) {
            found.add(new AbstractMap.SimpleImmutableEntry<>(figure, 0.0));
        }
        return true;
    }

    public @NonNull List<Map.Entry<Figure, Double>> findFiguresIntersecting(double vx, double vy, double vwidth, double vheight, boolean decompose, Predicate<Figure> predicate) {
        Transform vt = getDrawingView().getViewToWorld();
        Point2D pxy = vt.transform(vx, vy);
        Point2D pwh = vt.deltaTransform(vwidth, vheight);
        BoundingBox r = new BoundingBox(pxy.getX(), pxy.getY(), pwh.getX(), pwh.getY());
        List<Map.Entry<Figure, Double>> list = new ArrayList<>();
        final Parent parent = (Parent) figureToNodeMap.get(getDrawing());
        for (Node child : frontToBack(parent)) {
            findFiguresIntersectingRecursive(child, child.parentToLocal(r), list, decompose,
                    predicate);
        }
        return list;
    }

    private boolean findFiguresIntersectingRecursive(@NonNull Node node, @NonNull Bounds pp, @NonNull List<Map.Entry<Figure, Double>> found, boolean decompose, Predicate<Figure> predicate) {
        // base case
        // ---------
        if (!node.isVisible()) {
            return false;
        }

        boolean intersects = pp.intersects(node.getBoundsInLocal());
        if (!intersects) {
            return false;
        }

        final Figure figure = nodeToFigureMap.get(node);
        final boolean isWanted = figure != null && predicate.test(figure);
        if (figure != null && !decompose && isWanted) {
            found.add(new AbstractMap.SimpleImmutableEntry<>(figure, 0.0));
            return true;
        }

        // recursive case
        // --------------
        boolean foundAChildFigure = false;
        if (node instanceof Parent) {
            Parent parent = (Parent) node;
            for (Node child : frontToBack(parent)) {
                foundAChildFigure |= findFiguresIntersectingRecursive(
                        child,
                        child.parentToLocal(pp),
                        found,
                        decompose,
                        predicate
                );
            }
        }
        if (!foundAChildFigure && isWanted) {
            found.add(new AbstractMap.SimpleImmutableEntry<>(figure, 0.0));
        }
        return true;
    }

    /**
     * Finds figures within the given node that intersect with the circle
     * around the given point.
     *
     * @param node            a node
     * @param center          the center of the circle in local coordinates of the node
     * @param found           found figures are added to this list
     * @param decompose       whether figures should be decomposed
     * @param figurePredicate only figures which satisfy this predicate are added
     * @param radius          the radius of the circle around the point
     * @return whether figures were found
     */
    private boolean findFiguresRecursive(@NonNull Node node, @NonNull Point2D center,
                                         @NonNull List<Map.Entry<Figure, Double>> found, boolean decompose,
                                         @NonNull Predicate<Figure> figurePredicate, double radius) {
        // base case
        // ---------
        if (!node.isVisible()) {
            return false;
        }

        Double distance = nodeFinder.contains(node, center, radius);
        if (distance == null) {
            return false;
        }

        final Figure figure = nodeToFigureMap.get(node);
        final boolean isWanted = figure != null && figurePredicate.test(figure);
        if (figure != null && !decompose && isWanted) {
            found.add(new AbstractMap.SimpleImmutableEntry<>(figure, distance));
            return true;
        }

        // recursive case
        // --------------
        boolean foundAChildFigure = false;
        if (node instanceof Parent) {
            Parent parent = (Parent) node;
            for (Node child : frontToBack(parent)) {
                foundAChildFigure |= findFiguresRecursive(
                        child,
                        child.parentToLocal(center),
                        found,
                        decompose,
                        figurePredicate,
                        Math.abs(
                                FXTransforms.inverseDeltaTransform(
                                        child.getLocalToParentTransform(), radius, radius).getX()));
            }
        }
        if (!foundAChildFigure && isWanted) {
            found.add(new AbstractMap.SimpleImmutableEntry<>(figure, distance));
            return true;
        }
        return false;
    }

    public Bounds getClipBounds() {
        return clipBounds.get();
    }

    public void setClipBounds(Bounds clipBounds) {
        this.clipBounds.set(clipBounds);
    }

    public @Nullable Drawing getDrawing() {
        return getModel() == null ? null : getModel().getDrawing();
    }

    DrawingEditor getEditor() {
        return editorProperty().get();
    }

    public DrawingModel getModel() {
        return model.get();
    }

    public void setModel(DrawingModel model) {
        this.model.set(model);
    }

    public Node getNode() {
        return drawingPane;
    }

    public @Nullable Node getNode(@Nullable Figure f) {
        if (f == null) {
            return null;
        }
        Node n = figureToNodeMap.get(f);
        if (n == null) {
            n = f.createNode(getRenderContext());
            figureToNodeMap.put(f, n);
            nodeToFigureMap.put(n, f);
            dirtyFigureNodes.add(f);
            repaint();
        }
        return n;
    }

    public @NonNull NonNullObjectProperty<WritableRenderContext> renderContextProperty() {
        return renderContext;
    }

    public @NonNull WritableRenderContext getRenderContext() {
        return renderContext.get();
    }

    public void setRenderContext(@NonNull WritableRenderContext newValue) {
        renderContext.set(newValue);
    }

    public double getZoomFactor() {
        return zoomFactorProperty().get();
    }

    public void setZoomFactor(double newValue) {
        zoomFactorProperty().set(newValue);
    }

    private boolean hasNode(Figure f) {
        return figureToNodeMap.containsKey(f);
    }

    private void invalidateFigureNode(@NonNull Figure f) {
        if (hasNode(f)) {
            dirtyFigureNodes.add(f);
        }
    }

    private void invalidateLayerNodes() {
        Drawing drawing = getDrawing();
        if (drawing != null) {
            dirtyFigureNodes.addAll(drawing.getChildren());
        }
    }

    public @NonNull NonNullObjectProperty<DrawingModel> modelProperty() {
        return model;
    }

    private void onClipBoundsChanged(Observable observable) {
        invalidateLayerNodes();
        repaint();
    }

    private void onDrawingModelChanged(Observable o, @Nullable DrawingModel oldValue, @Nullable DrawingModel newValue) {
        if (oldValue != null) {
            oldValue.removeTreeModelListener(treeModelListener);
            dirtyFigureNodes.clear();
            figureToNodeMap.clear();
            nodeToFigureMap.clear();
        }
        if (newValue != null) {
            newValue.addTreeModelListener(treeModelListener);
            onRootChanged(newValue.getDrawing());
        }
    }

    private void onFigureAddedToParent(@NonNull Figure figure) {
        for (Figure f : figure.preorderIterable()) {
            invalidateFigureNode(f);
        }
        repaint();
    }

    private void onFigureRemovedFromParent(@NonNull Figure figure) {
        for (Figure f : figure.preorderIterable()) {
            removeNode(f);
        }
    }

    private void onNodeChanged(@NonNull Figure figure) {
        invalidateFigureNode(figure);
        repaint();
    }

    private void onNodeAddedToTree(@NonNull Figure f) {
    }

    private void onNodeRemovedFromTree(@NonNull Figure f) {
    }

    private void onRootChanged(@Nullable Figure f) {
        ObservableList<Node> children = drawingPane.getChildren();
        nodeToFigureMap.clear();
        figureToNodeMap.clear();
        Node node = getNode(f);
        if (node == null) {
            children.clear();
        } else {
            children.setAll(node);
        }
        dirtyFigureNodes.clear();
        if (f != null) {
            dirtyFigureNodes.add(f);
            repaint();
        }
    }

    private void onSubtreeNodesChanged(@NonNull Figure figure) {
        for (Figure f : figure.preorderIterable()) {
            dirtyFigureNodes.add(f);
        }
        repaint();
    }

    private void onTreeModelEvent(TreeModelEvent<Figure> event) {
        Figure f = event.getNode();
        switch (event.getEventType()) {
            case NODE_ADDED_TO_PARENT:
                onFigureAddedToParent(f);
                break;
            case NODE_REMOVED_FROM_PARENT:
                onFigureRemovedFromParent(f);
                break;
            case NODE_ADDED_TO_TREE:
                onNodeAddedToTree(f);
                break;
            case NODE_REMOVED_FROM_TREE:
                onNodeRemovedFromTree(f);
                break;
            case NODE_CHANGED:
                onNodeChanged(f);
                break;
            case ROOT_CHANGED:
                onRootChanged(f);
                break;
            case SUBTREE_NODES_CHANGED:
                onSubtreeNodesChanged(f);
                break;
            default:
                throw new UnsupportedOperationException(event.getEventType()
                        + " not supported");
        }
    }

    private void paint() {
        updateRenderContext();

        // A call to validate() may reveal new dirty nodes, and so may
        // a call to updateNodes().
        // We only update a limited number of figure nodes in one call
        // to this method. If there are remaining nodes, we update
        // them later.
        int remainingLimit = Math.max(1, getUpdateLimit());
        while (!dirtyFigureNodes.isEmpty() && remainingLimit > 0) {
            getModel().validate(getRenderContext());
            remainingLimit -= updateNodes(remainingLimit);
        }
        repainter.stop();
        if (!dirtyFigureNodes.isEmpty()) {
            repaint();
        }
    }

    /**
     * For testing: paints the drawing immediately.
     */
    public void paintImmediately() {
        paint();
    }

    private void updateRenderContext() {
        getRenderContext().set(RenderContext.CLIP_BOUNDS, getClipBounds());
        DefaultUnitConverter units = new DefaultUnitConverter(96, 1.0, 1024.0 / getZoomFactor(), 768 / getZoomFactor());
        getRenderContext().set(RenderContext.UNIT_CONVERTER_KEY, units);
    }

    private void removeNode(Figure f) {
        Node oldNode = figureToNodeMap.remove(f);
        if (oldNode != null) {
            Figure removedFigure = nodeToFigureMap.remove(oldNode);
            figureToNodeMap.remove(removedFigure);
        }
        dirtyFigureNodes.remove(f);
    }

    public void repaint() {
        repainter.start();
    }

    /**
     * Updates the nodes of the figures.
     *
     * @param limit Determines how many nodes we will update in this batch
     * @return returns the number of updated nodes
     */
    private int updateNodes(final int limit) {
        final Bounds visibleRectInWorld = getClipBounds();

        // create copies of the lists to allow for concurrent modification
        final Figure[] copyOfDirtyFigureNodes = dirtyFigureNodes.toArray(new Figure[0]);

        // If there are too many dirty figures, we update the node of
        // figures that intersect with the visible rect first.
        int count = 0;
        if (copyOfDirtyFigureNodes.length > limit) {
            for (int i = 0, n = copyOfDirtyFigureNodes.length; i < n && count < limit; i++) {
                final Figure f = copyOfDirtyFigureNodes[i];
                if (f.getVisualBoundsInWorld().intersects(visibleRectInWorld)) {
                    copyOfDirtyFigureNodes[i] = null;
                    count++;
                    final Node node = getNode(f);// this may add the node again to the list of dirties!
                    if (node != null) {
                        f.updateNode(getRenderContext(), node);
                        dirtyFigureNodes.remove(f);
                    }
                }
            }

            // If there are more figures intersecting visibleRectInWorld that need
            // to be updated. Lets update them in the next batch.
            if (count == limit) {
                return count;
            }
        }


        // Update figure nodes until we reach the limit.
        for (int i = 0, n = copyOfDirtyFigureNodes.length; i < n && count < limit; i++) {
            final Figure f = copyOfDirtyFigureNodes[i];
            count++;
            final Node node = getNode(f);// this may add the node again to the list of dirties!
            if (node != null) {
                f.updateNode(getRenderContext(), node);
                dirtyFigureNodes.remove(f);
            }
        }

        return count;
    }

    public @NonNull DoubleProperty zoomFactorProperty() {
        return zoomFactor;
    }

    public int getUpdateLimit() {
        return updateLimit.get();
    }

    /**
     * The maximal number of figures which are updated in one repaint.
     * <p>
     * The value should be sufficiently large, because a repaint is only
     * done once per frame. If the value is low, it will take many frames
     * until the drawing is completed.
     * <p>
     * If the value is set too high, then the editor may be become unresponsive
     * if lots of figures change. (For example, when new stylesheets are applied
     * to all figures).
     * <p>
     * If this is set to a value smaller or equal to zero, then no figures
     * are updated.
     *
     * @return the update limit
     */
    public IntegerProperty updateLimitProperty() {
        return updateLimit;
    }

    public void setUpdateLimit(int updateLimit) {
        this.updateLimit.set(updateLimit);
    }
}
