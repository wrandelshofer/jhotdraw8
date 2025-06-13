/*
 * @(#)UnitConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.value;


import java.util.Objects;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * UnitConverter.
 * <p>
 * References:
 * <dl>
 *     <dt>CSS Values and Units Module Level 3. § 5.1.1 Font-relative lengths: the em, ex, ch, rem units</dt>
 *     <dd><a href="https://www.w3.org/TR/css3-values/#font-relative-lengths">w3.org</a></dd>
 *     <dt>CSS Values and Units Module Level 3. § 5.1.2 Viewport-percentage lengths: the vw, vh, vmin, vmax units</dt>
 *     <dd><a href="https://www.w3.org/TR/css3-values/#viewport-relative-lengths">w3.org</a></dd>
 *     <dt>CSS Values and Units Module Level 3. § 5.2 Absolute lengths: the cm, mm, Q, in, pt, pc, px units</dt>
 *     <dd><a href="https://www.w3.org/TR/css3-values/#absolute-length">w3.org</a></dd>
 * </dl>
 */
public interface UnitConverter {
    /**
     * Default unit.
     */
    String DEFAULT = "";
    String CENTIMETERS = "cm";
    String DEGREES = "deg";
    String EM = "em";
    String EX = "ex";
    String INCH = "in";
    String MILLIMETERS = "mm";
    String QUARTER_MILLIMETERS = "Q";
    String PERCENTAGE = "%";
    String PICAS = "pc";
    String PIXELS = "px";
    String POINTS = "pt";
    String VIEWPORT_WIDTH_PERCENTAGE = "vw";
    String VIEWPORT_HEIGHT_PERCENTAGE = "vh";
    String VIEWPORT_MIN_PERCENTAGE = "vmin";
    String VIEWPORT_MAX_PERCENTAGE = "vmax";

    /**
     * Gets the resolution in dots per inch.
     *
     * @return dpi, default value: 96.0.
     */
    default double getDpi() {
        return 96.0;
    }

    /**
     * Gets the viewport width.
     *
     * @return viewport width, default value: 1000.0.
     */
    default double getViewportWidth() {
        return 1000;
    }

    /**
     * Gets the viewport height.
     *
     * @return viewport height, default value: 1000.0.
     */
    default double getViewportHeight() {
        return 1000.0;
    }

    /**
     * Gets the factor for percentage values.
     *
     * @return percentageFactor, for example 100.
     */
    default double getPercentageFactor() {
        return 100.0;
    }

    default double getFactor(String unit) {
        final double factor = switch (unit) {
            case PERCENTAGE -> getPercentageFactor();
            case CENTIMETERS -> 2.54 / getDpi();
            case MILLIMETERS -> 25.4 / getDpi();
            case QUARTER_MILLIMETERS -> 25.4 * 0.25 / getDpi();
            case INCH -> 1.0 / getDpi();
            case POINTS -> 72 / getDpi();
            case PICAS -> 72 * 12.0 / getDpi();
            case EM -> 1.0 / getFontSize();
            case EX -> 1.0 / getFontXHeight();
            case VIEWPORT_HEIGHT_PERCENTAGE -> 100.0 / getViewportHeight();
            case VIEWPORT_WIDTH_PERCENTAGE -> 100.0 / getViewportWidth();
            case VIEWPORT_MIN_PERCENTAGE -> 100.0 / min(getViewportHeight(), getViewportWidth());
            case VIEWPORT_MAX_PERCENTAGE -> 100.0 / max(getViewportHeight(), getViewportWidth());
            default -> 1.0;
        };
        return factor;
    }

    /**
     * Gets the font size;
     *
     * @return em
     */
    default double getFontSize() {
        return 12;
    }

    /**
     * Gets the x-height of the font size.
     *
     * @return ex
     */
    default double getFontXHeight() {
        return 8;
    }

    /**
     * Converts the specified value from input unit to output unit.
     *
     * @param value      a value
     * @param inputUnit  the units of the value
     * @param outputUnit the desired output unit
     * @return converted value
     */
    default double convert(double value, String inputUnit, String outputUnit) {
        if (value == 0.0 || Objects.equals(inputUnit, outputUnit)) {
            return value;
        }

        return value * getFactor(outputUnit) / getFactor(inputUnit);
    }

    default CssSize convertSize(double value, String inputUnit, String outputUnit) {
        return CssSize.of(convert(value, inputUnit, outputUnit), outputUnit);
    }

    /**
     * Converts the specified value from input unit to output unit.
     *
     * @param value      a value
     * @param outputUnit the desired output unit
     * @return converted value
     */
    default double convert(CssSize value, String outputUnit) {
        return convert(value.getValue(), value.getUnits(), outputUnit);
    }

    default CssSize convertSize(CssSize value, String outputUnit) {
        return CssSize.of(convert(value.getValue(), value.getUnits(), outputUnit), outputUnit);
    }


}
