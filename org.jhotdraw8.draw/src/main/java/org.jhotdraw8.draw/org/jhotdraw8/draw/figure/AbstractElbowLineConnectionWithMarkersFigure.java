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
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
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
import org.jhotdraw8.geom.FXPathElementsBuilder;
import org.jhotdraw8.geom.FXPreciseRotate;
import org.jhotdraw8.geom.PointAndDerivative;
import org.jhotdraw8.geom.SvgPaths;
import org.jhotdraw8.geom.intersect.IntersectionPointEx;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static java.lang.Math.abs;
import static java.lang.Math.signum;

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

    public AbstractElbowLineConnectionWithMarkersFigure(@NonNull Point2D start, @NonNull Point2D end) {
        this(start.getX(), start.getY(), end.getX(), end.getY());
    }

    public AbstractElbowLineConnectionWithMarkersFigure(double startX, double startY, double endX, double endY) {
        super(startX, startY, endX, endY);
    }

    @Override
    public @NonNull Node createNode(@NonNull RenderContext drawingView) {
        Group g = new Group();
        final Polyline line = new Polyline();
        final Path startMarker = new Path();
        final Path endMarker = new Path();
        g.getChildren().addAll(line, startMarker, endMarker);
        return g;
    }

    @Override
    public void createHandles(@NonNull HandleType handleType, @NonNull List<Handle> list) {
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
    protected void updateLineNode(@NonNull RenderContext ctx, @NonNull Polyline node) {

    }

    /**
     * This method can be overridden by a subclass to apply styles to the marker
     * node.
     *
     * @param ctx  the context
     * @param node the node
     */
    protected void updateStartMarkerNode(@NonNull RenderContext ctx, @NonNull Path node) {
        // empty
    }

    /**
     * This method can be overridden by a subclass to apply styles to the marker
     * node.
     *
     * @param ctx  the context
     * @param node the node
     */
    protected void updateEndMarkerNode(@NonNull RenderContext ctx, @NonNull Path node) {
        // empty
    }

    @Override
    public void updateNode(@NonNull RenderContext ctx, @NonNull Node node) {
        Group g = (Group) node;
        Polyline lineNode = (Polyline) g.getChildren().get(0);
        final Path startMarkerNode = (Path) g.getChildren().get(1);
        final Path endMarkerNode = (Path) g.getChildren().get(2);

        Point2D start = getNonNull(START).getConvertedValue();
        Point2D end = getNonNull(END).getConvertedValue();

        final double startInset = getStrokeCutStart(ctx);
        final double endInset = getStrokeCutEnd(ctx);
        final String startMarkerStr = getMarkerStartShape();

        ObservableList<Double> points = lineNode.getPoints();

        points.setAll(path.getPoints());
        int size = points.size();
        Point2D p0, p1, p3, p2;
        if (size > 4) {
            p0 = new Point2D(points.get(0), points.get(1));
            p1 = new Point2D(points.get(2), points.get(3));
            p3 = new Point2D(points.get(size - 2), points.get(size - 1));
            p2 = new Point2D(points.get(size - 4), points.get(size - 3));
        } else {
            p2 = p0 = new Point2D(points.get(0), points.get(1));
            p3 = p1 = new Point2D(points.get(2), points.get(3));
        }
        updateMarkerNode(ctx, g, startMarkerNode,
                new PointAndDerivative(p0.getX(), p0.getY(), p1.getX() - p0.getX(), p1.getY() - p0.getY()),
                startMarkerStr, getMarkerStartScaleFactor());
        final String endMarkerStr = getMarkerEndShape();
        updateMarkerNode(ctx, g, endMarkerNode,
                new PointAndDerivative(p3.getX(), p3.getY(), p2.getX() - p3.getX(), p2.getY() - p3.getY()),
                endMarkerStr, getMarkerEndScaleFactor());

        Point2D dir = end.subtract(start).normalize();
        if (startInset != 0) {
            start = start.add(dir.multiply(startInset));
        }
        if (endInset != 0) {
            end = end.add(dir.multiply(-endInset));
        }

        updateLineNode(ctx, lineNode);
        updateStartMarkerNode(ctx, startMarkerNode);
        updateEndMarkerNode(ctx, endMarkerNode);
    }

    protected void updateMarkerNode(@NonNull RenderContext ctx, @NonNull Group group,
                                    @NonNull Path markerNode,
                                    @NonNull PointAndDerivative pd, @Nullable String svgString, double markerScaleFactor) {
        if (svgString != null) {
            try {
                // Note: we must not add individual elements to the ObservableList
                // of the markerNode, because this fires too many change events.
                List<PathElement> nodes = new ArrayList<>();
                FXPathElementsBuilder builder = new FXPathElementsBuilder(nodes);
                SvgPaths.svgStringToBuilder(svgString, builder);
                builder.build();
                markerNode.getElements().setAll(nodes);
            } catch (ParseException e) {
                Logger.getLogger(AbstractElbowLineConnectionWithMarkersFigure.class.getName()).warning("Illegal path: " + svgString);
            }
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
    public @NonNull PathIterator getPathIterator(@NonNull RenderContext ctx, @Nullable AffineTransform tx) {
        return path == null ? AwtShapes.emptyPathIterator() : AwtShapes.pointCoordsToPathIterator(path.getPoints(), false, PathIterator.WIND_NON_ZERO, tx);
    }

    public abstract double getStrokeCutStart(@NonNull RenderContext ctx);

    public abstract double getStrokeCutEnd(@NonNull RenderContext ctx);

    public abstract @Nullable String getMarkerStartShape();

    public abstract double getMarkerStartScaleFactor();

    public abstract @Nullable String getMarkerEndShape();

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
    public void layout(@NonNull RenderContext ctx) {
        Point2D start = getNonNull(START).getConvertedValue();
        Point2D end = getNonNull(END).getConvertedValue();
        Connector startConnector = get(START_CONNECTOR);
        Connector endConnector = get(END_CONNECTOR);
        Figure startTarget = get(START_TARGET);
        Figure endTarget = get(END_TARGET);
        CssSize elbowOffset1 = getElbowOffset();
        double elbowOffset = elbowOffset1 == null ? 0.0 : ctx.getNonNull(RenderContext.UNIT_CONVERTER_KEY).convert(elbowOffset1, UnitConverter.DEFAULT);


        ObservableList<Double> points = path.getPoints();
        points.clear();

        // Find initial start and end points
        if (startConnector != null && startTarget != null) {
            start = startConnector.getPointAndDerivativeInWorld(this, startTarget).getPoint(Point2D::new);
        }
        if (endConnector != null && endTarget != null) {
            end = endConnector.getPointAndDerivativeInWorld(this, endTarget).getPoint(Point2D::new);
        }
        // Chop start and end points
        Point2D endDerivative = null;
        if (startConnector != null && startTarget != null) {
            IntersectionPointEx intersectionPointEx = startConnector.chopStart(ctx, this, startTarget, start, end);
            start = worldToParent(intersectionPointEx.getX(), intersectionPointEx.getY());
            set(START, new CssPoint2D(start));
        }
        if (endConnector != null && endTarget != null) {
            IntersectionPointEx intersectionPointEx = endConnector.chopStart(ctx, this, endTarget, end, start);
            endDerivative = new Point2D(intersectionPointEx.getDerivativeB().getX(), intersectionPointEx.getDerivativeB().getY());
            end = worldToParent(intersectionPointEx.getX(), intersectionPointEx.getY());
            set(END, new CssPoint2D(end));
        }

        CssSize elbowOffsetSize = getElbowOffset();
        if (elbowOffset == 0 || endDerivative == null || FXGeom.squaredMagnitude(endDerivative) < 1e-7) {
            points.addAll(start.getX(), start.getY());
            points.addAll(end.getX(), end.getY());
        } else {
            Point2D endDerivativeNormalized = endDerivative.normalize();
            // Enforce perfect vertical or perfect horizontal line
            if (abs(endDerivativeNormalized.getX()) > abs(endDerivativeNormalized.getY())) {
                endDerivativeNormalized = new Point2D(signum(endDerivativeNormalized.getX()), 0);
            } else {
                endDerivativeNormalized = new Point2D(0, signum(endDerivativeNormalized.getY()));
            }
            Point2D dir = new Point2D(endDerivative.getY(), -endDerivative.getX()).normalize();
            Point2D p1;
            Point2D p2;
            if (UnitConverter.PERCENTAGE.equals(elbowOffsetSize.getUnits())) {
                elbowOffset = elbowOffsetSize.getConvertedValue() * abs(dir.dotProduct(end.subtract(start)));
            }
            p2 = endDerivativeNormalized.multiply(elbowOffset);
            p1 = endDerivativeNormalized.multiply(abs(dir.dotProduct(end.subtract(start))) - elbowOffset);

            points.addAll(start.getX(), start.getY());
            points.addAll(start.getX() - p1.getY(), start.getY() + p1.getX());
            points.addAll(end.getX() + p2.getY(), end.getY() - p2.getX());
            points.addAll(end.getX(), end.getY());
        }
    }
}
