/*
 * @(#)TextStrokeableFigure.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.figure;

import javafx.scene.paint.Paint;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.shape.StrokeType;
import org.jhotdraw8.css.converter.SizeCssConverter;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.css.value.DefaultUnitConverter;
import org.jhotdraw8.css.value.UnitConverter;
import org.jhotdraw8.draw.css.value.Paintable;
import org.jhotdraw8.draw.key.CssSizeStyleableKey;
import org.jhotdraw8.draw.key.NonNullEnumStyleableKey;
import org.jhotdraw8.draw.key.NonNullListStyleableKey;
import org.jhotdraw8.draw.key.NullablePaintableStyleableKey;
import org.jhotdraw8.draw.key.StrokeStyleableMapAccessor;
import org.jhotdraw8.draw.render.RenderContext;
import org.jhotdraw8.icollection.VectorList;
import org.jhotdraw8.icollection.persistent.PersistentList;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Objects;

/**
 * {@code TextStrokeableFigure} allows to change the stroke of the
 * text.
 *
 * @author Werner Randelshofer
 */
public interface TextStrokeableFigure extends Figure {

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
    CssSizeStyleableKey TEXT_STROKE_DASH_OFFSET = new CssSizeStyleableKey("text-stroke-dashoffset", CssSize.ZERO);
    /**
     * Defines the end cap style. Default value: {@code SQUARE}.
     * <p>
     * References:
     * <dl>
     * <dt>SVG Stroke Properties</dt>
     * <dd><a href="http://www.w3.org/TR/SVG/painting.html#StrokeProperties">w3.org</a></dd>
     * </dl>
     */
    NonNullEnumStyleableKey<StrokeLineCap> TEXT_STROKE_LINE_CAP = new NonNullEnumStyleableKey<>("text-stroke-linecap", StrokeLineCap.class, StrokeLineCap.BUTT);
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
    NonNullEnumStyleableKey<StrokeLineJoin> TEXT_STROKE_LINE_JOIN = new NonNullEnumStyleableKey<>("text-stroke-linejoin", StrokeLineJoin.class, StrokeLineJoin.MITER);
    /**
     * Defines the limit for the {@code StrokeLineJoin.MITER} style. Default
     * value: {@code 4.0}.
     * <p>
     * References:
     * <dl>
     * <dt>SVG Stroke Properties</dt>
     * <dd><a href="http://www.w3.org/TR/SVG/painting.html#StrokeProperties">w3.org</a></dd>
     * </dl>
     */
    CssSizeStyleableKey TEXT_STROKE_MITER_LIMIT = new CssSizeStyleableKey("text-stroke-miterlimit", CssSize.of(10.0));
    /**
     * Defines the paint used for filling the outline of the figure. Default
     * value: {@code null}.
     * <p>
     * References:
     * <dl>
     * <dt>SVG Stroke Properties</dt>
     * <dd><a href="http://www.w3.org/TR/SVG/painting.html#StrokeProperties">w3.org</a></dd>
     * </dl>
     */
    NullablePaintableStyleableKey TEXT_STROKE = new NullablePaintableStyleableKey("text-stroke", null);
    /**
     * Defines the stroke type used for drawing outline of the figure.
     * <p>
     * Default value: {@code StrokeType.OUTSIDE}.
     */
    NonNullEnumStyleableKey<StrokeType> TEXT_STROKE_TYPE = new NonNullEnumStyleableKey<>("text-stroke-type", StrokeType.class, StrokeType.OUTSIDE);
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
    CssSizeStyleableKey TEXT_STROKE_WIDTH = new CssSizeStyleableKey("text-stroke-width", CssSize.ONE);

    /**
     * Defines the dash array used. Default value: {@code empty array}.
     * <p>
     * References:
     * <dl>
     * <dt>SVG Stroke Properties</dt>
     * <dd><a href="http://www.w3.org/TR/SVG/painting.html#StrokeProperties">w3.org</a></dd>
     * </dl>
     */
    NonNullListStyleableKey<CssSize> TEXT_STROKE_DASH_ARRAY = new NonNullListStyleableKey<>("text-stroke-dasharray",
            CssSize.class,
            new SizeCssConverter(false), VectorList.of());

    /**
     * Combined map accessor for all stroke style properties.
     */
    @SuppressWarnings("unused")//Field is accessed by CSS using Reflection
    StrokeStyleableMapAccessor TEXT_STROKE_STYLE = new StrokeStyleableMapAccessor("text-stroke-style",
            TEXT_STROKE_TYPE, TEXT_STROKE_LINE_CAP, TEXT_STROKE_LINE_JOIN, TEXT_STROKE_MITER_LIMIT, TEXT_STROKE_DASH_OFFSET, TEXT_STROKE_DASH_ARRAY);

    /**
     * Updates a shape node.
     *
     * @param ctx   the render context
     * @param shape a shape node
     */
    default void applyTextStrokeableFigureProperties(@Nullable RenderContext ctx, Shape shape) {
        Paint paint = Paintable.getPaint(getStyled(TEXT_STROKE), ctx);
        UnitConverter units = ctx == null ? DefaultUnitConverter.getInstance() : ctx.getNonNull(RenderContext.UNIT_CONVERTER_KEY);

        double strokeWidth = units.convert(getStyledNonNull(TEXT_STROKE_WIDTH), UnitConverter.DEFAULT);
        if (!Objects.equals(shape.getStroke(), paint)) {
            shape.setStroke(paint);
        }
        if (paint == null) {
            return;
        }
        if (shape.getStrokeWidth() != strokeWidth) {
            shape.setStrokeWidth(strokeWidth);
        }
        StrokeLineCap slp = getStyled(TEXT_STROKE_LINE_CAP);
        if (shape.getStrokeLineCap() != slp) {
            shape.setStrokeLineCap(slp);
        }
        StrokeLineJoin slj = getStyled(TEXT_STROKE_LINE_JOIN);
        if (shape.getStrokeLineJoin() != slj) {
            shape.setStrokeLineJoin(slj);
        }
        double d = units.convert(getStyledNonNull(TEXT_STROKE_MITER_LIMIT), UnitConverter.DEFAULT);
        if (shape.getStrokeMiterLimit() != d) {
            shape.setStrokeMiterLimit(d);
        }
        StrokeType st = getStyled(TEXT_STROKE_TYPE);
        if (shape.getStrokeType() != st) {
            shape.setStrokeType(st);
        }
        applyTextStrokeDashProperties(shape);

    }


    default void applyTextStrokeDashProperties(Shape shape) {
        double d = getStyledNonNull(TEXT_STROKE_DASH_OFFSET).getConvertedValue();
        if (shape.getStrokeDashOffset() != d) {
            shape.setStrokeDashOffset(d);
        }
        PersistentList<CssSize> dashArray = getStyledNonNull(TEXT_STROKE_DASH_ARRAY);
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

}
