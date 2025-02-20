/*
 * @(#)BezierControlPointEditHandle.java
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
import org.jhotdraw8.css.value.CssColor;
import org.jhotdraw8.css.value.CssPoint2D;
import org.jhotdraw8.draw.DrawLabels;
import org.jhotdraw8.draw.DrawingEditor;
import org.jhotdraw8.draw.DrawingView;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.fxcollection.typesafekey.MapAccessor;
import org.jhotdraw8.geom.FXGeom;
import org.jhotdraw8.geom.FXTransforms;
import org.jhotdraw8.geom.Points;
import org.jhotdraw8.geom.shape.BezierNode;
import org.jhotdraw8.geom.shape.BezierPath;
import org.jspecify.annotations.Nullable;

import static org.jhotdraw8.draw.figure.TransformableFigure.ROTATE;
import static org.jhotdraw8.draw.figure.TransformableFigure.ROTATION_AXIS;

/**
 * Handle for the point of a figure.
 *
 */
public class BezierControlPointEditHandle extends AbstractHandle {
    private static final @Nullable Background REGION_BACKGROUND =
            new Background(new BackgroundFill(Color.WHITE, null, null));
    private static final @Nullable Border REGION_BORDER = new Border(
            new BorderStroke(Color.BLUE, BorderStrokeStyle.SOLID, null, null));
    private static final Path REGION_SHAPE_COLINEAR = new Path();
    private static final Rectangle REGION_SHAPE_CUSP = new Rectangle(5, 5);
    private static final Path REGION_SHAPE_EQUIDISTANT = new Path();
    private static final Circle REGION_SHAPE_SMOOTH = new Circle(0, 0, 3);

    static {
        final ObservableList<PathElement> elements = REGION_SHAPE_COLINEAR.getElements();
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

    static {
        final ObservableList<PathElement> elements = REGION_SHAPE_EQUIDISTANT.getElements();
        elements.add(new MoveTo(0, 0));
        elements.add(new LineTo(3, -3));
        elements.add(new LineTo(6, 0));
        elements.add(new LineTo(3, 3));
        elements.add(new ClosePath());
    }

    private final int controlPointMask;
    private final Region node;
    private Point2D pickLocation;
    private final int nodeIndex;
    private final MapAccessor<BezierPath> pathKey;

    public BezierControlPointEditHandle(Figure figure, MapAccessor<BezierPath> pathKey, int nodeIndex, int controlPointMask) {
        super(figure);
        this.pathKey = pathKey;
        this.nodeIndex = nodeIndex;
        this.controlPointMask = controlPointMask;
        if (this.controlPointMask != BezierNode.IN_MASK && this.controlPointMask != BezierNode.OUT_MASK) {
            throw new IllegalArgumentException("controlPoint:" + controlPointMask);
        }
        node = new Region();
        node.setShape(REGION_SHAPE_CUSP);
        node.setManaged(false);
        node.setScaleShape(true);
        node.setCenterShape(true);
        node.resize(11, 11);
        node.setBorder(REGION_BORDER);
        node.setBackground(REGION_BACKGROUND);
    }

    @Override
    public boolean contains(DrawingView dv, double x, double y, double tolerance) {
        Point2D p = getLocationInView();
        return p != null && Points.squaredDistance(x, y, p.getX(), p.getY()) <= tolerance * tolerance;
    }

    private @Nullable BezierNode getBezierNode() {
        BezierPath path = owner.get(pathKey);
        return path == null || path.size() <= nodeIndex ? null : path.get(nodeIndex);

    }

    @Override
    public Cursor getCursor() {
        return Cursor.CROSSHAIR;
    }

    private Point2D getLocation() {
        BezierNode bezierNode = getBezierNode();
        return bezierNode == null ? Point2D.ZERO : bezierNode.getC(controlPointMask, Point2D::new);

    }

    public Point2D getLocationInView() {
        return pickLocation;
    }

    @Override
    public Region getNode(DrawingView view) {
        DrawingEditor editor = view.getEditor();
        if (editor == null) {
            return node;
        }
        double size = editor.getHandleSize() * 0.8;
        if (node.getWidth() != size) {
            node.resize(size, size);
        }
        CssColor color = editor.getHandleColor();
        BorderStroke borderStroke = node.getBorder().getStrokes().getFirst();
        if (!borderStroke.getTopStroke().equals(color.getColor())) {
            node.setBorder(new Border(new BorderStroke(color.getColor(), BorderStrokeStyle.SOLID, null, null)));
        }

        return node;
    }

    @Override
    public void onMouseClicked(MouseEvent event, DrawingView dv) {
        if (event.getClickCount() == 1) {
            if (event.isControlDown() || event.isAltDown()) {
                BezierNode bn = getBezierNode();
                if (bn == null) {
                    return;
                }
                BezierPath path = owner.get(pathKey);
                if (path == null) {
                    return;
                }
                BezierNode newbn;
                if (bn.isCollinear()) {
                    if (bn.isEquidistant()) {
                        newbn = bn.withCollinear(false).withEquidistant(false);
                    } else {
                        newbn = bn.withCollinear(true).withEquidistant(true);
                    }
                } else {
                    newbn = bn.withCollinear(true).withEquidistant(false);
                }
                dv.getModel().set(owner, pathKey,
                        path.set(nodeIndex, newbn));
            }
        }
    }

    @Override
    public void onMouseDragged(MouseEvent event, DrawingView view) {
        Point2D newPoint = view.viewToWorld(new Point2D(event.getX(), event.getY()));
        final Figure f = getOwner();

        if (!event.isAltDown() && !event.isControlDown()) {
            // alt or control switches the constrainer off
            newPoint = view.getConstrainer().constrainPoint(f, new CssPoint2D(newPoint)).getConvertedValue();
        }

        BezierPath list = owner.get(pathKey);
        BezierNode bn = getBezierNode();
        if (bn == null) {
            return;
        }
        Point2D p = f.worldToLocal(newPoint);

        if (!bn.isCollinear()) {
            if (!bn.isEquidistant()) {
                // move control point independently
                BezierNode newBezierNode = bn.withC(controlPointMask, p.getX(), p.getY());
                view.getModel().set(f, pathKey,
                        list.set(nodeIndex, newBezierNode));
            } else {
                // move control point and opposite control point to same distance
                BezierNode newBezierNode = bn.withC(controlPointMask, p.getX(), p.getY());
                Point2D c0 = bn.getPoint(Point2D::new);
                double r = p.distance(c0);
                if (controlPointMask == BezierNode.IN_MASK) {
                    Point2D p2 = bn.getOut(Point2D::new);
                    Point2D dir = p2.subtract(c0).normalize();
                    if (dir.magnitude() == 0) {
                        dir = new Point2D(0, 1);// point down
                    }
                    p2 = c0.add(dir.multiply(r));
                    Point2D constrainedPoint = view.getConstrainer().constrainPoint(owner, new CssPoint2D(p2)).getConvertedValue();
                    if (Points.almostEqual(p2.getX(), p2.getY(), constrainedPoint.getX(), constrainedPoint.getY())) {
                        p2 = constrainedPoint;
                    }
                    newBezierNode = newBezierNode.withOut(p2.getX(), p2.getY());
                } else {
                    Point2D p2 = bn.getIn(Point2D::new);
                    Point2D dir = p2.subtract(c0).normalize();
                    if (dir.magnitude() == 0) {
                        dir = new Point2D(0, 1);// point down
                    }
                    p2 = c0.add(dir.multiply(r));
                    Point2D constrainedPoint = view.getConstrainer().constrainPoint(owner, new CssPoint2D(p2)).getConvertedValue();
                    if (Points.almostEqual(p2.getX(), p2.getY(), constrainedPoint.getX(), constrainedPoint.getY())) {
                        p2 = constrainedPoint;
                    }
                    newBezierNode = newBezierNode.withIn(p2.getX(), p2.getY());
                }

                view.getModel().set(f, pathKey,
                        list.set(nodeIndex, newBezierNode));
            }
        } else {
            Point2D c0 = bn.getPoint(Point2D::new);

            // move control point and opposite control point on same line
            double a = Math.PI + FXGeom.angle(c0, p);
            Point2D p2;
            if (controlPointMask == BezierNode.IN_MASK) {
                p2 = bn.getOut(Point2D::new);
            } else {
                p2 = bn.getIn(Point2D::new);
            }

            double r;
            if (bn.isEquidistant()) {
                r = Math.sqrt((p.getX() - c0.getX()) * (p.getX() - c0.getX())
                        + (p.getY() - c0.getY()) * (p.getY() - c0.getY()));
            } else {
                r = Math.sqrt((p2.getX() - c0.getX()) * (p2.getX() - c0.getX())
                        + (p2.getY() - c0.getY()) * (p2.getY() - c0.getY()));
            }
            double sina = Math.sin(a);
            double cosa = Math.cos(a);

            p2 = new Point2D(
                    Math.fma(r, cosa, c0.getX()),
                    Math.fma(r, sina, c0.getY()));
            Point2D constrainedPoint = view.getConstrainer().constrainPoint(owner, new CssPoint2D(p2)).getConvertedValue();
            if (Points.almostEqual(p2.getX(), p2.getY(), constrainedPoint.getX(), constrainedPoint.getY())) {
                p2 = constrainedPoint;
            }
            BezierNode newBezierNode;
            if (controlPointMask == BezierNode.IN_MASK) {
                newBezierNode = bn.withIn(p.getX(), p.getY()).withOut(p2.getX(), p2.getY());
            } else {
                newBezierNode = bn.withOut(p.getX(), p.getY()).withIn(p2.getX(), p2.getY());
            }
            view.getModel().set(f, pathKey,
                    list.set(nodeIndex, newBezierNode));
        }
    }

    @Override
    public void onMousePressed(MouseEvent event, DrawingView view) {
        if (event.isPopupTrigger()) {
            onPopupTriggered(event, view);
        }
    }

    private void onPopupTriggered(MouseEvent event, DrawingView view) {
        BezierPath nullablePath = owner.get(pathKey);
        if (nullablePath == null) {
            return;
        }

        ContextMenu contextMenu = new ContextMenu();

        Menu constraints = new Menu(DrawLabels.getResources().getString("handle.bezierControlPoint.constraints.text"));
        RadioMenuItem noneRadio = new RadioMenuItem(DrawLabels.getResources().getString("handle.bezierControlPoint.noConstraints.text"));
        RadioMenuItem collinearRadio = new RadioMenuItem(DrawLabels.getResources().getString("handle.bezierControlPoint.collinearConstraint.text"));
        RadioMenuItem equidistantRadio = new RadioMenuItem(DrawLabels.getResources().getString("handle.bezierControlPoint.equidistantConstraint.text"));
        RadioMenuItem bothRadio = new RadioMenuItem(DrawLabels.getResources().getString("handle.bezierControlPoint.collinearAndEquidistantConstraint.text"));

        final BezierPath[] path = {nullablePath};
        BezierNode bnode = path[0].get(nodeIndex);
        if (bnode.isEquidistant() && bnode.isCollinear()) {
            bothRadio.setSelected(true);
        } else if (bnode.isEquidistant()) {
            equidistantRadio.setSelected(true);
        } else if (bnode.isCollinear()) {
            collinearRadio.setSelected(true);
        } else {
            noneRadio.setSelected(true);
        }
        noneRadio.setOnAction(actionEvent -> {
            BezierNode changedNode = bnode.withCollinear(false).withEquidistant(false);
            path[0] = path[0].set(nodeIndex, changedNode);
            view.getModel().set(owner, pathKey, path[0]);
            view.recreateHandles();
        });
        collinearRadio.setOnAction(actionEvent -> {
            BezierNode changedNode = bnode.withCollinear(true).withEquidistant(false);
            path[0] = path[0].set(nodeIndex, changedNode);
            view.getModel().set(owner, pathKey, path[0]);
            view.recreateHandles();
        });
        equidistantRadio.setOnAction(actionEvent -> {
            BezierNode changedNode = bnode.withCollinear(false).withEquidistant(true);
            path[0] = path[0].set(nodeIndex, changedNode);
            view.getModel().set(owner, pathKey, path[0]);
            view.recreateHandles();
        });
        bothRadio.setOnAction(actionEvent -> {
            BezierNode changedNode = bnode.withCollinear(true).withEquidistant(true);
            path[0] = path[0].set(nodeIndex, changedNode);
            view.getModel().set(owner, pathKey, path[0]);
            view.recreateHandles();
        });

        constraints.getItems().addAll(noneRadio, collinearRadio, equidistantRadio, bothRadio);
        contextMenu.getItems().add(constraints);
        contextMenu.show(node, event.getScreenX(), event.getScreenY());
        event.consume();
    }

    @Override
    public void onMouseReleased(MouseEvent event, DrawingView view) {
        if (event.isPopupTrigger()) {
            onPopupTriggered(event, view);
        }
    }

    @Override
    public boolean isSelectable() {
        return true;
    }

    @Override
    public void updateNode(DrawingView view) {
        Figure f = getOwner();
        Transform t = FXTransforms.concat(view.getWorldToView(), f.getLocalToWorld());
        final BezierPath path = owner.get(pathKey);
        if (path == null) {
            node.setVisible(false);
            return;
        }
        BezierNode bnode = getBezierNode();
        if (bnode == null
                || (bnode.getMask() & controlPointMask) != controlPointMask) {
            node.setVisible(false);
            return;
        } else {
            node.setVisible(true);
        }
        Point2D cp = getLocation();
        pickLocation = cp = FXTransforms.transform(t, cp);
        double size = node.getWidth();
        node.relocate(cp.getX() - size * 0.5, cp.getY() - size * 0.5);
        // rotates the node:
        node.setRotate(f.getStyledNonNull(ROTATE));
        node.setRotationAxis(f.getStyled(ROTATION_AXIS));

        BezierNode bn = getBezierNode();
        if (bn == null) {
            return;
        }
        if (bn.isCollinear()) {
            if (bn.isEquidistant()) {
                node.setShape(REGION_SHAPE_SMOOTH);
            } else {
                node.setShape(REGION_SHAPE_COLINEAR);
            }
        } else if (bn.isEquidistant()) {
            node.setShape(REGION_SHAPE_EQUIDISTANT);
        } else {
            node.setShape(REGION_SHAPE_CUSP);
        }

        node.setVisible(bn.hasMaskBits(controlPointMask));

    }

}
