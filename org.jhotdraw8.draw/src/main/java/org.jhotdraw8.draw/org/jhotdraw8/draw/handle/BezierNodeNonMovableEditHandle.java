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
public class BezierNodeNonMovableEditHandle extends AbstractHandle {

    private static final @Nullable Background REGION_BACKGROUND = new Background(new BackgroundFill(Color.WHITE, null, null));
    private static final @Nullable Border REGION_BORDER = new Border(new BorderStroke(Color.BLUE, BorderStrokeStyle.SOLID, null, null));
    private static final Circle REGION_SHAPE_CUBIC = new Circle(0, 0, 4);
    private static final Rectangle REGION_SHAPE_LINEAR = new Rectangle(7, 7);
    private static final Path REGION_SHAPE_QUADRATIC = new Path();


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


    public BezierNodeNonMovableEditHandle(@NonNull Figure figure, @NonNull MapAccessor<BezierPath> pathKey, @NonNull int nodeIndex) {
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

    private @Nullable BezierNode getBezierNode() {
        BezierPath path = owner.get(pathKey);
        return path == null || path.size() <= nodeIndex ? null : path.get(nodeIndex);

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
        BorderStroke borderStroke = node.getBorder().getStrokes().get(0);
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



    @Override
    public void onMousePressed(@NonNull MouseEvent event, @NonNull DrawingView view) {
        if (event.isPopupTrigger()) {
            onPopupTriggered(event, view);
        }
    }

    private void onPopupTriggered(@NonNull MouseEvent event, @NonNull DrawingView view) {
        ContextMenu contextMenu = new ContextMenu();

        // path menu ----
        Menu pathMenu = new Menu(DrawLabels.getResources().getString("handle.bezierNode.path.text"));
        RadioMenuItem moveToRadio = new RadioMenuItem(DrawLabels.getResources().getString("handle.bezierNode.moveTo.text"));
        RadioMenuItem lineToRadio = new RadioMenuItem(DrawLabels.getResources().getString("handle.bezierNode.lineTo.text"));

        BezierPath path = owner.get(pathKey);
        if (path == null || nodeIndex >= path.size()) return;
        BezierPath[] finalPath = {path};
        BezierNode bnode = finalPath[0].get(nodeIndex);

        if (nodeIndex == 0 || bnode.isMoveTo()) {
            moveToRadio.setSelected(true);
        } else {
            lineToRadio.setSelected(true);
        }

        moveToRadio.setOnAction(actionEvent -> {
            BezierNode changedNode = bnode.withMaskBitsClears(CLOSE_MASK).withMaskBitsSet(MOVE_MASK);
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
        pathMenu.getItems().addAll(moveToRadio, lineToRadio);

        // tangents menu ----
        Menu tangentsMenu = new Menu(DrawLabels.getResources().getString("handle.bezierNode.tangents.text"));

        RadioMenuItem noneRadio = new RadioMenuItem();
        Actions.bindMenuItem(noneRadio, new BezierNodeHandleNoTangentsAction(owner, pathKey, nodeIndex, view));
        RadioMenuItem inRadio;
        if (nodeIndex > 0) {
            inRadio = new RadioMenuItem();
            Actions.bindMenuItem(inRadio, new BezierNodeHandleIncomingTangentAction(owner, pathKey, nodeIndex, view));
        } else {
            inRadio = null;
        }
        RadioMenuItem outRadio;
        int nodeListSize = path == null ? 0 : path.size();
        if (nodeIndex < nodeListSize - 1) {
            outRadio = new RadioMenuItem();
            Actions.bindMenuItem(outRadio, new BezierNodeHandleOutgoingTangentAction(owner, pathKey, nodeIndex, view));
        } else {
            outRadio = null;
        }
        RadioMenuItem bothRadio;
        if (nodeIndex > 0 && nodeIndex < nodeListSize - 1) {
            bothRadio = new RadioMenuItem();
            Actions.bindMenuItem(bothRadio, new BezierNodeHandleIncomingAndOutgoingTangentAction(owner, pathKey, nodeIndex, view));
        } else {
            bothRadio = null;
        }
        tangentsMenu.getItems().add(noneRadio);
        if (inRadio != null)
            tangentsMenu.getItems().add(inRadio);
        if (outRadio != null)
            tangentsMenu.getItems().add(outRadio);
        if (bothRadio != null)
            tangentsMenu.getItems().add(bothRadio);


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
        BezierPath path = f.get(pathKey);
        if (path == null || nodeIndex >= path.size()) {
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
