/*
 * @(#)SvgLineFigure.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.svg.figure;

import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.shape.Line;
import javafx.scene.transform.Transform;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.css.value.UnitConverter;
import org.jhotdraw8.draw.css.value.CssPoint2D;
import org.jhotdraw8.draw.css.value.CssRectangle2D;
import org.jhotdraw8.draw.figure.AbstractLeafFigure;
import org.jhotdraw8.draw.figure.HideableFigure;
import org.jhotdraw8.draw.figure.LockableFigure;
import org.jhotdraw8.draw.figure.PathIterableFigure;
import org.jhotdraw8.draw.figure.StyleableFigure;
import org.jhotdraw8.draw.key.CssSizeStyleableKey;
import org.jhotdraw8.draw.render.RenderContext;
import org.jhotdraw8.geom.FXTransforms;
import org.jhotdraw8.svg.text.SvgShapeRendering;
import org.jspecify.annotations.Nullable;

import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;

/**
 * Represents an SVG 'line' element.
 *
 */
public class SvgLineFigure extends AbstractLeafFigure
        implements StyleableFigure, LockableFigure, SvgTransformableFigure, PathIterableFigure, HideableFigure, SvgPathLengthFigure, SvgDefaultableFigure,
        SvgElementFigure {
    /**
     * The CSS type selector for this object is {@value #TYPE_SELECTOR}.
     */
    public static final String TYPE_SELECTOR = "line";
    public static final CssSizeStyleableKey X1 = new CssSizeStyleableKey("x1", CssSize.ZERO);
    public static final CssSizeStyleableKey Y1 = new CssSizeStyleableKey("y1", CssSize.ZERO);
    public static final CssSizeStyleableKey X2 = new CssSizeStyleableKey("x2", CssSize.ZERO);
    public static final CssSizeStyleableKey Y2 = new CssSizeStyleableKey("y2", CssSize.ZERO);

    @Override
    public Node createNode(RenderContext ctx) {
        Line n = new Line();
        n.setManaged(false);
        return n;
    }

    @Override
    public PathIterator getPathIterator(RenderContext ctx, @Nullable AffineTransform tx) {
        Path2D.Double p = new Path2D.Double();
        p.moveTo(getNonNull(X1).getConvertedValue(),
                getNonNull(Y1).getConvertedValue());
        p.lineTo(getNonNull(X2).getConvertedValue(),
                getNonNull(Y2).getConvertedValue());
        return p.getPathIterator(tx);
    }


    @Override
    public Bounds getBoundsInLocal() {
        return getCssLayoutBounds().getConvertedBoundsValue();
    }

    @Override
    public CssRectangle2D getCssLayoutBounds() {
        CssSize startX = getNonNull(X1);
        CssSize startY = getNonNull(Y1);
        CssSize endX = getNonNull(X2);
        CssSize endY = getNonNull(Y2);
        return new CssRectangle2D(new CssPoint2D(startX, startY), new CssPoint2D(endX, endY));
    }


    @Override
    public void reshapeInLocal(Transform transform) {
        CssSize startX = getNonNull(X1);
        CssSize startY = getNonNull(Y1);
        CssSize endX = getNonNull(X2);
        CssSize endY = getNonNull(Y2);
        CssPoint2D start = new CssPoint2D(startX, startY);
        CssPoint2D end = new CssPoint2D(endX, endY);

        CssPoint2D tstart = new CssPoint2D(FXTransforms.transform(transform, start.getConvertedValue()));
        CssPoint2D tend = new CssPoint2D(FXTransforms.transform(transform, end.getConvertedValue()));
        set(X1, tstart.getX());
        set(Y1, tstart.getY());
        set(X2, tend.getX());
        set(Y2, tend.getY());

    }

    @Override
    public void reshapeInLocal(CssSize x, CssSize y, CssSize width, CssSize height) {
        reshapeInLocal(x.getConvertedValue(), y.getConvertedValue(), width.getConvertedValue(), height.getConvertedValue());
    }

    @Override
    public void reshapeInLocal(double x, double y, double width, double height) {
        reshapeInLocal(FXTransforms.createReshapeTransform(getLayoutBounds(), x, y, width, height));
    }

    @Override
    public void updateNode(RenderContext ctx, Node node) {
        Line n = (Line) node;
        UnitConverter unit = ctx.getNonNull(RenderContext.UNIT_CONVERTER_KEY);
        double startX = getNonNull(X1).getConvertedValue(unit);
        double startY = getNonNull(Y1).getConvertedValue(unit);
        double endX = getNonNull(X2).getConvertedValue(unit);
        double endY = getNonNull(Y2).getConvertedValue(unit);
        // Zero size disables rendering
        if (startX == endX && startY == endY) {
            n.setVisible(false);
            return;
        }

        applyHideableFigureProperties(ctx, node);
        applyStyleableFigureProperties(ctx, node);
        applyTransformableFigureProperties(ctx, node);
        applySvgDefaultableStrokeProperties(ctx, n);
        applySvgDefaultableCompositingProperties(ctx, n);
        n.setStartX(startX);
        n.setStartY(startY);
        n.setEndX(endX);
        n.setEndY(endY);
        n.applyCss();

        // stroke is translated by 0.5 pixels down right
        SvgShapeRendering shapeRendering = getDefaultableStyled(SHAPE_RENDERING_KEY);
        if (shapeRendering == SvgShapeRendering.CRISP_EDGES) {
            // stroke is translated by 0.5 pixels down right
            // FIXME do this only for stroke and only on low-dpi renderings
            n.setTranslateX(0.5);
            n.setTranslateY(0.5);
        }
    }

    @Override
    public String getTypeSelector() {
        return TYPE_SELECTOR;
    }
}
