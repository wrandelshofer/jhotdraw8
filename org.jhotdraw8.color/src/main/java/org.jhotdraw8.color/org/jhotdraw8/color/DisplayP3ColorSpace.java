package org.jhotdraw8.color;

import javafx.geometry.Point2D;

import static org.jhotdraw8.color.ParametricLinearRgbColorSpace.ILLUMINANT_D65;

/**
 * Display P3 Color Space.
 * <p>
 * P3 is an RGB color space. DCI-P3 (Digital Cinema Initiative) is used with digital theatrical motion picture
 * distribution. Display P3 is a variant developed by Apple Inc. for wide-gamut displays.
 * <dl>
 *     <dt>Wikipedia: DCI-P3.</dt>
 *     <dd><a href="https://en.wikipedia.org/wiki/DCI-P3">wikipedia</a></dd>
 *
 *     <dt>CSS Color Module Level 4. The Predefined Display P3 Color Space: the display-p3 keyword.</dt>
 *     <dd><a href="https://www.w3.org/TR/2022/CRD-css-color-4-20221101/#predefined-display-p3">w3.org</a></dd>
 *
 *     <dt>CSS Color Module Level 4. Sample code for Color Conversions.</dt>
 *     <dd><a href="https://www.w3.org/TR/2022/CRD-css-color-4-20221101/#color-conversion-code">w3.org</a></dd>
 * </dl>
 */
public class DisplayP3ColorSpace extends ParametricNonLinearRgbColorSpace {

    public DisplayP3ColorSpace() {
        super("Display P3", new ParametricLinearRgbColorSpace("Linear Display P3",
                        new Point2D(0.68, 0.32),
                        new Point2D(0.265, 0.69),
                        new Point2D(0.15, 0.06),
                        ILLUMINANT_D65
                ), SrgbColorSpace::toLinear,
                SrgbColorSpace::fromLinear
        );
    }
}
