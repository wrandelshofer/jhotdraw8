/*
 * @(#)AbstractStraightLineConnectionWithMarkersFigure.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.figure;

import javafx.geometry.Point2D;
import javafx.scene.Group;
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
import org.jhotdraw8.draw.handle.Handle;
import org.jhotdraw8.draw.handle.HandleType;
import org.jhotdraw8.draw.handle.LineConnectorHandle;
import org.jhotdraw8.draw.handle.LineOutlineHandle;
import org.jhotdraw8.draw.handle.MoveHandle;
import org.jhotdraw8.draw.handle.SelectionHandle;
import org.jhotdraw8.draw.locator.PointLocator;
import org.jhotdraw8.draw.render.RenderContext;
import org.jhotdraw8.geom.FXPathElementsBuilder;
import org.jhotdraw8.geom.FXPreciseRotate;
import org.jhotdraw8.geom.FXShapes;
import org.jhotdraw8.geom.PointAndDerivative;
import org.jhotdraw8.geom.SvgPaths;

import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * AbstractStraightLineConnectionWithMarkersFigure draws a straight line from start to end.
 * <p>
 * A subclass can hardcode the markers, or can implement one or multiple "marker-able" interfaces
 * that allow user-definable markers: {@link MarkerStartableFigure}, {@link MarkerEndableFigure},
 * {@link MarkerSegmentableFigure}.
 *
 * @author Werner Randelshofer
 */
public abstract class AbstractStraightLineConnectionWithMarkersFigure extends AbstractLineConnectionFigure
        implements PathIterableFigure {

    public AbstractStraightLineConnectionWithMarkersFigure() {
        this(0, 0, 1, 1);
    }

    public AbstractStraightLineConnectionWithMarkersFigure(@NonNull Point2D start, @NonNull Point2D end) {
        this(start.getX(), start.getY(), end.getX(), end.getY());
    }

    public AbstractStraightLineConnectionWithMarkersFigure(double startX, double startY, double endX, double endY) {
        super(startX, startY, endX, endY);
    }

    @Override
    public void createHandles(@NonNull HandleType handleType, @NonNull List<Handle> list) {
        if (handleType == HandleType.SELECT) {
            list.add(new LineOutlineHandle(this));
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
            list.add(new LineOutlineHandle(this));
            list.add(new LineConnectorHandle(this, START, START_CONNECTOR, START_TARGET));
            list.add(new LineConnectorHandle(this, END, END_CONNECTOR, END_TARGET));
        } else if (handleType == HandleType.TRANSFORM) {
            list.add(new LineOutlineHandle(this));
        } else {
            super.createHandles(handleType, list);
        }
    }

    @Override
    public @NonNull Node createNode(@NonNull RenderContext drawingView) {
        Group g = new Group();
        final Line line = new Line();
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
    public @NonNull PathIterator getPathIterator(@NonNull RenderContext ctx, @Nullable AffineTransform tx) {
        // FIXME include markers in path
        return FXShapes.awtShapeFromFX(new Line(
                getNonNull(START_X).getConvertedValue(),
                getNonNull(START_Y).getConvertedValue(),
                getNonNull(END_X).getConvertedValue(),
                getNonNull(END_Y).getConvertedValue())).getPathIterator(tx);
    }

    public abstract double getStrokeCutEnd(@NonNull RenderContext ctx);

    public abstract double getStrokeCutStart(@NonNull RenderContext ctx);

    @Override
    public void layout(@NonNull RenderContext ctx) {
        Point2D start = getNonNull(START).getConvertedValue();
        Point2D end = getNonNull(END).getConvertedValue();
        Connector startConnector = get(START_CONNECTOR);
        Connector endConnector = get(END_CONNECTOR);
        Figure startTarget = get(START_TARGET);
        Figure endTarget = get(END_TARGET);

        // Find initial start and end points
        if (startConnector != null && startTarget != null) {
            start = startConnector.getPointAndDerivativeInWorld(this, startTarget).getPoint(Point2D::new);
        }
        if (endConnector != null && endTarget != null) {
            end = endConnector.getPointAndDerivativeInWorld(this, endTarget).getPoint(Point2D::new);
        }
        // Chop start and end points
        if (startConnector != null && startTarget != null) {
            java.awt.geom.Point2D.Double chp = startConnector.chopStart(ctx, this, startTarget, start, end);
            final Point2D p = worldToParent(chp.getX(), chp.getY());
            set(START, new CssPoint2D(p));
        }
        if (endConnector != null && endTarget != null) {
            java.awt.geom.Point2D.Double chp = endConnector.chopStart(ctx, this, endTarget, end, start);
            final Point2D p = worldToParent(chp.getX(), chp.getY());
            set(END, new CssPoint2D(p));
        }
    }

    @Override
    public void translateInLocal(@NonNull CssPoint2D t) {
        set(START, getNonNull(START).add(t));
        set(END, getNonNull(END).add(t));
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

    /**
     * This method can be overridden by a subclass to apply styles to the line
     * node.
     *
     * @param ctx  the context
     * @param node the node
     */

    protected void updateLineNode(@NonNull RenderContext ctx, @NonNull Line node) {
    }

    protected void updateMarkerNode(@NonNull RenderContext ctx,
                                    @NonNull Group group,
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
                if (!nodes.equals(markerNode.getElements())) {
                    markerNode.getElements().setAll(nodes);
                }
            } catch (ParseException e) {
                Logger.getLogger(AbstractStraightLineConnectionWithMarkersFigure.class.getName()).warning("Illegal path: " + svgString);
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
        Group g = (Group) node;
        Line lineNode = (Line) g.getChildren().get(0);
        final Path startMarkerNode = (Path) g.getChildren().get(1);
        final Path endMarkerNode = (Path) g.getChildren().get(2);

        Point2D start = getNonNull(START).getConvertedValue();
        Point2D end = getNonNull(END).getConvertedValue();

        final double startInset = getStrokeCutStart(ctx);
        final double endInset = getStrokeCutEnd(ctx);
        final String startMarkerStr = getMarkerStartShape();
        final String endMarkerStr = getMarkerEndShape();

        Point2D endMinusStart = end.subtract(start);
        Point2D startMinusEnd = start.subtract(end);
        Point2D dir = endMinusStart.normalize();
        if (startInset != 0) {
            start = start.add(dir.multiply(startInset));
        }
        if (endInset != 0) {
            end = end.add(dir.multiply(-endInset));
        }
        lineNode.setStartX(start.getX());
        lineNode.setStartY(start.getY());
        lineNode.setEndX(end.getX());
        lineNode.setEndY(end.getY());

        updateLineNode(ctx, lineNode);
        updateMarkerNode(ctx, g, startMarkerNode, new PointAndDerivative(start.getX(), start.getY(), endMinusStart.getX(), endMinusStart.getY()), startMarkerStr, getMarkerStartScaleFactor());
        updateMarkerNode(ctx, g, endMarkerNode, new PointAndDerivative(end.getX(), end.getY(), startMinusEnd.getX(), startMinusEnd.getY()), endMarkerStr, getMarkerEndScaleFactor());
        updateStartMarkerNode(ctx, startMarkerNode);
        updateEndMarkerNode(ctx, endMarkerNode);
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
}
