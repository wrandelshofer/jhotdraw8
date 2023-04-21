/*
 * @(#)SvgLinearGradientFigure.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.svg.figure;

import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Paint;
import javafx.scene.paint.Stop;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.immutable.ImmutableList;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.css.value.DefaultUnitConverter;
import org.jhotdraw8.css.value.UnitConverter;
import org.jhotdraw8.draw.key.CssSizeStyleableKey;
import org.jhotdraw8.draw.render.RenderContext;
import org.jhotdraw8.svg.text.SvgGradientUnits;

import java.util.ArrayList;

/**
 * Represents an SVG 'linearGradient' element.
 *
 * @author Werner Randelshofer
 */
public class SvgLinearGradientFigure extends AbstractSvgGradientFigure {

    /**
     * The CSS type selector for a label object is {@value #TYPE_SELECTOR}.
     */
    public static final String TYPE_SELECTOR = "linearGradient";
    public static final @NonNull CssSizeStyleableKey X1 = new CssSizeStyleableKey("x1", CssSize.ZERO);
    public static final @NonNull CssSizeStyleableKey Y1 = new CssSizeStyleableKey("y1", CssSize.ZERO);
    public static final @NonNull CssSizeStyleableKey X2 = new CssSizeStyleableKey("x2", CssSize.ONE);
    public static final @NonNull CssSizeStyleableKey Y2 = new CssSizeStyleableKey("y2", CssSize.ZERO);

    @Override
    public @Nullable Paint getPaint(@Nullable RenderContext ctx) {
        UnitConverter unit = ctx == null ? null : ctx.get(RenderContext.UNIT_CONVERTER_KEY);
        if (unit == null) {
            unit = DefaultUnitConverter.getInstance();
        }

        double x1 = getStyledNonNull(X1).getConvertedValue(unit);
        double x2 = getStyledNonNull(X2).getConvertedValue(unit);
        double y1 = getStyledNonNull(Y1).getConvertedValue(unit);
        double y2 = getStyledNonNull(Y2).getConvertedValue(unit);
        SvgGradientUnits gradientUnits = getStyledNonNull(GRADIENT_UNITS);

        ImmutableList<SvgStop> cssStops = getNonNull(STOPS);
        ArrayList<Stop> stops = getStops(cssStops);

        CycleMethod spreadMethod = getStyledNonNull(SPREAD_METHOD);

        if (stops.size() == 1) {
            return stops.get(0).getColor();
        }

        return new LinearGradient(x1, y1, x2, y2, gradientUnits == SvgGradientUnits.OBJECT_BOUNDING_BOX,
                spreadMethod, stops);

    }


    @Override
    public @NonNull String getTypeSelector() {
        return TYPE_SELECTOR;
    }


}
