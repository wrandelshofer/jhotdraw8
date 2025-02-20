/*
 * @(#)MarkerStrokableFigure.java
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
import org.jhotdraw8.css.value.Paintable;
import org.jhotdraw8.draw.key.CssSizeStyleableKey;
import org.jhotdraw8.draw.key.NonNullEnumStyleableKey;
import org.jhotdraw8.draw.key.NonNullListStyleableKey;
import org.jhotdraw8.draw.key.NullablePaintableStyleableKey;
import org.jhotdraw8.draw.key.StrokeStyleableMapAccessor;
import org.jhotdraw8.icollection.VectorList;
import org.jhotdraw8.icollection.persistent.PersistentList;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Interface for figures which can stroke a marker.
 *
 */
public interface MarkerStrokableFigure extends Figure {

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
    @Nullable CssSizeStyleableKey MARKER_STROKE_DASH_OFFSET = new CssSizeStyleableKey("marker-stroke-dashoffset", CssSize.ZERO);
    /**
     * Defines the end cap style. Default value: {@code SQUARE}.
     * <p>
     * References:
     * <dl>
     * <dt>SVG Stroke Properties</dt>
     * <dd><a href="http://www.w3.org/TR/SVG/painting.html#StrokeProperties">w3.org</a></dd>
     * </dl>
     */
    NonNullEnumStyleableKey<StrokeLineCap> MARKER_STROKE_LINE_CAP = new NonNullEnumStyleableKey<>("marker-stroke-linecap", StrokeLineCap.class, StrokeLineCap.BUTT);
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
    NonNullEnumStyleableKey<StrokeLineJoin> MARKER_STROKE_LINE_JOIN = new NonNullEnumStyleableKey<>("marker-stroke-linejoin", StrokeLineJoin.class, StrokeLineJoin.MITER);
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
    CssSizeStyleableKey MARKER_STROKE_MITER_LIMIT = new CssSizeStyleableKey("marker-stroke-miterlimit", CssSize.of(4.0));
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
    NullablePaintableStyleableKey MARKER_STROKE = new NullablePaintableStyleableKey("marker-stroke", null);
    /**
     * Defines the stroke type used for drawing outline of the figure.
     * <p>
     * Default value: {@code StrokeType.CENTERED}.
     */
    NonNullEnumStyleableKey<StrokeType> MARKER_STROKE_TYPE = new NonNullEnumStyleableKey<>("marker-stroke-type", StrokeType.class, StrokeType.CENTERED);
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
    CssSizeStyleableKey MARKER_STROKE_WIDTH = new CssSizeStyleableKey("marker-stroke-width", CssSize.ONE);
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
    NonNullListStyleableKey<CssSize> MARKER_STROKE_DASH_ARRAY = new NonNullListStyleableKey<>("marker-stroke-dasharray",
            CssSize.class,
            new SizeCssConverter(false), VectorList.of());

    /**
     * Combined map accessor for all stroke style properties.
     * <p>
     * Note: this is a non-standard composite map accessor and thus transient!
     */
    @Nullable StrokeStyleableMapAccessor STROKE_STYLE = new StrokeStyleableMapAccessor("marker-stroke-style",
            MARKER_STROKE_TYPE, MARKER_STROKE_LINE_CAP, MARKER_STROKE_LINE_JOIN, MARKER_STROKE_MITER_LIMIT,
            MARKER_STROKE_DASH_OFFSET, MARKER_STROKE_DASH_ARRAY);

    default void applyMarkerStrokeCapAndJoinProperties(Shape shape) {
        double d;
        StrokeLineCap slp = getStyled(MARKER_STROKE_LINE_CAP);
        if (shape.getStrokeLineCap() != slp) {
            shape.setStrokeLineCap(slp);
        }
        StrokeLineJoin slj = getStyled(MARKER_STROKE_LINE_JOIN);
        if (shape.getStrokeLineJoin() != slj) {
            shape.setStrokeLineJoin(slj);
        }
        d = getStyledNonNull(MARKER_STROKE_MITER_LIMIT).getConvertedValue();
        if (shape.getStrokeMiterLimit() != d) {
            shape.setStrokeMiterLimit(d);
        }
    }

    default void applyMarkerStrokeDashProperties(Shape shape) {
        double d = getStyledNonNull(MARKER_STROKE_DASH_OFFSET).getConvertedValue();
        if (shape.getStrokeDashOffset() != d) {
            shape.setStrokeDashOffset(d);
        }
        PersistentList<CssSize> dashArray = getStyledNonNull(MARKER_STROKE_DASH_ARRAY);
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

    default void applyMarkerStrokeTypeProperties(Shape shape) {
        StrokeType st = getStyled(MARKER_STROKE_TYPE);
        if (shape.getStrokeType() != st) {
            shape.setStrokeType(st);
        }
    }

    /**
     * Updates a shape node.
     *
     * @param shape a shape node
     */
    default void applyMarkerStrokableFigureProperties(Shape shape) {
        applyMarkerStrokeColorProperties(shape);
        applyMarkerStrokeWidthProperties(shape);
        applyMarkerStrokeCapAndJoinProperties(shape);

        applyMarkerStrokeTypeProperties(shape);
        applyMarkerStrokeDashProperties(shape);
    }

    default void applyMarkerStrokeColorProperties(Shape shape) {
        Paint p = Paintable.getPaint(getStyled(MARKER_STROKE));
        if (!Objects.equals(shape.getStroke(), p)) {
            shape.setStroke(p);
        }
    }

    default void applyMarkerStrokeWidthProperties(Shape shape) {
        double d = getStyledNonNull(MARKER_STROKE_WIDTH).getConvertedValue();
        if (shape.getStrokeWidth() != d) {
            shape.setStrokeWidth(d);
        }

    }
}
