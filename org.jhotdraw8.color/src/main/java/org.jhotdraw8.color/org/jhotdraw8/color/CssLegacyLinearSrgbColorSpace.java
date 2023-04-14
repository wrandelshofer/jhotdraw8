package org.jhotdraw8.color;

import javafx.geometry.Point2D;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.color.math.Matrix3;
import org.jhotdraw8.color.math.Matrix3Double;

import java.util.Arrays;

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
        M = FROM_D65_TO_D50.mul(M);
        TO_XYZ = M.mul(new Matrix3Double(
                1 / 255d, 0, 0,
                0, 1 / 255d, 0,
                0, 0, 1 / 255d

        ));
        FROM_XYZ = new Matrix3Double(
                255d, 0, 0,
                0, 255d, 0,
                0, 0, 255d
        ).mul(M.inv());
    }

    public CssLegacyLinearSrgbColorSpace() {
        super("srgb-linear", TO_XYZ, FROM_XYZ, 0f, 255f);
    }

    @Override
    public float[] toRGB(float[] colorvalue, float[] rgb) {
        for (int i = 0; i < 3; i++) {
            rgb[i] = colorvalue[i] * 1 / 255f;
        }
        return rgb;
    }

    @Override
    public float[] fromRGB(float[] rgb, float[] colorvalue) {
        for (int i = 0; i < 3; i++) {
            colorvalue[i] = rgb[i] * 255f;
        }
        return colorvalue;
    }

    public static void main(String... args) {
        var M = ParametricLinearRgbColorSpace.computeToXyzMatrix(new Point2D(0.64, 0.33),
                new Point2D(0.3, 0.6),
                new Point2D(0.15, 0.06),
                ParametricLinearRgbColorSpace.ILLUMINANT_D65);
        float[] r = {1f, 0, 0};
        System.out.println(Arrays.toString(M.mul(r, new float[3])));
        float[] red = {255f, 0, 0};
        System.out.println(Arrays.toString(M.mul(red, new float[3])));
        System.out.println(Arrays.toString(TO_XYZ.mul(red, new float[3])));
    }

}
