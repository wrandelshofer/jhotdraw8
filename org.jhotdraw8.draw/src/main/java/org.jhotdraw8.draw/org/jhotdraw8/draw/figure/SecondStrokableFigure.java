/*
 * @(#)SecondStrokableFigure.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.figure;

import javafx.scene.paint.Paint;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.shape.StrokeType;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.css.converter.CssSizeConverter;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.draw.css.value.Paintable;
import org.jhotdraw8.draw.key.CssSizeStyleableKey;
import org.jhotdraw8.draw.key.EnumStyleableKey;
import org.jhotdraw8.draw.key.ListStyleableKey;
import org.jhotdraw8.draw.key.NullablePaintableStyleableKey;
import org.jhotdraw8.draw.key.StrokeStyleableMapAccessor;
import org.jhotdraw8.draw.render.RenderContext;
import org.jhotdraw8.fxcollection.typesafekey.TypeToken;
import org.jhotdraw8.icollection.SimpleImmutableList;
import org.jhotdraw8.icollection.immutable.ImmutableList;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Interface for figures which can render a second stroke.
 *
 * @author Werner Randelshofer
 */
public interface SecondStrokableFigure extends Figure {

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
    @Nullable CssSizeStyleableKey SECOND_STROKE_DASH_OFFSET = new CssSizeStyleableKey("second-stroke-dashoffset", CssSize.ZERO);
    /**
     * Defines the end cap style. Default value: {@code SQUARE}.
     * <p>
     * References:
     * <dl>
     * <dt>SVG Stroke Properties</dt>
     * <dd><a href="http://www.w3.org/TR/SVG/painting.html#StrokeProperties">w3.org</a></dd>
     * </dl>
     */
    EnumStyleableKey<StrokeLineCap> SECOND_STROKE_LINE_CAP = new EnumStyleableKey<>("second-stroke-linecap", StrokeLineCap.class, StrokeLineCap.BUTT);
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
    EnumStyleableKey<StrokeLineJoin> SECOND_STROKE_LINE_JOIN = new EnumStyleableKey<>("second-stroke-linejoin", StrokeLineJoin.class, StrokeLineJoin.MITER);
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
    CssSizeStyleableKey SECOND_STROKE_MITER_LIMIT = new CssSizeStyleableKey("second-stroke-miterlimit", CssSize.of(4.0));
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
    @NonNull
    NullablePaintableStyleableKey SECOND_STROKE = new NullablePaintableStyleableKey("second-stroke", null);
    /**
     * Defines the stroke type used for drawing outline of the figure.
     * <p>
     * Default value: {@code StrokeType.CENTERED}.
     */
    EnumStyleableKey<StrokeType> SECOND_STROKE_TYPE = new EnumStyleableKey<>("second-stroke-type", StrokeType.class, StrokeType.CENTERED);
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
    CssSizeStyleableKey SECOND_STROKE_WIDTH = new CssSizeStyleableKey("second-stroke-width", CssSize.ONE);
    /**
     * Defines the opacity of the outline of the figure.
     * <p>
     * Default value: {@code 1.0}.
     * <p>
     * References:
     * <dl>
     *<dt>SVG Stroke Properties</dt>
     *<dd><a href="http://www.w3.org/TR/SVG/painting.html#StrokeProperties">w3.org</a></dd>
     *</dl>
     * /
     * public static NullableCssSizeStyleableKey STROKE_OPACITY = new
     * NullableCssSizeStyleableKey("stroke-opacity", DirtyMask.of(DirtyBits.NODE),
     * 1.0);
     */
    /**
     * Defines the dash array used. Default value: {@code empty array}.
     * <p>
     * References:
     * <dl>
     * <dt>SVG Stroke Properties</dt>
     * <dd><a href="http://www.w3.org/TR/SVG/painting.html#StrokeProperties">w3.org</a></dd>
     * </dl>
     */
    ListStyleableKey<CssSize> SECOND_STROKE_DASH_ARRAY = new ListStyleableKey<>("second-stroke-dasharray",
            new TypeToken<ImmutableList<CssSize>>() {
            },
            new CssSizeConverter(false), SimpleImmutableList.of());

    /**
     * Combined map accessor for all stroke style properties.
     * <p>
     * Note: this is a non-standard composite map accessor and thus transient!
     */
    @Nullable StrokeStyleableMapAccessor STROKE_STYLE = new StrokeStyleableMapAccessor("second-stroke-style",
            SECOND_STROKE_TYPE, SECOND_STROKE_LINE_CAP, SECOND_STROKE_LINE_JOIN, SECOND_STROKE_MITER_LIMIT,
            SECOND_STROKE_DASH_OFFSET, SECOND_STROKE_DASH_ARRAY);

    default void applySecondStrokeCapAndJoinProperties(RenderContext ctx, @NonNull Shape shape) {
        double d;
        StrokeLineCap slp = getStyled(SECOND_STROKE_LINE_CAP);
        if (shape.getStrokeLineCap() != slp) {
            shape.setStrokeLineCap(slp);
        }
        StrokeLineJoin slj = getStyled(SECOND_STROKE_LINE_JOIN);
        if (shape.getStrokeLineJoin() != slj) {
            shape.setStrokeLineJoin(slj);
        }
        d = getStyledNonNull(SECOND_STROKE_MITER_LIMIT).getConvertedValue();
        if (shape.getStrokeMiterLimit() != d) {
            shape.setStrokeMiterLimit(d);
        }
    }

    default void applySecondStrokeDashProperties(RenderContext ctx, @NonNull Shape shape) {
        double d = getStyledNonNull(SECOND_STROKE_DASH_OFFSET).getConvertedValue();
        if (shape.getStrokeDashOffset() != d) {
            shape.setStrokeDashOffset(d);
        }
        ImmutableList<CssSize> dashArray = getStyledNonNull(SECOND_STROKE_DASH_ARRAY);
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

    default void applySecondStrokeTypeProperties(RenderContext ctx, @NonNull Shape shape) {
        StrokeType st = getStyled(SECOND_STROKE_TYPE);
        if (shape.getStrokeType() != st) {
            shape.setStrokeType(st);
        }
    }

    /**
     * Updates a shape node.
     *
     * @param ctx
     * @param shape a shape node
     */
    default void applySecondStrokeableFigureProperties(RenderContext ctx, @NonNull Shape shape) {
        applySecondStrokeColorProperties(ctx, shape);
        applySecondStrokeWidthProperties(ctx, shape);
        applySecondStrokeCapAndJoinProperties(ctx, shape);

        applySecondStrokeTypeProperties(ctx, shape);
        applySecondStrokeDashProperties(ctx, shape);
    }

    default void applySecondStrokeColorProperties(RenderContext ctx, @NonNull Shape shape) {
        Paint p = Paintable.getPaint(getStyled(SECOND_STROKE), ctx);
        if (!Objects.equals(shape.getStroke(), p)) {
            shape.setStroke(p);
        }
    }

    default void applySecondStrokeWidthProperties(RenderContext ctx, @NonNull Shape shape) {
        double d = getStyledNonNull(SECOND_STROKE_WIDTH).getConvertedValue();
        if (shape.getStrokeWidth() != d) {
            shape.setStrokeWidth(d);
        }

    }
}
