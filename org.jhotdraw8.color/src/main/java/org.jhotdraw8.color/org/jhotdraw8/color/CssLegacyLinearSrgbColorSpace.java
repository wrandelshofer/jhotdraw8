package org.jhotdraw8.color;

import javafx.geometry.Point2D;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.color.linalg.Matrix3;
import org.jhotdraw8.color.linalg.Matrix3Float;

/**
 * Linear {@code sRGB} Color Space with components in the range from 0 to 255.
 * <dl>
 *     <dt>Wikipedia: sRGB. Transformation</dt>
 *     <dd><a href="https://en.wikipedia.org/wiki/SRGB#Transformation">wikipedia</a></dd>
 *
 *     <dt>CSS Color Module Level 4. Sample code for Color Conversions.</dt>
 *     <dd><a href="https://www.w3.org/TR/2022/CRD-css-color-4-20221101/#color-conversion-code">w3.org</a></dd>
 * </dl>
 */
public class CssLegacyLinearSrgbColorSpace extends ParametricLinearRgbColorSpace {


    private final static @NonNull Matrix3 TO_XYZ;


    private final static @NonNull Matrix3 FROM_XYZ;

    static {
        Matrix3 M;
        M = ParametricLinearRgbColorSpace.computeToXyzMatrix(new Point2D(0.64, 0.33),
                new Point2D(0.3, 0.6),
                new Point2D(0.15, 0.06),
                ParametricLinearRgbColorSpace.ILLUMINANT_D65);
        TO_XYZ = M.mul(new Matrix3Float(
                255f, 0, 0,
                0, 255f, 0,
                0, 0, 255f

        ));
        FROM_XYZ = new Matrix3Float(
                1 / 255f, 0, 0,
                0, 1 / 255f, 0,
                0, 0, 1 / 255f
        ).mul(M);
    }

    public CssLegacyLinearSrgbColorSpace() {
        super("srgb-linear", TO_XYZ, FROM_XYZ, 0f, 255f);
    }


}
