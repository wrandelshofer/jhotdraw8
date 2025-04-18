/*
 * @(#)StrokableFigure.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.figure;

import javafx.geometry.Bounds;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.shape.StrokeType;
import org.jhotdraw8.css.converter.SizeCssConverter;
import org.jhotdraw8.css.value.CssColor;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.css.value.Paintable;
import org.jhotdraw8.css.value.UnitConverter;
import org.jhotdraw8.draw.key.CssSizeStyleableKey;
import org.jhotdraw8.draw.key.NonNullEnumStyleableKey;
import org.jhotdraw8.draw.key.NonNullListStyleableKey;
import org.jhotdraw8.draw.key.NullablePaintableStyleableKey;
import org.jhotdraw8.draw.key.StrokeStyleableMapAccessor;
import org.jhotdraw8.draw.render.RenderContext;
import org.jhotdraw8.geom.FXRectangles;
import org.jhotdraw8.icollection.VectorList;
import org.jhotdraw8.icollection.persistent.PersistentList;
import org.jspecify.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Interface for figures which render a {@code javafx.scene.shape.Shape} and can
 * be stroked.
 *
 */
public interface StrokableFigure extends Figure {

    /**
     * Defines the distance in user coordinates for the dashing pattern. Default
     * value: {@code 0}.
     * <p>
     * References:
     * <dl>
     * <dt>SVG Stroke Properties</dt>
     * <dd><a href="http://www.w3.org/TR/SVG/painting.html#StrokeProperties">w3.org</a></dd>
     * </dl>
     */
    CssSizeStyleableKey STROKE_DASH_OFFSET = new CssSizeStyleableKey("stroke-dashoffset", CssSize.ZERO);
    /**
     * Defines the end cap style. Default value: {@code SQUARE}.
     * <p>
     * References:
     * <dl>
     * <dt>SVG Stroke Properties</dt>
     * <dd><a href="http://www.w3.org/TR/SVG/painting.html#StrokeProperties">w3.org</a></dd>
     * </dl>
     */
    NonNullEnumStyleableKey<StrokeLineCap> STROKE_LINE_CAP = new NonNullEnumStyleableKey<>("stroke-linecap", StrokeLineCap.class, StrokeLineCap.BUTT);
    /**
     * Defines the style applied where path segments meet. Default value:
     * {@code MITER}.
     * <p>
     * References:
     * <dl>
     * <dt>SVG Stroke Properties</dt>
     * <dd><a href="http://www.w3.org/TR/SVG/painting.html#StrokeProperties">w3.org</a></dd>
     * </dl>
     */
    NonNullEnumStyleableKey<StrokeLineJoin> STROKE_LINE_JOIN = new NonNullEnumStyleableKey<>("stroke-linejoin", StrokeLineJoin.class, StrokeLineJoin.MITER);
    /**
     * Defines the limit for the {@code StrokeLineJoin.MITER} style.
     * <p>
     * Default value: {@code 4.0}.
     * <p>
     * References:
     * <dl>
     * <dt>SVG Stroke Properties</dt>
     * <dd><a href="http://www.w3.org/TR/SVG/painting.html#StrokeProperties">w3.org</a></dd>
     * </dl>
     */
    CssSizeStyleableKey STROKE_MITER_LIMIT = new CssSizeStyleableKey("stroke-miterlimit", CssSize.of(4.0));
    /**
     * Defines the paint used for filling the outline of the figure. Default
     * value: {@code Color.BLACK}.
     * <p>
     * References:
     * <dl>
     * <dt>SVG Stroke Properties</dt>
     * <dd><a href="http://www.w3.org/TR/SVG/painting.html#StrokeProperties">w3.org</a></dd>
     * </dl>
     */
    NullablePaintableStyleableKey STROKE = new NullablePaintableStyleableKey("stroke", new CssColor("canvastext", Color.BLACK));
    /**
     * Defines the stroke type used for drawing outline of the figure.
     * <p>
     * Default value: {@code StrokeType.CENTERED}.
     */
    NonNullEnumStyleableKey<StrokeType> STROKE_TYPE = new NonNullEnumStyleableKey<>("stroke-type", StrokeType.class, StrokeType.CENTERED);
    /**
     * Defines the width of the outline of the figure.
     * <p>
     * Default value: {@code 1.0}.
     * <p>
     * References:
     * <dl>
     * <dt>SVG Stroke Properties</dt>
     * <dd><a href="http://www.w3.org/TR/SVG/painting.html#StrokeProperties">w3.org</a></dd>
     * </dl>
     */
    CssSizeStyleableKey STROKE_WIDTH = new CssSizeStyleableKey("stroke-width", CssSize.ONE);
    /**
     * Defines the dash array used. Default value: {@code empty array}.
     * <p>
     * References:
     * <dl>
     * <dt>SVG Stroke Properties</dt>
     * <dd><a href="http://www.w3.org/TR/SVG/painting.html#StrokeProperties">w3.org</a></dd>
     * </dl>
     */
    NonNullListStyleableKey<CssSize> STROKE_DASH_ARRAY = new NonNullListStyleableKey<>("stroke-dasharray",
            CssSize.class,
            new SizeCssConverter(false), VectorList.of());

    /**
     * Combined map accessor for all stroke style properties.
     * <p>
     * Note: this is a non-standard composite map accessor and thus transient!
     */
    @Nullable StrokeStyleableMapAccessor STROKE_STYLE = new StrokeStyleableMapAccessor("stroke-style",
            STROKE_TYPE, STROKE_LINE_CAP, STROKE_LINE_JOIN, STROKE_MITER_LIMIT, STROKE_DASH_OFFSET, STROKE_DASH_ARRAY);

    default void applyStrokeCapAndJoinProperties(RenderContext ctx, Shape shape) {
        double d;
        StrokeLineCap slp = getStyled(STROKE_LINE_CAP);
        if (shape.getStrokeLineCap() != slp) {
            shape.setStrokeLineCap(slp);
        }
        StrokeLineJoin slj = getStyled(STROKE_LINE_JOIN);
        if (shape.getStrokeLineJoin() != slj) {
            shape.setStrokeLineJoin(slj);
        }
        d = getStyledNonNull(STROKE_MITER_LIMIT).getConvertedValue();
        if (shape.getStrokeMiterLimit() != d) {
            shape.setStrokeMiterLimit(d);
        }
    }

    default void applyStrokeDashProperties(RenderContext ctx, Shape shape) {
        double d = getStyledNonNull(STROKE_DASH_OFFSET).getConvertedValue();
        if (shape.getStrokeDashOffset() != d) {
            shape.setStrokeDashOffset(d);
        }
        PersistentList<CssSize> dashArray = getStyledNonNull(STROKE_DASH_ARRAY);
        if (dashArray.isEmpty()) {
            shape.getStrokeDashArray().clear();
        } else {
            ArrayList<Double> list = new ArrayList<>(dashArray.size());
            for (CssSize sz : dashArray) {
                list.add(sz.getConvertedValue());
            }
            shape.getStrokeDashArray().setAll(list);
        }
    }

    default void applyStrokeTypeProperties(RenderContext ctx, Shape shape) {
        StrokeType st = getStyled(STROKE_TYPE);
        if (shape.getStrokeType() != st) {
            shape.setStrokeType(st);
        }
    }

    /**
     * Updates a shape node.
     *
     * @param ctx   the render context
     * @param shape a shape node
     */
    default void applyStrokableFigureProperties(@Nullable RenderContext ctx, Shape shape) {
        applyStrokeColorProperties(ctx, shape);
        applyStrokeWidthProperties(ctx, shape);
        applyStrokeCapAndJoinProperties(ctx, shape);
        applyStrokeTypeProperties(ctx, shape);
        applyStrokeDashProperties(ctx, shape);
    }

    default void applyStrokeColorProperties(@Nullable RenderContext ctx, Shape shape) {
        Paint p = Paintable.getPaint(getStyled(STROKE), ctx);
        if (!Objects.equals(shape.getStroke(), p)) {
            shape.setStroke(p);
        }
    }

    default void applyStrokeWidthProperties(@Nullable RenderContext ctx, Shape shape) {
        CssSize cssSize = getStyledNonNull(STROKE_WIDTH);
        double width = ctx == null ? cssSize.getConvertedValue()
                : ctx.getNonNull(RenderContext.UNIT_CONVERTER_KEY).convert(cssSize, UnitConverter.DEFAULT);
        if (shape.getStrokeWidth() != width) {
            shape.setStrokeWidth(width);
        }

    }

    default BasicStroke getStyledStroke(@Nullable RenderContext ctx) {
        CssSize cssSize = getStyledNonNull(STROKE_WIDTH);
        double width = ctx == null ? cssSize.getConvertedValue()
                : ctx.getNonNull(RenderContext.UNIT_CONVERTER_KEY).convert(cssSize, UnitConverter.DEFAULT);
        final StrokeLineCap cap = getStyledNonNull(STROKE_LINE_CAP);
        final int basicCap = switch (cap) {
            default -> BasicStroke.CAP_BUTT;
            case ROUND -> BasicStroke.CAP_ROUND;
            case SQUARE -> BasicStroke.CAP_SQUARE;
        };
        final PersistentList<CssSize> dashlist = getStyledNonNull(STROKE_DASH_ARRAY);
        float[] dasharray;
        if (dashlist.isEmpty()) {
            dasharray = null;
        } else {
            dasharray = new float[dashlist.size()];
            int i = 0;
            for (CssSize sz : dashlist) {
                dasharray[i++] = (float) sz.getConvertedValue();
            }
        }
        final double dashoffset = getStyledNonNull(STROKE_DASH_OFFSET).getConvertedValue();
        final StrokeLineJoin join = getStyledNonNull(STROKE_LINE_JOIN);
        final int basicJoin = switch (join) {
            default -> BasicStroke.JOIN_BEVEL;
            case MITER -> BasicStroke.JOIN_MITER;
            case ROUND -> BasicStroke.JOIN_ROUND;
        };
        final double miterlimit = getStyledNonNull(STROKE_MITER_LIMIT).getConvertedValue();

        return new BasicStroke((float) width, basicCap, basicJoin, (float) miterlimit, dasharray, (float) dashoffset);

    }

    @Override
    default Bounds getBoundsInLocal() {
        Bounds layoutBounds = getLayoutBounds();
        Paintable paintable = get(STROKE);
        if (paintable == null) {
            return layoutBounds;
        }
        double strokeWidth = getNonNull(STROKE_WIDTH).getConvertedValue();
        if (strokeWidth == 0.0) {
            return layoutBounds;
        }
        StrokeType strokeType = getNonNull(STROKE_TYPE);
        return switch (strokeType) {
            default -> layoutBounds;
            case OUTSIDE -> FXRectangles.grow(layoutBounds, strokeWidth * 2, strokeWidth * 2);
            case CENTERED -> FXRectangles.grow(layoutBounds, strokeWidth, strokeWidth);
        };
    }
}
