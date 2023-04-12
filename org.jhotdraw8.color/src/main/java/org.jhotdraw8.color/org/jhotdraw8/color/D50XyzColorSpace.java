package org.jhotdraw8.color;

import org.jhotdraw8.color.linalg.Matrix3Double;

/**
 * CIE XYZ Color Space.
 * <p>
 * The xyz color space accepts three numeric parameters, representing the X,Y and Z values.
 * <dl>
 *     <dt>CSS Color Module Level 4. The Predefined CIE XYZ Color Spaces: the xyz-d50, xyz-d65, and xyz keywords.</dt>
 *     <dd><a href="https://www.w3.org/TR/2022/CRD-css-color-4-20221101/#predefined-xyz">w3.org</a></dd>
 *
 *     <dt>CSS Color Module Level 4. Sample code for Color Conversions.</dt>
 *     <dd><a href="https://www.w3.org/TR/2022/CRD-css-color-4-20221101/#color-conversion-code">w3.org</a></dd>
 * </dl>
 */
public class D50XyzColorSpace extends ParametricXyzColorSpace {
    public D50XyzColorSpace() {
        super("D50 XYZ",
                // Bradford chromatic adaptation from D50 to D65
                new Matrix3Double(
                        0.9554734527042182, -0.023098536874261423, 0.0632593086610217,
                        -0.028369706963208136, 1.0099954580058226, 0.021041398966943008,
                        0.012314001688319899, -0.020507696433477912, 1.3303659366080753
                ).toFloat(),

                // Bradford chromatic adaptation from D65 to D50
                // The matrix below is the result of three operations:
                // - convert from XYZ to retinal cone domain
                // - scale components from one reference white to another
                // - convert back to XYZ
                // http://www.brucelindbloom.com/index.html?Eqn_ChromAdapt.html
                new Matrix3Double(
                        1.0479298208405488, 0.022946793341019088, -0.05019222954313557,
                        0.029627815688159344, 0.990434484573249, -0.01707382502938514,
                        -0.009243058152591178, 0.015055144896577895, 0.7518742899580008
                ).toFloat());
    }
}
