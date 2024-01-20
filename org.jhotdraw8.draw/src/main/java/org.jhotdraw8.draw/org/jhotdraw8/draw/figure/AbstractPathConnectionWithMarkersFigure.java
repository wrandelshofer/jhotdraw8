/*
 * @(#)AbstractStraightLineConnectionWithMarkersFigure.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.figure;

import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.draw.connector.Connector;
import org.jhotdraw8.draw.css.value.CssPoint2D;
import org.jhotdraw8.draw.handle.BezierControlPointEditHandle;
import org.jhotdraw8.draw.handle.BezierNodeEditHandle;
import org.jhotdraw8.draw.handle.BezierNodeNonMovableEditHandle;
import org.jhotdraw8.draw.handle.BezierNodeTangentHandle;
import org.jhotdraw8.draw.handle.BezierPathEditHandle;
import org.jhotdraw8.draw.handle.Handle;
import org.jhotdraw8.draw.handle.HandleType;
import org.jhotdraw8.draw.handle.LineConnectorHandle;
import org.jhotdraw8.draw.handle.LineOutlineHandle;
import org.jhotdraw8.draw.handle.MoveHandle;
import org.jhotdraw8.draw.handle.PathIterableOutlineHandle;
import org.jhotdraw8.draw.handle.SelectionHandle;
import org.jhotdraw8.draw.key.BezierPathStyleableKey;
import org.jhotdraw8.draw.locator.PointLocator;
import org.jhotdraw8.draw.render.RenderContext;
import org.jhotdraw8.geom.FXPathElementsBuilder;
import org.jhotdraw8.geom.FXPreciseRotate;
import org.jhotdraw8.geom.FXShapes;
import org.jhotdraw8.geom.PathMetrics;
import org.jhotdraw8.geom.PointAndDerivative;
import org.jhotdraw8.geom.SimplePathMetrics;
import org.jhotdraw8.geom.SvgPaths;
import org.jhotdraw8.geom.intersect.IntersectionPointEx;
import org.jhotdraw8.geom.shape.BezierNode;
import org.jhotdraw8.geom.shape.BezierPath;

import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static org.jhotdraw8.draw.figure.FillRulableFigure.FILL_RULE;

/**
 * AbstractPathConnectionWithMarkersFigure draws a path from start to end.
 * <p>
 * A subclass can hardcode the markers, or can implement one or multiple "marker-able" interfaces
 * that allow user-definable markers: {@link MarkerStartableFigure}, {@link MarkerEndableFigure},
 * {@link MarkerSegmentableFigure}.
 *
 * @author Werner Randelshofer
 */
public abstract class AbstractPathConnectionWithMarkersFigure extends AbstractLineConnectionFigure
        implements PathIterableFigure {

    public static final @NonNull BezierPathStyleableKey PATH = new BezierPathStyleableKey("path");



    public AbstractPathConnectionWithMarkersFigure() {
        this(0, 0, 1, 1);
    }

    public AbstractPathConnectionWithMarkersFigure(@NonNull Point2D start, @NonNull Point2D end) {
        this(start.getX(), start.getY(), end.getX(), end.getY());
    }

    public AbstractPathConnectionWithMarkersFigure(double startX, double startY, double endX, double endY) {
        super(startX, startY, endX, endY);
    }

    @Override
    public void createHandles(@NonNull HandleType handleType, @NonNull List<Handle> list) {
        if (handleType == HandleType.SELECT) {
            list.add(new PathIterableOutlineHandle(this, true));
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
            list.add(new BezierPathEditHandle(this, PATH));
            list.add(new LineConnectorHandle(this, START, START_CONNECTOR, START_TARGET));
            list.add(new LineConnectorHandle(this, END, END_CONNECTOR, END_TARGET));

            BezierPath nodes = get(PATH);
            for (int i = 0, n = nodes.size(); i < n; i++) {
                list.add(new BezierNodeTangentHandle(this, PATH, i));
                if (i == 0 || i == n - 1) {
                    list.add(new BezierNodeNonMovableEditHandle(this, PATH, i));
                } else {
                    list.add(new BezierNodeEditHandle(this, PATH, i));
                }
                if (nodes.get(i).hasIn()) {
                    list.add(new BezierControlPointEditHandle(this, PATH, i, BezierNode.IN_MASK));
                }
                if (nodes.get(i).hasOut()) {
                    list.add(new BezierControlPointEditHandle(this, PATH, i, BezierNode.OUT_MASK));
                }
            }
        } else if (handleType == HandleType.TRANSFORM) {
            list.add(new LineOutlineHandle(this));
        } else {
            super.createHandles(handleType, list);
        }
    }

    @Override
    public @NonNull Node createNode(@NonNull RenderContext drawingView) {
        javafx.scene.Group g = new javafx.scene.Group();
        final Path line = new Path();
        final Path startMarker = new Path();
        final Path endMarker = new Path();
        startMarker.setStroke(null);
        endMarker.setStroke(null);
        g.getChildren().addAll(line, startMarker, endMarker);
        return g;
    }

    public abstract double getMarkerCenterScaleFactor();

    public abstract @Nullable String getMarkerCenterShape();

    public abstract double getMarkerEndScaleFactor();

    public abstract @Nullable String getMarkerEndShape();

    public abstract double getMarkerStartScaleFactor();

    public abstract @Nullable String getMarkerStartShape();

    @Override
    public @NonNull PathIterator getPathIterator(@NonNull RenderContext ctx, AffineTransform tx) {
        BezierPath path = get(PATH);
        if (path == null || path.isEmpty()) {
            Point2D start = getNonNull(START).getConvertedValue();
            Point2D end = getNonNull(END).getConvertedValue();
            return new Line2D.Double(start.getX(), start.getY(), end.getX(), end.getY()).getPathIterator(tx);
        }
        return path.getPathIterator(tx);
    }

    public abstract double getStrokeCutEnd(RenderContext ctx);

    public abstract double getStrokeCutStart(RenderContext ctx);

    @Override
    public void layout(@NonNull RenderContext ctx) {
        Point2D start = getNonNull(START).getConvertedValue();
        Point2D end = getNonNull(END).getConvertedValue();
        Connector startConnector = get(START_CONNECTOR);
        Connector endConnector = get(END_CONNECTOR);
        Figure startTarget = get(START_TARGET);
        Figure endTarget = get(END_TARGET);
        BezierPath path = getNonNull(PATH);

        // Find initial start and end points
        if (startConnector != null && startTarget != null) {
            start = startConnector.getPointAndDerivativeInWorld(this, startTarget).getPoint(Point2D::new);
        }
        if (endConnector != null && endTarget != null) {
            end = endConnector.getPointAndDerivativeInWorld(this, endTarget).getPoint(Point2D::new);
        }

        // Chop start and end points
        if (startConnector != null && startTarget != null) {
            IntersectionPointEx chp;
            if (path.size() > 2) {
                PointAndDerivative pd = path.evalFirst();
                chp = startConnector.chopStart(ctx, this, startTarget, start.getX(), start.getY(), pd.x() + pd.dx(), pd.y() + pd.dy());
            } else {
                chp = startConnector.chopStart(ctx, this, startTarget, start, end);
            }
            start = worldToParent(chp.getX(), chp.getY());
            set(START, new CssPoint2D(start));
        }
        if (endConnector != null && endTarget != null) {
            IntersectionPointEx chp;
            if (path.size() > 2) {
                PointAndDerivative pd = path.evalLastInReverse();
                chp = endConnector.chopStart(ctx, this, endTarget,
                        end.getX(), end.getY(),
                        pd.x() + pd.dx(), pd.y() + pd.dy()
                );
            } else {
                chp = endConnector.chopStart(ctx, this, endTarget, end, start);
            }
            end = worldToParent(chp.getX(), chp.getY());
            set(END, new CssPoint2D(end));
        }

        // Update start and end positions of the path
        if (path.size() < 2) {
            path = path.add(new BezierNode(start)).add(new BezierNode(end));
        }
            if (start != null) {
                BezierNode first = path.getFirst();
                path = path.set(0,
                        first.transform(Transform.translate(start.getX() - first.pointX(), start.getY() - first.pointY())));
            }
            if (end != null) {
                BezierNode last = path.getLast();
                path = path.set(path.size() - 1,
                        last.transform(Transform.translate(end.getX() - last.pointX(), end.getY() - last.pointY())));
            }

        // store the path and compute path metrics
        set(PATH, path);
    }

    @Override
    public void transformInLocal(@NonNull Transform tx) {
        set(START, new CssPoint2D(tx.transform(getNonNull(START).getConvertedValue())));
        set(END, new CssPoint2D(tx.transform(getNonNull(END).getConvertedValue())));
        BezierPath path = get(PATH);
        if (path != null) {
            for (int i = 0, n = path.size(); i < n; i++) {
                var node = path.get(i);
                path = path.set(i, node.transform(tx));
            }
            set(PATH, path);
        }
    }
    @Override
    public void translateInLocal(@NonNull CssPoint2D t) {
        set(START, getNonNull(START).add(t));
        set(END, getNonNull(END).add(t));
        BezierPath path = get(PATH);
        if (path != null) {
            Point2D tc = t.getConvertedValue();
            Translate tx = new Translate(tc.getX(), tc.getY());
            for (int i = 0, n = path.size(); i < n; i++) {
                var node = path.get(i);
                path = path.set(i, node.transform(tx));
            }
            set(PATH, path);
        }
    }

    /**
     * This method can be overridden by a subclass to apply styles to the marker
     * node.
     * <p>
     * The implementation of this method in this class is empty.
     * So no call to super is necessary.
     *
     * @param ctx  the context
     * @param node the node
     */
    protected void updateEndMarkerNode(@NonNull RenderContext ctx, @NonNull Path node) {
        // empty
    }

    /**
     * This method can be overridden by a subclass to apply styles to the line
     * node.
     *
     * @param ctx  the context
     * @param node the node
     */
    protected void updateLineNode(@NonNull RenderContext ctx, @NonNull Path node) {

    }

    protected void updateMarkerNode(RenderContext ctx, javafx.scene.Group group,
                                    @NonNull Path markerNode,
                                    @NonNull PointAndDerivative pd, @Nullable String svgString, double markerScaleFactor) {
        if (svgString != null) {
            try {
                // Note: we must not add individual elements to the ObservableList
                // of the markerNode, because this fires too many change events.
                List<PathElement> nodes = new ArrayList<>();
                FXPathElementsBuilder builder = new FXPathElementsBuilder(nodes);
                SvgPaths.buildFromSvgString(builder, svgString);
                builder.build();
                if (!nodes.equals(markerNode.getElements())) {
                    markerNode.getElements().setAll(nodes);
                }
            } catch (ParseException e) {
                Logger.getLogger(AbstractPathConnectionWithMarkersFigure.class.getName()).warning("Illegal path: " + svgString);
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
    public void updateNode(@NonNull RenderContext ctx, @NonNull Node node) {
        PathMetrics pathMetrics = getPathMetrics();
        if (pathMetrics == null) {
            node.setVisible(false);
            return;
        }


        javafx.scene.Group g = (javafx.scene.Group) node;
        Path lineNode = (Path) g.getChildren().get(0);
        final Path startMarkerNode = (Path) g.getChildren().get(1);
        final Path endMarkerNode = (Path) g.getChildren().get(2);

        final String startMarkerStr = getMarkerStartShape();
        final String endMarkerStr = getMarkerEndShape();


        // Cut stroke at start and at end
        double strokeCutStart = getStrokeCutStart(ctx);
        double strokeCutEnd = getStrokeCutEnd(ctx);
        lineNode.setFillRule(getStyledNonNull(FILL_RULE));
        final List<PathElement> elements =
                FXShapes.fxPathElementsFromAwt(
                        pathMetrics.getSubPathIteratorAtArcLength(strokeCutStart, pathMetrics.arcLength() - strokeCutEnd, null));
        if (!lineNode.getElements().equals(elements)) {
            lineNode.getElements().setAll(elements);
        }

        updateLineNode(ctx, lineNode);
        updateMarkerNode(ctx, g, startMarkerNode,
                pathMetrics.eval(0),
                startMarkerStr, getMarkerStartScaleFactor());
        updateMarkerNode(ctx, g, endMarkerNode,
                pathMetrics.eval(1).reverse(),
                endMarkerStr, getMarkerEndScaleFactor());
        updateStartMarkerNode(ctx, startMarkerNode);
        updateEndMarkerNode(ctx, endMarkerNode);
    }

    public @NonNull PathMetrics getPathMetrics() {
        BezierPath path = get(PATH);
        if (path == null || path.isEmpty()) {
            Point2D start = getNonNull(START).getConvertedValue();
            Point2D end = getNonNull(END).getConvertedValue();
            return new SimplePathMetrics(new Line2D.Double(start.getX(), start.getY(), end.getX(), end.getY()));
        }
        return path.getPathMetrics();
    }

    /**
     * This method can be overridden by a subclass to apply styles to the marker
     * node.
     * <p>
     * The implementation of this method in this class is empty.
     * So no call to super is necessary.
     *
     * @param ctx  the context
     * @param node the node
     */
    protected void updateStartMarkerNode(@NonNull RenderContext ctx, @NonNull Path node) {
        // empty
    }
}
