/*
 * @(#)AbstractStraightLineConnectionWithMarkersFigure.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.figure;

import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.shape.Line;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.draw.connector.Connector;
import org.jhotdraw8.draw.css.value.CssPoint2D;
import org.jhotdraw8.draw.handle.*;
import org.jhotdraw8.draw.key.BezierNodeListStyleableKey;
import org.jhotdraw8.draw.locator.PointLocator;
import org.jhotdraw8.draw.render.RenderContext;
import org.jhotdraw8.geom.*;
import org.jhotdraw8.geom.shape.BezierNode;
import org.jhotdraw8.geom.shape.BezierNodePath;
import org.jhotdraw8.icollection.VectorList;
import org.jhotdraw8.icollection.immutable.ImmutableList;

import java.awt.geom.AffineTransform;
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

    public static final BezierNodeListStyleableKey PATH = new BezierNodeListStyleableKey("path", VectorList.of());

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
            list.add(new LineOutlineHandle(this));
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
            list.add(new LineOutlineHandle(this));
            list.add(new LineConnectorHandle(this, START, START_CONNECTOR, START_TARGET));
            list.add(new LineConnectorHandle(this, END, END_CONNECTOR, END_TARGET));
        } else if (handleType == HandleType.POINT) {
            list.add(new BezierPathEditHandle(this, PATH));
            list.add(new LineConnectorHandle(this, START, START_CONNECTOR, START_TARGET));
            list.add(new LineConnectorHandle(this, END, END_CONNECTOR, END_TARGET));

            ImmutableList<BezierNode> nodes = get(PATH);
            for (int i = 0, n = nodes.size(); i < n; i++) {
                list.add(new BezierNodeTangentHandle(this, PATH, i));
                if (i > 0 && i < n - 1) {
                    list.add(new BezierNodeEditHandle(this, PATH, i));
                }
                if (nodes.get(i).isC1()) {
                    list.add(new BezierControlPointEditHandle(this, PATH, i, BezierNode.C1_MASK));
                }
                if (nodes.get(i).isC2()) {
                    list.add(new BezierControlPointEditHandle(this, PATH, i, BezierNode.C2_MASK));
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
        ImmutableList<BezierNode> path = get(PATH);
        if (path == null || path.isEmpty()) {
            // FIXME include markers in path
            return FXShapes.awtShapeFromFX(new Line(
                    getNonNull(START_X).getConvertedValue(),
                    getNonNull(START_Y).getConvertedValue(),
                    getNonNull(END_X).getConvertedValue(),
                    getNonNull(END_Y).getConvertedValue())).getPathIterator(tx);
        }
        return new BezierNodePath(path).getPathIterator(tx);
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
        if (startConnector != null && startTarget != null) {
            start = startConnector.getPointAndDerivativeInWorld(this, startTarget).getPoint(Point2D::new);
        }
        if (endConnector != null && endTarget != null) {
            end = endConnector.getPointAndDerivativeInWorld(this, endTarget).getPoint(Point2D::new);
        }

        Point2D startPoint = null;
        Point2D endPoint = null;
        if (startConnector != null && startTarget != null) {
            java.awt.geom.Point2D.Double chp = startConnector.chopStart(ctx, this, startTarget, start, end);
            startPoint = worldToParent(chp.getX(), chp.getY());
            set(START, new CssPoint2D(startPoint));
        }
        if (endConnector != null && endTarget != null) {
            java.awt.geom.Point2D.Double chp = endConnector.chopEnd(ctx, this, endTarget, start, end);
            endPoint = worldToParent(chp.getX(), chp.getY());
            set(END, new CssPoint2D(endPoint));
        }

        // If we have a path, set its end positions to START and END.
        ImmutableList<BezierNode> path = get(PATH);
        if (path == null || path.isEmpty()) {
            path = path.add(new BezierNode(start.getX(), start.getY()).setMask(BezierNode.MOVE_MASK));
            path = path.add(new BezierNode(end.getX(), end.getY()));
        } else {
            if (startPoint != null) {
                path = path.set(0, path.getFirst().setX0(startPoint.getX()).setY0(startPoint.getY()));
            }
            if (endPoint != null) {
                path = path.set(path.size() - 1, path.getLast().setX0(endPoint.getX()).setY0(endPoint.getY()));
            }
        }
        set(PATH, path);
    }

    @Override
    public void translateInLocal(@NonNull CssPoint2D t) {
        set(START, getNonNull(START).add(t));
        set(END, getNonNull(END).add(t));
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
    protected void updateEndMarkerNode(RenderContext ctx, Path node) {
        // empty
    }

    /**
     * This method can be overridden by a subclass to apply styles to the line
     * node.
     *
     * @param ctx  the context
     * @param node the node
     */
    protected void updateLineNode(RenderContext ctx, Path node) {

    }

    protected void updateMarkerNode(RenderContext ctx, javafx.scene.Group group,
                                    @NonNull Path markerNode,
                                    @NonNull Point2D start, @NonNull Point2D end, @Nullable String svgString, double markerScaleFactor) {
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
            double angle = Angles.atan2(start.getY() - end.getY(), start.getX() - end.getX());
            markerNode.getTransforms().setAll(
                    new FXPreciseRotate(angle * 180 / Math.PI, start.getX(), start.getY()),
                    new Scale(markerScaleFactor, markerScaleFactor, start.getX(), start.getY()),
                    new Translate(start.getX(), start.getY()));
            markerNode.setVisible(true);
        } else {
            markerNode.setVisible(false);
        }
    }

    @Override
    public void updateNode(@NonNull RenderContext ctx, @NonNull Node node) {
        Point2D start = getNonNull(START).getConvertedValue();
        Point2D end = getNonNull(END).getConvertedValue();
        ImmutableList<BezierNode> path = get(PATH);
        if (path == null || path.isEmpty()) {
            path = VectorList.of(
                    new BezierNode(start.getX(), start.getY()).setMask(BezierNode.MOVE_MASK),
                    new BezierNode(end.getX(), end.getY()).setMask(BezierNode.C0_MASK)
            );
        }


        javafx.scene.Group g = (javafx.scene.Group) node;
        Path lineNode = (Path) g.getChildren().get(0);
        final Path startMarkerNode = (Path) g.getChildren().get(1);
        final Path endMarkerNode = (Path) g.getChildren().get(2);

        final double startInset = getStrokeCutStart(ctx);
        final double endInset = getStrokeCutEnd(ctx);
        final String startMarkerStr = getMarkerStartShape();
        final String endMarkerStr = getMarkerEndShape();

        Point2D dir = end.subtract(start).normalize();
        if (startInset != 0) {
            start = start.add(dir.multiply(startInset));
        }
        if (endInset != 0) {
            end = end.add(dir.multiply(-endInset));
        }

        var bezierNodePath = new BezierNodePath(path,
                getStyledNonNull(FILL_RULE));
        final List<PathElement> elements =
                FXShapes.fxPathElementsFromAwt(
                        bezierNodePath.getPathIterator(null));

        if (!lineNode.getElements().equals(elements)) {
            lineNode.getElements().setAll(elements);
        }

        Point2D secondFirstPoint = end;
        Point2D secondLastPoint = start;

        updateLineNode(ctx, lineNode);
        updateMarkerNode(ctx, g, startMarkerNode, start, secondFirstPoint, startMarkerStr, getMarkerStartScaleFactor());
        updateMarkerNode(ctx, g, endMarkerNode, end, secondLastPoint, endMarkerStr, getMarkerEndScaleFactor());
        updateStartMarkerNode(ctx, startMarkerNode);
        updateEndMarkerNode(ctx, endMarkerNode);
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
    protected void updateStartMarkerNode(RenderContext ctx, Path node) {
        // empty
    }
}
