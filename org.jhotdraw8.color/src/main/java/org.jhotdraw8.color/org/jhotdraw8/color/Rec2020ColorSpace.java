package org.jhotdraw8.color;

import javafx.geometry.Point2D;

import static org.jhotdraw8.color.ParametricLinearRgbColorSpace.ILLUMINANT_D65_XYZ;

/**
 * Rec. 2020 Color Space.
 * <p>
 * The Rec. 2020 color space is used for Ultra High Definition, 4k and 8k television.
 * <dl>
 *     <dt>Wikipedia: Rec. 2020.</dt>
 *     <dd><a href="https://en.wikipedia.org/wiki/Rec._2020">wikipedia</a></dd>
 *
 *     <dt>CSS Color Module Level 4.  The Predefined ITU-R BT.2020-2 Color Space: the rec2020 keyword.</dt>
 *     <dd><a href="https://www.w3.org/TR/2022/CRD-css-color-4-20221101/#predefined-rec2020">w3.org</a></dd>
 *
 *     <dt>CSS Color Module Level 4. Sample code for Color Conversions.</dt>
 *     <dd><a href="https://www.w3.org/TR/2022/CRD-css-color-4-20221101/#color-conversion-code">w3.org</a></dd>
 * </dl>
 */
public class Rec2020ColorSpace extends ParametricNonLinearRgbColorSpace {

    public Rec2020ColorSpace() {
        super("Rec 2020", new ParametricLinearRgbColorSpace("Linear Rec 2020",
                        new Point2D(0.708, 0.292),
                        new Point2D(0.170, 0.797),
                        new Point2D(0.131, 0.046),
                        ILLUMINANT_D65_XYZ
                ),
                Rec2020ColorSpace::toLinear, Rec2020ColorSpace::fromLinear
        );
    }

    /**
     * Convert an array of linear-light rec2020 RGB  in the range 0.0-1.0
     * to gamma corrected form.
     * ITU-R BT.2020-2 p.4
     */
    public static float fromLinear(float linear) {
        float α = 1.09929682680944f;
        float β = 0.018053968510807f;

        float sign = Math.signum(linear);
        float abs = Math.abs(linear);

        float c;
        //if (abs < β / 4.5f) {
        if (abs < β) {
            c = linear * 4.5f;
        } else {
            c = sign * ((float) Math.pow(abs, 0.45f) * α - α + 1);
        }
        return c;
    }

    /**
     * Convert an array of rec2020 RGB values in the range 0.0 - 1.0
     * to linear light (un-companded) form.
     * ITU-R BT.2020-2 p.4
     */
    public static float toLinear(float nonlinear) {
        float α = 1.09929682680944f;
        float β = 0.018053968510807f;

        float sign = Math.signum(nonlinear);
        float abs = Math.abs(nonlinear);

        float cl;
        if (abs < β * 4.5f) {
            cl = nonlinear / 4.5f;
        } else {
            cl = sign * ((float) Math.pow((abs + α - 1) / α, 1 / 0.45f));
        }
        return cl;
    }
}
