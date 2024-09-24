/*
 * @(#)LineConnectionWithMarkersFigure.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.figure;

import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import org.jhotdraw8.draw.css.value.CssColor;
import org.jhotdraw8.draw.render.RenderContext;
import org.jhotdraw8.icollection.immutable.ImmutableList;
import org.jspecify.annotations.Nullable;

/**
 * This figure draws a straight line from a start point to an end point.
 * <p>
 * The start point and the end point can be located either
 * <ul>
 *     <li>at an absolute position on the drawing,</li>
 *     <li>or at a point inside another figure</li>
 * </ul>
 * <p>
 * The figure can have an optional marker shape at the start point,
 * and at the end point.
 *
 *
 * @author Werner Randelshofer
 */
public class LineConnectionWithMarkersFigure extends AbstractStraightLineConnectionWithMarkersFigure
        implements HideableFigure, StyleableFigure,
        LockableFigure, CompositableFigure, MarkerFillableFigure, StrokableFigure, MarkerStartableFigure,
        MarkerEndableFigure, StrokeCuttableFigure {

    /**
     * The CSS type selector for this object is {@value #TYPE_SELECTOR}.
     */
    public static final String TYPE_SELECTOR = "LineConnectionWithMarkers";

    public LineConnectionWithMarkersFigure() {
        this(0, 0, 1, 1);
    }

    public LineConnectionWithMarkersFigure(Point2D start, Point2D end) {
        this(start.getX(), start.getY(), end.getX(), end.getY());
    }

    public LineConnectionWithMarkersFigure(double startX, double startY, double endX, double endY) {
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
    protected void updateLineNode(RenderContext ctx, Line node) {
        super.updateLineNode(ctx, node);
        applyStrokableFigureProperties(ctx, node);
    }

    @Override
    public void updateNode(RenderContext ctx, Node node) {
        super.updateNode(ctx, node);

        applyHideableFigureProperties(ctx, node);
        applyCompositableFigureProperties(ctx, node);
        applyStyleableFigureProperties(ctx, node);
    }

    @Override
    protected void updateStartMarkerNode(RenderContext ctx, Path node) {
        super.updateStartMarkerNode(ctx, node);
        applyMarkerFillableFigureProperties(ctx, node);
    }

    @Override
    public double getMarkerEndScaleFactor() {
        return getStyledNonNull(MarkerEndableFigure.MARKER_END_SCALE_FACTOR);
    }

    @Override
    public @Nullable ImmutableList<PathElement> getMarkerEndShape() {
        return getStyled(MarkerEndableFigure.MARKER_END_SHAPE);
    }

    @Override
    public double getMarkerStartScaleFactor() {
        return getStyledNonNull(MarkerStartableFigure.MARKER_START_SCALE_FACTOR);
    }

    @Override
    public @Nullable ImmutableList<PathElement> getMarkerStartShape() {
        return getStyled(MarkerStartableFigure.MARKER_START_SHAPE);
    }

    @Override
    public double getStrokeCutEnd(RenderContext ctx) {
        return StrokeCuttableFigure.super.getStrokeCutEnd();
    }

    @Override
    public double getStrokeCutStart(RenderContext ctx) {
        return StrokeCuttableFigure.super.getStrokeCutStart();
    }

    @Override
    public @Nullable ImmutableList<PathElement> getMarkerCenterShape() {
        return null;
    }

    @Override
    public double getMarkerCenterScaleFactor() {
        return 1.0;
    }
}
