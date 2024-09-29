/*
 * @(#)AbstractElbowLineConnectionWithMarkersFigure.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.figure;

import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.Polyline;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.css.value.UnitConverter;
import org.jhotdraw8.draw.connector.Connector;
import org.jhotdraw8.draw.css.value.CssPoint2D;
import org.jhotdraw8.draw.handle.Handle;
import org.jhotdraw8.draw.handle.HandleType;
import org.jhotdraw8.draw.handle.LineConnectorHandle;
import org.jhotdraw8.draw.handle.LineOutlineHandle;
import org.jhotdraw8.draw.handle.MoveHandle;
import org.jhotdraw8.draw.handle.PathIterableOutlineHandle;
import org.jhotdraw8.draw.handle.SelectionHandle;
import org.jhotdraw8.draw.locator.PointLocator;
import org.jhotdraw8.draw.render.RenderContext;
import org.jhotdraw8.geom.AwtShapes;
import org.jhotdraw8.geom.FXGeom;
import org.jhotdraw8.geom.FXPreciseRotate;
import org.jhotdraw8.geom.PointAndDerivative;
import org.jhotdraw8.geom.intersect.IntersectRayRay;
import org.jhotdraw8.geom.intersect.IntersectionPointEx;
import org.jhotdraw8.icollection.immutable.ImmutableList;
import org.jspecify.annotations.Nullable;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.util.List;

import static org.jhotdraw8.draw.render.RenderContext.UNIT_CONVERTER_KEY;
import static org.jhotdraw8.geom.Scalars.almostEqual;

/**
 * AbstractElbowLineConnectionWithMarkersFigure draws a straight line or an elbow line from start to end.
 * <p>
 * A subclass can hardcode the markers, or can implement one or multiple "markerable" interfaces
 * that allow user-defineable markers: {@link MarkerStartableFigure}, {@link MarkerEndableFigure},
 * {@link MarkerSegmentableFigure}, {@link MarkerMidableFigure}.
 *
 * @author Werner Randelshofer
 */
public abstract class AbstractElbowLineConnectionWithMarkersFigure extends AbstractLineConnectionFigure
        implements PathIterableFigure {

    private final Polyline path = new Polyline();

    public AbstractElbowLineConnectionWithMarkersFigure() {
        this(0, 0, 1, 1);
    }

    public AbstractElbowLineConnectionWithMarkersFigure(Point2D start, Point2D end) {
        this(start.getX(), start.getY(), end.getX(), end.getY());
    }

    public AbstractElbowLineConnectionWithMarkersFigure(double startX, double startY, double endX, double endY) {
        super(startX, startY, endX, endY);
    }

    @Override
    public Node createNode(RenderContext drawingView) {
        Group g = new Group();
        final Polyline line = new Polyline();
        final Path startMarker = new Path();
        final Path endMarker = new Path();
        g.getChildren().addAll(line, startMarker, endMarker);
        return g;
    }

    @Override
    public void createHandles(HandleType handleType, List<Handle> list) {
        if (handleType == HandleType.SELECT) {
            list.add(new LineOutlineHandle(this));
        } else if (handleType == HandleType.MOVE) {
            list.add(new PathIterableOutlineHandle(this, true));
            if (get(START_CONNECTOR) == null) {
                list.add(new MoveHandle(this, new PointLocator(START)));
            } else {
                list.add(new SelectionHandle(this, new PointLocator(START)));
            }
            if (get(END_CONNECTOR) == null) {
                list.add(new MoveHandle(this, new PointLocator(END)));
            } else {
                list.add(new SelectionHandle(this, new PointLocator(END)));
            }
        } else if (handleType == HandleType.RESIZE) {
            list.add(new PathIterableOutlineHandle(this, true));
            list.add(new LineConnectorHandle(this, START, START_CONNECTOR, START_TARGET));
            list.add(new LineConnectorHandle(this, END, END_CONNECTOR, END_TARGET));
        } else if (handleType == HandleType.POINT) {
            list.add(new PathIterableOutlineHandle(this, true));
            list.add(new LineConnectorHandle(this, START, START_CONNECTOR, START_TARGET));
            list.add(new LineConnectorHandle(this, END, END_CONNECTOR, END_TARGET));
        } else if (handleType == HandleType.TRANSFORM) {
            list.add(new LineOutlineHandle(this));
        } else {
            super.createHandles(handleType, list);
        }
    }

    /**
     * This method can be overridden by a subclass to apply styles to the line
     * node.
     *
     * @param ctx  the context
     * @param node the node
     */
    protected void updateLineNode(RenderContext ctx, Polyline node) {
        // empty
    }

    /**
     * This method can be overridden by a subclass to apply styles to the marker
     * node.
     *
     * @param ctx  the context
     * @param node the node
     */
    protected void updateStartMarkerNode(RenderContext ctx, Path node) {
        // empty
    }

    /**
     * This method can be overridden by a subclass to apply styles to the marker
     * node.
     *
     * @param ctx  the context
     * @param node the node
     */
    protected void updateEndMarkerNode(RenderContext ctx, Path node) {
        // empty
    }

    @Override
    public void updateNode(RenderContext ctx, Node node) {
        try {
            Group g = (Group) node;
            Polyline lineNode = (Polyline) g.getChildren().get(0);
            final Path startMarkerNode = (Path) g.getChildren().get(1);
            final Path endMarkerNode = (Path) g.getChildren().get(2);

            Point2D start = getNonNull(START).getConvertedValue();
            Point2D end = getNonNull(END).getConvertedValue();

            final double startInset = getStrokeCutStart(ctx);
            final double endInset = getStrokeCutEnd(ctx);
            final ImmutableList<PathElement> startMarkerShape = getMarkerStartShape();

            ObservableList<Double> points = lineNode.getPoints();

            points.setAll(path.getPoints());
            int size = points.size();
            Point2D p0, p1, p3, p2;
            if (size > 4) {
                p0 = new Point2D(points.get(0), points.get(1));
                p1 = new Point2D(points.get(2), points.get(3));
                p3 = new Point2D(points.get(size - 2), points.get(size - 1));
                p2 = new Point2D(points.get(size - 4), points.get(size - 3));
            } else if (size == 4) {
                p2 = p0 = new Point2D(points.get(0), points.get(1));
                p3 = p1 = new Point2D(points.get(2), points.get(3));
            } else {
                p2 = p0 = p1 = p3 = new Point2D(0, 0);
            }
            updateMarkerNode(ctx, g, startMarkerNode,
                    new PointAndDerivative(p0.getX(), p0.getY(), p1.getX() - p0.getX(), p1.getY() - p0.getY()),
                    startMarkerShape, getMarkerStartScaleFactor());
            final ImmutableList<PathElement> endMarkerShape = getMarkerEndShape();
            updateMarkerNode(ctx, g, endMarkerNode,
                    new PointAndDerivative(p3.getX(), p3.getY(), p2.getX() - p3.getX(), p2.getY() - p3.getY()),
                    endMarkerShape, getMarkerEndScaleFactor());

            updateLineNode(ctx, lineNode);
            updateStartMarkerNode(ctx, startMarkerNode);
            updateEndMarkerNode(ctx, endMarkerNode);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    protected void updateMarkerNode(RenderContext ctx, Group group,
                                    Path markerNode,
                                    PointAndDerivative pd, @Nullable ImmutableList<PathElement> shapeElements, double markerScaleFactor) {
        if (shapeElements != null) {
            markerNode.getElements().setAll(shapeElements.toMutable());
            double angle = Math.PI + pd.getAngle();
            double pdx = pd.x();
            double pdy = pd.y();
            markerNode.getTransforms().setAll(
                    new FXPreciseRotate(angle * 180 / Math.PI, pdx, pdy),
                    new Scale(markerScaleFactor, markerScaleFactor, pdx, pdy),
                    new Translate(pdx, pdy));
            markerNode.setVisible(true);
        } else {
            markerNode.setVisible(false);
        }
    }

    @Override
    public PathIterator getPathIterator(RenderContext ctx, @Nullable AffineTransform tx) {
        return path == null ? AwtShapes.emptyPathIterator() : AwtShapes.pointCoordsToPathIterator(path.getPoints(), false, PathIterator.WIND_NON_ZERO, tx);
    }

    public abstract double getStrokeCutStart(RenderContext ctx);

    public abstract double getStrokeCutEnd(RenderContext ctx);

    public abstract @Nullable ImmutableList<PathElement> getMarkerStartShape();

    public abstract double getMarkerStartScaleFactor();

    public abstract @Nullable ImmutableList<PathElement> getMarkerEndShape();

    public abstract double getMarkerEndScaleFactor();

    /**
     * The offset of the elbow from the end of the line.
     * <p>
     * If the value is null, or less or equal 0, then a straight line is drawn instead of an elbow.
     *
     * @return an offset
     */
    public abstract @Nullable CssSize getElbowOffset();


    @Override
    public void layout(RenderContext ctx) {
        // Get start and end points
        Point2D start = getNonNull(START).getConvertedValue();
        Point2D end = getNonNull(END).getConvertedValue();

        // If the figure is connected at its start and/or end, move the start and/or end points
        Connector startConnector = get(START_CONNECTOR);
        Connector endConnector = get(END_CONNECTOR);
        Figure startTarget = get(START_TARGET);
        Figure endTarget = get(END_TARGET);
        Point2D startDerivative = null;
        Point2D endDerivative = null;
        Point2D initialStart = start;
        Point2D initalEnd = end;
        if (startConnector != null && startTarget != null) {
            initialStart = startConnector.getPointAndDerivativeInWorld(this, startTarget).getPoint(Point2D::new);
        }
        if (endConnector != null && endTarget != null) {
            initalEnd = endConnector.getPointAndDerivativeInWorld(this, endTarget).getPoint(Point2D::new);
        }
        if (startConnector != null && startTarget != null) {
            IntersectionPointEx intersectionPointEx = startConnector.chopStart(ctx, this, startTarget, initialStart, initalEnd);
            startDerivative = new Point2D(intersectionPointEx.getDerivativeB().getX(), intersectionPointEx.getDerivativeB().getY());
            start = worldToParent(intersectionPointEx.getX(), intersectionPointEx.getY());
            set(START, new CssPoint2D(start));
        }
        if (endConnector != null && endTarget != null) {
            IntersectionPointEx intersectionPointEx = endConnector.chopStart(ctx, this, endTarget, initalEnd, initialStart);
            endDerivative = new Point2D(intersectionPointEx.getDerivativeB().getX(), intersectionPointEx.getDerivativeB().getY());
            end = worldToParent(intersectionPointEx.getX(), intersectionPointEx.getY());
            set(END, new CssPoint2D(end));
        }
        Point2D lineDerivative = end.subtract(start);
        if (startDerivative == null) {
            startDerivative = lineDerivative;
        }
        if (endDerivative == null) {
            endDerivative = lineDerivative.multiply(-1);
        }

        // Layout the elbow line
        ObservableList<Double> points = path.getPoints();
        points.clear();
        points.add(start.getX());
        points.add(start.getY());
        if (start.getX() == end.getX() || start.getY() == end.getY()) {
            // case 1: The line is horizontal or vertical: we draw a straight line
            // nothing to do
        } else {
            // Compute perpendicular to boundary of start and end target shape
            Point2D startPerp = FXGeom.perp(startDerivative).normalize();
            Point2D endPerp = FXGeom.perp(endDerivative).normalize();
            // Flip perpendiculars if necessary, so that each points towards the other shape
            Point2D lineDerivativeNormalized = lineDerivative.normalize();
            if (!FXGeom.isSameDirection(startPerp, lineDerivativeNormalized)) {
                startPerp = new Point2D(-startPerp.getX(), -startPerp.getY());
            }
            if (FXGeom.isSameDirection(lineDerivativeNormalized, endPerp)) {
                endPerp = new Point2D(-endPerp.getX(), -endPerp.getY());
            }
            // Round perpendiculars to 90° angles
            startPerp = FXGeom.normalizeTo90Degrees(startPerp);
            endPerp = FXGeom.normalizeTo90Degrees(endPerp);

            CssSize elbowOffsetSize = getElbowOffset();
            double elbowOffset = elbowOffsetSize == null ? 0 : ctx.getNonNull(UNIT_CONVERTER_KEY).convert(elbowOffsetSize, UnitConverter.DEFAULT);

            double cosine = startPerp.dotProduct(endPerp);
            if (almostEqual(cosine, 0)) {
                if (elbowOffset <= 0) {
                    // case 2: the lines meet at a 90 degrees angle: we draw an 'L'-shape
                    var intersection = IntersectRayRay.intersectRayRayEx(
                            start.getX(), start.getY(), startPerp.getX(), startPerp.getY(),
                            end.getX(), end.getY(), endPerp.getX(), endPerp.getY()
                    ).intersections().getFirst();
                    points.add(intersection.getX());
                    points.add(intersection.getY());
                } else {
                    // case 3: the lines miss their meeting point, we draw a '|_|⎺'-shape
                    Point2D p1 = start.add(startPerp.multiply(elbowOffset));
                    double distance2 = -endPerp.dotProduct(end.subtract(p1));
                    double actualOffset2 = distance2 * 0.5;
                    Point2D p2 = p1.add(endPerp.multiply(-actualOffset2));
                    Point2D p3 = end.add(endPerp.multiply(distance2 - actualOffset2));
                    points.add(p1.getX());
                    points.add(p1.getY());
                    points.add(p2.getX());
                    points.add(p2.getY());
                    points.add(p3.getX());
                    points.add(p3.getY());
                }
            } else {
                double distance = startPerp.dotProduct(lineDerivative);
                double actualOffset = (elbowOffset <= 0) ? distance * 0.5 : elbowOffset;
                if (actualOffset <= distance) {
                    // case 4: we draw a Z-shape
                    Point2D p1 = start.add(startPerp.multiply(actualOffset));
                    Point2D p2 = end.add(endPerp.multiply(distance - actualOffset));
                    points.add(p1.getX());
                    points.add(p1.getY());
                    points.add(p2.getX());
                    points.add(p2.getY());

                } else {
                    // case 4: we draw a Z-shape
                    Point2D p1 = start.add(startPerp.multiply(actualOffset));
                    points.add(p1.getX());
                    points.add(p1.getY());
                    double distance2 = endPerp.dotProduct(end.subtract(p1));
                    double actualOffset2 = distance2 * 0.5;
                    Point2D p3 = end.add(endPerp.multiply(distance2 - actualOffset2));
                    connectWithElbowLine(p1, p3, FXGeom.perp(startPerp), points);
                    points.add(p3.getX());
                    points.add(p3.getY());
                }
            }
        }

        points.add(end.getX());
        points.add(end.getY());

    }

    /**
     * Connects two points with a z-shaped elbow line.
     *
     * @param a      the first point
     * @param b      the second point
     * @param dir    the direction of the z-shape
     * @param points
     */
    private void connectWithElbowLine(Point2D a, Point2D b, Point2D dir, ObservableList<Double> points) {
        Point2D derivative = b.subtract(a);
        double distance = dir.dotProduct(derivative);
        double offsetDistance = distance * 0.5;
        points.add(a.getX() + dir.getX() * offsetDistance);
        points.add(a.getY() + dir.getY() * offsetDistance);
        points.add(b.getX() - dir.getX() * (distance - offsetDistance));
        points.add(b.getY() - dir.getY() * (distance - offsetDistance));
    }
}
