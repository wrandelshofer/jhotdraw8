package org.jhotdraw8.draw.figure;

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.draw.css.value.CssColor;
import org.jhotdraw8.draw.render.RenderContext;
import org.jhotdraw8.icollection.immutable.ImmutableList;
import org.jspecify.annotations.Nullable;

/**
 * This figure draws a straight line or an elbow line from a start point to an end point.
 * <p>
 * The start/end point can be located at an absolute position on the drawing,
 * or it can be connected to a point inside another figure.
 * <p>
 * The figure can have an optional marker shape at the start point, and at
 * the end point.
 *
 * @author Werner Randelshofer
 */
public class ElbowConnectionWithMarkersFigure extends AbstractElbowLineConnectionWithMarkersFigure
        implements HideableFigure, StyleableFigure,
        LockableFigure, CompositableFigure, MarkerFillableFigure, StrokableFigure, MarkerStartableFigure,
        MarkerEndableFigure, StrokeCuttableFigure, ElbowableLineFigure {
    /**
     * The CSS type selector for this object is {@value #TYPE_SELECTOR}.
     */
    public static final String TYPE_SELECTOR = "ElbowConnectionWithMarkers";

    public ElbowConnectionWithMarkersFigure() {
        this(0, 0, 1, 1);
    }

    public ElbowConnectionWithMarkersFigure(Point2D start, Point2D end) {
        this(start.getX(), start.getY(), end.getX(), end.getY());
    }

    public ElbowConnectionWithMarkersFigure(double startX, double startY, double endX, double endY) {
        super(startX, startY, endX, endY);
        set(MARKER_FILL, new CssColor("black", Color.BLACK));
    }

    @Override
    public String getTypeSelector() {
        return TYPE_SELECTOR;
    }

    @Override
    protected void updateEndMarkerNode(RenderContext ctx, Path node) {
        super.updateEndMarkerNode(ctx, node);
        applyMarkerFillableFigureProperties(ctx, node);
    }

    @Override
    public double getStrokeCutStart(RenderContext ctx) {
        return StrokeCuttableFigure.super.getStrokeCutStart();
    }

    @Override
    public double getStrokeCutEnd(RenderContext ctx) {
        return StrokeCuttableFigure.super.getStrokeCutEnd();
    }

    @Override
    public @Nullable ImmutableList<PathElement> getMarkerStartShape() {
        return getStyled(MarkerStartableFigure.MARKER_START_SHAPE);
    }

    @Override
    public double getMarkerStartScaleFactor() {
        return getStyledNonNull(MarkerStartableFigure.MARKER_START_SCALE_FACTOR);
    }

    @Override
    public @Nullable ImmutableList<PathElement> getMarkerEndShape() {
        return getStyled(MarkerEndableFigure.MARKER_END_SHAPE);
    }

    @Override
    public double getMarkerEndScaleFactor() {
        return getStyledNonNull(MarkerEndableFigure.MARKER_END_SCALE_FACTOR);
    }

    @Override
    public @Nullable CssSize getElbowOffset() {
        return getStyled(ElbowableLineFigure.ELBOW_OFFSET);
    }

}
