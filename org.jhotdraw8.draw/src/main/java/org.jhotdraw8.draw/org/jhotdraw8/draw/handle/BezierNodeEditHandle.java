/*
 * @(#)BezierNodeEditHandle.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.handle;

import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Transform;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.application.action.Actions;
import org.jhotdraw8.draw.DrawLabels;
import org.jhotdraw8.draw.DrawingView;
import org.jhotdraw8.draw.css.value.CssColor;
import org.jhotdraw8.draw.css.value.CssPoint2D;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.fxcollection.typesafekey.MapAccessor;
import org.jhotdraw8.geom.FXTransforms;
import org.jhotdraw8.geom.Points;
import org.jhotdraw8.geom.shape.BezierNode;
import org.jhotdraw8.geom.shape.BezierPath;

import static org.jhotdraw8.draw.figure.TransformableFigure.ROTATE;
import static org.jhotdraw8.draw.figure.TransformableFigure.ROTATION_AXIS;
import static org.jhotdraw8.geom.shape.BezierNode.CLOSE_MASK;
import static org.jhotdraw8.geom.shape.BezierNode.MOVE_MASK;

/**
 * Handle for the point ofCollection a figure.
 *
 * @author Werner Randelshofer
 */
public class BezierNodeEditHandle extends AbstractHandle {

    private static final @Nullable Background REGION_BACKGROUND = new Background(new BackgroundFill(Color.WHITE, null, null));
    private static final @Nullable Border REGION_BORDER = new Border(new BorderStroke(Color.BLUE, BorderStrokeStyle.SOLID, null, null));
    private static final @NonNull Circle REGION_SHAPE_CUBIC = new Circle(0, 0, 4);
    private static final @NonNull Rectangle REGION_SHAPE_LINEAR = new Rectangle(7, 7);
    private static final @NonNull Path REGION_SHAPE_QUADRATIC = new Path();


    static {
        final ObservableList<PathElement> elements = REGION_SHAPE_QUADRATIC.getElements();
        elements.add(new MoveTo(2, 0));
        elements.add(new LineTo(4, 0));
        elements.add(new LineTo(6, 2));
        elements.add(new LineTo(6, 4));
        elements.add(new LineTo(4, 6));
        elements.add(new LineTo(2, 6));
        elements.add(new LineTo(0, 4));
        elements.add(new LineTo(0, 2));
        elements.add(new ClosePath());
        elements.add(new MoveTo(3, 0));
        elements.add(new LineTo(3, 6));
    }

    private final @NonNull Region node;
    private @Nullable Point2D pickLocation;
    private final int nodeIndex;
    private final @NonNull MapAccessor<BezierPath> pathKey;


    public BezierNodeEditHandle(@NonNull Figure figure, @NonNull MapAccessor<BezierPath> pathKey, int nodeIndex) {
        super(figure);
        this.pathKey = pathKey;
        this.nodeIndex = nodeIndex;
        node = new Region();
        node.setShape(REGION_SHAPE_LINEAR);
        node.setManaged(false);
        node.setScaleShape(true);
        node.setCenterShape(true);
        node.resize(11, 11);
        node.setBorder(REGION_BORDER);
        node.setBackground(REGION_BACKGROUND);
    }

    @Override
    public boolean contains(DrawingView drawingView, double x, double y, double tolerance) {
        Point2D p = getLocationInView();
        return Points.squaredDistance(x, y, p.getX(), p.getY()) <= tolerance * tolerance;
    }

    private BezierNode getBezierNode() {
        BezierPath list = owner.get(pathKey);
        return list.get(nodeIndex);

    }

    @Override
    public Cursor getCursor() {
        return Cursor.CROSSHAIR;
    }

    private @NonNull Point2D getLocation() {
        return getBezierNode().getPoint(Point2D::new);

    }

    public Point2D getLocationInView() {
        return pickLocation;
    }

    @Override
    public @NonNull Region getNode(@NonNull DrawingView view) {
        double size = view.getEditor().getHandleSize();
        if (node.getWidth() != size) {
            node.resize(size, size);
        }
        CssColor color = view.getEditor().getHandleColor();
        BorderStroke borderStroke = node.getBorder().getStrokes().getFirst();
        if (!borderStroke.getTopStroke().equals(color.getColor())) {
            node.setBorder(new Border(
                    new BorderStroke(color.getColor(), BorderStrokeStyle.SOLID, null, null)
            ));
        }

        return node;
    }

    @Override
    public void onMouseClicked(@NonNull MouseEvent event, @NonNull DrawingView dv) {
        // do nothing on mouse clicked, because it may happen accidentally
    }

    private void removePoint(@NonNull DrawingView dv) {
        BezierPath path = owner.get(pathKey);
        if (path != null && path.size() > 2) {
            path = path.join(nodeIndex, 1.0);
            dv.getModel().set(owner, pathKey, path);
            dv.recreateHandles();
        }
    }

    /**
     * Insert a new node before the node at pointIndex.
     *
     * @param view
     */
    private void addPoint(@NonNull DrawingView view) {
        BezierPath path = owner.get(pathKey);
        if (path == null) {
            path = BezierPath.of();
        }

        BezierNode node = path.get(nodeIndex);

        // If the oldNode was a MOVE_TO, convert it into a LINE_TO
        path = path.set(nodeIndex, node.withMaskBitsClears(MOVE_MASK));

        // Remove the CLOSE path mask from the new node
        path = path.add(nodeIndex, node.withMaskBitsClears(CLOSE_MASK));

        view.getModel().set(owner, pathKey, path);
        view.recreateHandles();
    }

    @Override
    public void onMouseDragged(@NonNull MouseEvent event, @NonNull DrawingView view) {
        Point2D newPoint = view.viewToWorld(new Point2D(event.getX(), event.getY()));

        if (!event.isAltDown() && !event.isControlDown()) {
            // alt or control switches the constrainer off
            newPoint = view.getConstrainer().constrainPoint(getOwner(), new CssPoint2D(newPoint)).getConvertedValue();
        }

        BezierPath list = owner.get(pathKey);
        if (nodeIndex >= list.size()) {
            return;
        }
        BezierNode p = list.get(nodeIndex);
        view.getModel().set(getOwner(), pathKey,
                list.set(nodeIndex, p.withPointAndTranslatedInOut(getOwner().worldToLocal(newPoint))));
    }

    @Override
    public void onMousePressed(@NonNull MouseEvent event, @NonNull DrawingView view) {
        if (event.isPopupTrigger()) {
            onPopupTriggered(event, view);
        }
    }

    private void onPopupTriggered(@NonNull MouseEvent event, @NonNull DrawingView view) {
        BezierPath path = owner.get(pathKey);
        if (path == null) {
            return;
        }
        ContextMenu contextMenu = new ContextMenu();

        // add point / remove point menu ----
        MenuItem addPoint = new MenuItem(DrawLabels.getResources().getString("handle.addPoint.text"));
        MenuItem removePoint = new MenuItem(DrawLabels.getResources().getString("handle.removePoint.text"));
        addPoint.setOnAction(actionEvent -> addPoint(view));
        removePoint.setOnAction(actionEvent -> removePoint(view));
        contextMenu.getItems().addAll(addPoint, removePoint);

        // tangents menu ----
        Menu tangentsMenu = new Menu(DrawLabels.getResources().getString("handle.bezierNode.tangents.text"));
        RadioMenuItem noneRadio = new RadioMenuItem();
        Actions.bindMenuItem(noneRadio, new BezierNodeHandleNoTangentsAction(owner, pathKey, nodeIndex, view));
        RadioMenuItem inRadio = new RadioMenuItem();
        Actions.bindMenuItem(inRadio, new BezierNodeHandleIncomingTangentAction(owner, pathKey, nodeIndex, view));
        RadioMenuItem outRadio = new RadioMenuItem();
        Actions.bindMenuItem(outRadio, new BezierNodeHandleOutgoingTangentAction(owner, pathKey, nodeIndex, view));
        RadioMenuItem bothRadio = new RadioMenuItem();
        Actions.bindMenuItem(bothRadio, new BezierNodeHandleIncomingAndOutgoingTangentAction(owner, pathKey, nodeIndex, view));
        tangentsMenu.getItems().addAll(noneRadio, inRadio, outRadio, bothRadio);

        // path menu ----
        Menu pathMenu = new Menu(DrawLabels.getResources().getString("handle.bezierNode.path.text"));
        RadioMenuItem moveToRadio = new RadioMenuItem(DrawLabels.getResources().getString("handle.bezierNode.moveTo.text"));
        RadioMenuItem lineToRadio = new RadioMenuItem(DrawLabels.getResources().getString("handle.bezierNode.lineTo.text"));
        RadioMenuItem closePathRadio = new RadioMenuItem(DrawLabels.getResources().getString("handle.bezierNode.closePath.text"));
        closePathRadio.setDisable(nodeIndex == 0);

        BezierPath[] finalPath = {path};
        BezierNode bnode = finalPath[0].get(nodeIndex);

        if (nodeIndex == 0 || bnode.isMoveTo()) {
            moveToRadio.setSelected(true);
        } else if (bnode.isClosePath()) {
            closePathRadio.setSelected(true);
        } else {
            lineToRadio.setSelected(true);
        }

        moveToRadio.setOnAction(actionEvent -> {
            BezierNode changedNode = bnode.withMaskBitsClears(CLOSE_MASK).withMaskBitsSet(MOVE_MASK);
            finalPath[0] = finalPath[0].set(nodeIndex, changedNode);
            view.getModel().set(owner, pathKey, finalPath[0]);
            view.recreateHandles();
        });
        closePathRadio.setOnAction(actionEvent -> {
            BezierNode changedNode = bnode.withMaskBitsSet(CLOSE_MASK).withMaskBitsClears(MOVE_MASK);
            finalPath[0] = finalPath[0].set(nodeIndex, changedNode);
            view.getModel().set(owner, pathKey, finalPath[0]);
            view.recreateHandles();
        });
        lineToRadio.setOnAction(actionEvent -> {
            BezierNode changedNode = bnode.withMaskBitsClears(MOVE_MASK | CLOSE_MASK);
            finalPath[0] = finalPath[0].set(nodeIndex, changedNode);
            view.getModel().set(owner, pathKey, finalPath[0]);
            view.recreateHandles();
        });
        pathMenu.getItems().addAll(moveToRadio, lineToRadio, closePathRadio);

        // assemble all menus ----
        contextMenu.getItems().add(tangentsMenu);
        contextMenu.getItems().add(pathMenu);
        contextMenu.show(node, event.getScreenX(), event.getScreenY());
        event.consume();
    }


    @Override
    public void onMouseReleased(@NonNull MouseEvent event, @NonNull DrawingView view) {
        if (event.isPopupTrigger()) {
            onPopupTriggered(event, view);
        }
    }

    @Override
    public boolean isSelectable() {
        return true;
    }

    @Override
    public void updateNode(@NonNull DrawingView view) {
        Figure f = getOwner();
        Transform t = FXTransforms.concat(view.getWorldToView(), f.getLocalToWorld());
        BezierPath list = f.get(pathKey);
        if (list == null || nodeIndex >= list.size()) {
            node.setVisible(false);
            return;
        } else {
            node.setVisible(true);
        }
        Point2D c0 = getLocation();
        pickLocation = c0 = FXTransforms.transform(t, c0);
        double size = node.getWidth();
        node.relocate(c0.getX() - size * 0.5, c0.getY() - size * 0.5);
        // rotates the node:
        node.setRotate(f.getStyledNonNull(ROTATE));
        node.setRotationAxis(f.getStyled(ROTATION_AXIS));

        BezierNode bn = getBezierNode();
        if (bn.hasIn() && bn.hasOut()) {
            node.setShape(REGION_SHAPE_CUBIC);// FIXME this is not correct
        } else if (bn.hasIn() || bn.hasOut()) {
            node.setShape(REGION_SHAPE_QUADRATIC);// FIXME this is not correct
        } else {
            node.setShape(REGION_SHAPE_LINEAR);
        }
    }

}
