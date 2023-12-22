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
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.draw.DrawLabels;
import org.jhotdraw8.draw.DrawingView;
import org.jhotdraw8.draw.css.value.CssColor;
import org.jhotdraw8.draw.css.value.CssPoint2D;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.fxcollection.typesafekey.MapAccessor;
import org.jhotdraw8.geom.FXGeom;
import org.jhotdraw8.geom.FXTransforms;
import org.jhotdraw8.geom.Points;
import org.jhotdraw8.geom.shape.BezierNode;
import org.jhotdraw8.geom.shape.BezierNodePath;
import org.jhotdraw8.icollection.VectorList;
import org.jhotdraw8.icollection.immutable.ImmutableList;

import static org.jhotdraw8.draw.figure.TransformableFigure.ROTATE;
import static org.jhotdraw8.draw.figure.TransformableFigure.ROTATION_AXIS;

/**
 * Handle for the point of a figure.
 *
 * @author Werner Randelshofer
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
    private final @NonNull Region node;
    private Point2D pickLocation;
    private final int pointIndex;
    private final MapAccessor<ImmutableList<BezierNode>> pointKey;

    public BezierControlPointEditHandle(Figure figure, MapAccessor<ImmutableList<BezierNode>> pointKey, int pointIndex, int controlPointMask) {
        super(figure);
        this.pointKey = pointKey;
        this.pointIndex = pointIndex;
        this.controlPointMask = controlPointMask;
        if (this.controlPointMask != BezierNode.C1_MASK && this.controlPointMask != BezierNode.C2_MASK) {
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

    private BezierNode getBezierNode() {
        ImmutableList<BezierNode> list = owner.get(pointKey);
        return list.get(pointIndex);

    }

    @Override
    public Cursor getCursor() {
        return Cursor.CROSSHAIR;
    }

    private @NonNull Point2D getLocation() {
        return getBezierNode().getC(controlPointMask);

    }

    public Point2D getLocationInView() {
        return pickLocation;
    }

    @Override
    public @NonNull Region getNode(@NonNull DrawingView view) {
        double size = view.getEditor().getHandleSize() * 0.8;
        if (node.getWidth() != size) {
            node.resize(size, size);
        }
        CssColor color = view.getEditor().getHandleColor();
        BorderStroke borderStroke = node.getBorder().getStrokes().get(0);
        if (!borderStroke.getTopStroke().equals(color.getColor())) {
            node.setBorder(new Border(new BorderStroke(color.getColor(), BorderStrokeStyle.SOLID, null, null)));
        }

        return node;
    }

    @Override
    public void onMouseClicked(@NonNull MouseEvent event, @NonNull DrawingView dv) {
        if (pointKey != null) {
            if (event.getClickCount() == 1) {
                if (event.isControlDown() || event.isAltDown()) {
                    ImmutableList<BezierNode> list = owner.get(pointKey);
                    BezierNode bn = list.get(pointIndex);

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
                    dv.getModel().set(owner, pointKey,
                            list.set(pointIndex, newbn));
                }
            }
        }
    }

    @Override
    public void onMouseDragged(@NonNull MouseEvent event, @NonNull DrawingView view) {
        Point2D newPoint = view.viewToWorld(new Point2D(event.getX(), event.getY()));
        final Figure f = getOwner();

        if (!event.isAltDown() && !event.isControlDown()) {
            // alt or control switches the constrainer off
            newPoint = view.getConstrainer().constrainPoint(f, new CssPoint2D(newPoint)).getConvertedValue();
        }

        ImmutableList<BezierNode> list = owner.get(pointKey);
        if (pointIndex >= list.size()) {
            return;
        }
        BezierNode bn = list.get(pointIndex);
        Point2D p = f.worldToLocal(newPoint);

        if (!bn.isCollinear()) {
            if (!bn.isEquidistant()) {
                // move control point independently
                BezierNode newBezierNode = bn.withC(controlPointMask, p);
                view.getModel().set(f, pointKey,
                        list.set(pointIndex, newBezierNode));
            } else {
                // move control point and opposite control point to same distance
                BezierNode newBezierNode = bn.withC(controlPointMask, p);
                Point2D c0 = bn.getC0();
                double r = p.distance(c0);
                if (controlPointMask == BezierNode.C1_MASK) {
                    Point2D p2 = bn.getC2();
                    Point2D dir = p2.subtract(c0).normalize();
                    if (dir.magnitude() == 0) {
                        dir = new Point2D(0, 1);// point down
                    }
                    p2 = c0.add(dir.multiply(r));
                    Point2D constrainedPoint = view.getConstrainer().constrainPoint(owner, new CssPoint2D(p2)).getConvertedValue();
                    if (Points.almostEqual(p2.getX(), p2.getY(), constrainedPoint.getX(), constrainedPoint.getY())) {
                        p2 = constrainedPoint;
                    }
                    newBezierNode = newBezierNode.withC2(p2);
                } else {
                    Point2D p2 = bn.getC1();
                    Point2D dir = p2.subtract(c0).normalize();
                    if (dir.magnitude() == 0) {
                        dir = new Point2D(0, 1);// point down
                    }
                    p2 = c0.add(dir.multiply(r));
                    Point2D constrainedPoint = view.getConstrainer().constrainPoint(owner, new CssPoint2D(p2)).getConvertedValue();
                    if (Points.almostEqual(p2.getX(), p2.getY(), constrainedPoint.getX(), constrainedPoint.getY())) {
                        p2 = constrainedPoint;
                    }
                    newBezierNode = newBezierNode.withC1(p2);
                }

                view.getModel().set(f, pointKey,
                        list.set(pointIndex, newBezierNode));
            }
        } else {
            Point2D c0 = bn.getC0();

            // move control point and opposite control point on same line
            double a = Math.PI + FXGeom.angle(c0, p);
            Point2D p2;
            if (controlPointMask == BezierNode.C1_MASK) {
                p2 = bn.getC2();
            } else {
                p2 = bn.getC1();
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
            if (controlPointMask == BezierNode.C1_MASK) {
                newBezierNode = bn.withC1(p).withC2(p2);
            } else {
                newBezierNode = bn.withC2(p).withC1(p2);
            }
            view.getModel().set(f, pointKey,
                    list.set(pointIndex, newBezierNode));
        }
    }

    @Override
    public void onMousePressed(@NonNull MouseEvent event, @NonNull DrawingView view) {
        if (event.isPopupTrigger()) {
            onPopupTriggered(event, view);
        }
    }

    private void onPopupTriggered(@NonNull MouseEvent event, @NonNull DrawingView view) {
        ContextMenu contextMenu = new ContextMenu();

        Menu constraints = new Menu(DrawLabels.getResources().getString("handle.bezierControlPoint.constraints.text"));
        RadioMenuItem noneRadio = new RadioMenuItem(DrawLabels.getResources().getString("handle.bezierControlPoint.noConstraints.text"));
        RadioMenuItem collinearRadio = new RadioMenuItem(DrawLabels.getResources().getString("handle.bezierControlPoint.collinearConstraint.text"));
        RadioMenuItem equidistantRadio = new RadioMenuItem(DrawLabels.getResources().getString("handle.bezierControlPoint.equidistantConstraint.text"));
        RadioMenuItem bothRadio = new RadioMenuItem(DrawLabels.getResources().getString("handle.bezierControlPoint.collinearAndEquidistantConstraint.text"));

        final ImmutableList<BezierNode> nodes = owner.get(pointKey);
        if (nodes == null) {
            return;
        }
        BezierNodePath path = new BezierNodePath(nodes);
        BezierNode bnode = path.getNodes().get(pointIndex);
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
            path.getNodes().set(pointIndex, changedNode);
            view.getModel().set(owner, pointKey, VectorList.copyOf(path.getNodes()));
            view.recreateHandles();
        });
        collinearRadio.setOnAction(actionEvent -> {
            BezierNode changedNode = bnode.withCollinear(true).withEquidistant(false);
            path.getNodes().set(pointIndex, changedNode);
            view.getModel().set(owner, pointKey, VectorList.copyOf(path.getNodes()));
            view.recreateHandles();
        });
        equidistantRadio.setOnAction(actionEvent -> {
            BezierNode changedNode = bnode.withCollinear(false).withEquidistant(true);
            path.getNodes().set(pointIndex, changedNode);
            view.getModel().set(owner, pointKey, VectorList.copyOf(path.getNodes()));
            view.recreateHandles();
        });
        bothRadio.setOnAction(actionEvent -> {
            BezierNode changedNode = bnode.withCollinear(true).withEquidistant(true);
            path.getNodes().set(pointIndex, changedNode);
            view.getModel().set(owner, pointKey, VectorList.copyOf(path.getNodes()));
            view.recreateHandles();
        });

        constraints.getItems().addAll(noneRadio, collinearRadio, equidistantRadio, bothRadio);
        contextMenu.getItems().add(constraints);
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
        final ImmutableList<BezierNode> nodes = owner.get(pointKey);
        if (nodes == null) {
            node.setVisible(false);
            return;
        }
        ImmutableList<BezierNode> list = f.get(pointKey);
        BezierNodePath path = new BezierNodePath(nodes);
        BezierNode bnode = path.getNodes().get(pointIndex);
        if (list == null || pointIndex >= list.size()
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

        node.setVisible(bn.isC(controlPointMask));

    }

}
