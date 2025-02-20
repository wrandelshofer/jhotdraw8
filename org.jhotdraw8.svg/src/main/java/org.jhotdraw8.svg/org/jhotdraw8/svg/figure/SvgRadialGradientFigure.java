/*
 * @(#)SvgRadialGradientFigure.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.svg.figure;

import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Paint;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import org.jhotdraw8.css.render.BasicRenderContext;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.css.value.DefaultUnitConverter;
import org.jhotdraw8.css.value.UnitConverter;
import org.jhotdraw8.draw.key.CssSizeStyleableKey;
import org.jhotdraw8.draw.render.RenderContext;
import org.jhotdraw8.icollection.persistent.PersistentList;
import org.jhotdraw8.svg.text.SvgGradientUnits;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;

/**
 * Represents an SVG 'radialGradient' element.
 *
 */
public class SvgRadialGradientFigure extends AbstractSvgGradientFigure {

    /**
     * The CSS type selector for a label object is {@value #TYPE_SELECTOR}.
     */
    public static final String TYPE_SELECTOR = "radialGradient";
    public static final CssSizeStyleableKey CX = new CssSizeStyleableKey("cx", CssSize.of(0.5));
    public static final CssSizeStyleableKey CY = new CssSizeStyleableKey("cy", CssSize.of(0.5));
    public static final CssSizeStyleableKey R = new CssSizeStyleableKey("r", CssSize.of(0.5));

    @Override
    public @Nullable Paint getPaint(@Nullable BasicRenderContext ctx) {
        UnitConverter unit = ctx == null ? null : ctx.get(RenderContext.UNIT_CONVERTER_KEY);
        if (unit == null) {
            unit = DefaultUnitConverter.getInstance();
        }

        double cx = getStyledNonNull(CX).getConvertedValue(unit);
        double r = getStyledNonNull(R).getConvertedValue(unit);
        double cy = getStyledNonNull(CY).getConvertedValue(unit);
        SvgGradientUnits gradientUnits = getStyledNonNull(GRADIENT_UNITS);

        PersistentList<SvgStop> cssStops = getNonNull(STOPS);
        ArrayList<Stop> stops = getStops(cssStops);
        CycleMethod spreadMethod = getStyledNonNull(SPREAD_METHOD);

        if (stops.size() == 1) {
            return stops.getFirst().getColor();
        }

        return new RadialGradient(0, 0, cx, cy, r, gradientUnits == SvgGradientUnits.OBJECT_BOUNDING_BOX,
                spreadMethod, stops);

    }

    @Override
    public String getTypeSelector() {
        return TYPE_SELECTOR;
    }


}
