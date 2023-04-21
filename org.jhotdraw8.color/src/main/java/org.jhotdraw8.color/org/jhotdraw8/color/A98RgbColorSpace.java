/*
 * @(#)A98RgbColorSpace.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.color;

import javafx.geometry.Point2D;

import static org.jhotdraw8.color.ParametricLinearRgbColorSpace.ILLUMINANT_D65_XYZ;

/**
 * A98 RGB Color Space.
 * <p>
 * The Adobe RGB (1998) color space or opRGB is a color space developed by Adobe Inc. in 1998.
 * It was designed to encompass most of the colors achievable on CMYK color printers, but by using RGB primary colors on
 * a device such as a computer display.
 * <p>
 * References:
 * <dl>
 *     <dt>Wikipedia: Adobe RGB color space.</dt>
 *     <dd><a href="https://en.wikipedia.org/wiki/Adobe_RGB_color_space">wikipedia</a></dd>
 *
 *     <dt>CSS Color Module Level 4. The Predefined A98 RGB Color Space: the a98-rgb keyword.</dt>
 *     <dd><a href="https://www.w3.org/TR/2022/CRD-css-color-4-20221101/#predefined-a98-rgb3">w3.org</a></dd>
 * </dl>
 */
public class A98RgbColorSpace extends ParametricNonLinearRgbColorSpace {

    public A98RgbColorSpace() {
        super("A98 RGB", new ParametricLinearRgbColorSpace("Linear A98 RGB",
                new Point2D(0.64, 0.33),
                new Point2D(0.21, 0.71),
                new Point2D(0.15, 0.06),
                ILLUMINANT_D65_XYZ
        ), A98RgbColorSpace::toLinear, A98RgbColorSpace::fromLinear);
    }

    /**
     * Convert an array of a98-rgb values in the range 0.0 - 1.0
     * to linear light (un-companded) form.
     * Negative values are also now accepted.
     */
    public static float fromLinear(float val) {
        float sign = val < 0 ? -1 : 1;
        float abs = Math.abs(val);

        return sign * (float) Math.pow(abs, 563 / 256f);
    }

    /**
     * Convert an array of linear-light a98-rgb  in the range 0.0-1.0
     * to gamma corrected form.
     * Negative values are also now accepted.
     */
    public static float toLinear(float val) {
        float sign = val < 0 ? -1 : 1;
        float abs = Math.abs(val);

        return sign * (float) Math.pow(abs, 256 / 563f);
    }
}
