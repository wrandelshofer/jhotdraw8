/*
 * @(#)OKLabColorSpace.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.color;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.color.math.Matrix3Double;

import java.awt.color.ColorSpace;
import java.io.Serial;

/**
 * The OK Lab Color Space.
 * <p>
 * Given a color in XYZ coordinates, with a D65 white point and white as Y=1,
 * OK Lab coordinates can be computed like this:
 * <p>
 * First the XYZ coordinates are converted to an approximate cone responses:
 * <pre>
 *    [ l ]          [ X ]
 *    [ m ] = M_1 ×  [ Y ]
 *    [ s ]          [ Z ]
 * </pre>
 * A non-linearity  is applied
 * <pre>
 *    [ l' ]    [ ∛l ]
 *    [ m' ] =  [ ∛m ]
 *    [ s' ]    [ ∛s ]
 * </pre>
 * Finally, this is transformed into the LabLab-coordinates:
 * <pre>
 *    [ L ]          [ l' ]
 *    [ a ] = M_2 ×  [ m' ]
 *    [ b ]          [ s' ]
 * </pre>
 * With the following values for M1M1 and M2M2:
 * <pre>
 * M1= [ +0.8189330101 +0.3618667424 −0.1288597137 ]
 *     [ +0.0329845436 +0.9293118715 +0.0361456387 ]
 *     [ +0.0482003018 +0.2643662691 +0.6338517070 ]
 *
 *
 * M2 = [ +0.2104542553 +0.7936177850 −0.0040720468 ]
 *      [ +1.9779984951 −2.4285922050 +0.4505937099 ]
 *      [ +0.0259040371 +0.7827717662 −0.8086757660 ]
 *
 * </pre>
 * Note that we have to use a different matrix for M1 when we compute from/to XYZ with a D50 white point.
 * <p>
 * References:
 * <dl>
 *     <dt>Börn Ottosson, A perceptual color space for image processing, Converting from linear sRGB to Oklab/dt>
 *     <dd><a href="https://bottosson.github.io/posts/oklab/#converting-from-linear-srgb-to-oklab">github.io</a></dd>
 * </dl>
 *
 * @author Werner Randelshofer
 */
@SuppressWarnings("UnnecessaryLocalVariable")
public class OKLabColorSpace extends AbstractNamedColorSpace {
    /**
     * Concatenation of RGB_to_XYZ_D65 matrix and the M1 matrix.
     * <p>
     * This matrix computes lms directly from linear sRGB values.
     * <pre>
     *    [ X ]                     [ R ]
     *    [ Y ] = RGB_to_XYZ_D65 ×  [ G ]
     *    [ Z ]                     [ B ]
     * </pre>
     * <pre>
     *    [ l ]          [ X ]
     *    [ m ] = M_1 ×  [ Y ]
     *    [ s ]          [ Z ]
     * </pre>
     */
    private static final @NonNull Matrix3Double M1_RGB = new Matrix3Double(
            0.4122214708, +0.5363325363, +0.0514459929,
            0.2119034982, +0.6806995451, +0.1073969566,
            0.0883024619, +0.2817188376, +0.6299787005
    );
    /**
     * The M2 matrix.
     */
    private static final @NonNull Matrix3Double M2 = new Matrix3Double(
            0.2104542553, 0.7936177850, -0.0040720468,
            1.9779984951, -2.4285922050, +0.4505937099,
            0.0259040371, +0.7827717662, -0.8086757660
    );
    /**
     * The inverse of the M2 matrix.
     */
    private static final @NonNull Matrix3Double M2_INV = new Matrix3Double(
            1, +0.3963377774, +0.2158037573,
            1, -0.1055613458, -0.0638541728,
            1, -0.0894841775, -1.2914855480
    );
    /**
     * Concatenation of M<sup>-1</sup> and RGB<sup>-1</sup> matrix.
     * <p>
     * This matrix computes sRGB values directly from lms values.
     * <pre>
     *    [ X ]            [ l ]
     *    [ Y ] = M_1^-1 ×  [ m ]
     *    [ Z ]            [ s ]
     * </pre>
     * <pre>
     *    [ R ]                        [ X ]
     *    [ G ] = RGB_to_XYZ_D65^-1 ×  [ Y ]
     *    [ B ]                        [ Z ]
     * </pre>
     */
    private static final @NonNull Matrix3Double RGB_INV_M1_INV = new Matrix3Double(
            4.0767416621, -3.3077115913, +0.2309699292,
            -1.2684380046, +2.6097574011, -0.3413193965,
            -0.0041960863, -0.7034186147, +1.7076147010
    );
    private static @NonNull
    final NamedColorSpace linearSrgb = new SrgbColorSpace().getLinearColorSpace();
    @Serial
    private static final long serialVersionUID = 1L;

    public OKLabColorSpace() {
        super(ColorSpace.TYPE_Lab, 3);

    }


    @Override
    public float @NonNull [] fromCIEXYZ(float @NonNull [] xyz, float @NonNull [] colorvalue) {
        return fromLinearRGB(linearSrgb.fromCIEXYZ(xyz, colorvalue), colorvalue);
    }

    protected float[] fromLinear(float linear[], float corrected[]) {
        corrected[0] = SrgbColorSpace.fromLinear(linear[0]);
        corrected[1] = SrgbColorSpace.fromLinear(linear[1]);
        corrected[2] = SrgbColorSpace.fromLinear(linear[2]);
        return corrected;
    }

    public float[] fromLinearRGB(float[] rgb, float[] lab) {
        // Convert from linear sRGB to approximate cone responses
        float[] lms = M1_RGB.mul(rgb, lab);

        // Apply non-linearity
        float[] lms_ = lms;
        lms_[0] = (float) Math.cbrt(lms[0]);
        lms_[1] = (float) Math.cbrt(lms[1]);
        lms_[2] = (float) Math.cbrt(lms[2]);

        // Convert transformed cone responses to lab
        return M2.mul(lms_, lab);
    }

    @Override
    public float @NonNull [] fromRGB(float @NonNull [] srgbvalue, float @NonNull [] colorvalue) {
        return fromLinearRGB(toLinear(srgbvalue, colorvalue), colorvalue);
    }

    @Override
    public float getMaxValue(int component) {
        switch (component) {
            case 0:
                return 1f;
            case 1:
            case 2:
                return 0.4f;
        }
        throw new IllegalArgumentException("Illegal component:" + component);
    }

    @Override
    public float getMinValue(int component) {
        switch (component) {
            case 0:
                return 0f;
            case 1:
            case 2:
                return -0.4f;
        }
        throw new IllegalArgumentException("Illegal component:" + component);
    }

    @Override
    public @NonNull String getName() {
        return "OKLAB";
    }

    @Override
    public float @NonNull [] toCIEXYZ(float @NonNull [] colorvalue, float @NonNull [] xyz) {
        return linearSrgb.toCIEXYZ(toLinearRGB(colorvalue, xyz), xyz);
    }

    protected float[] toLinear(float corrected[], float linear[]) {
        linear[0] = SrgbColorSpace.toLinear(corrected[0]);
        linear[1] = SrgbColorSpace.toLinear(corrected[1]);
        linear[2] = SrgbColorSpace.toLinear(corrected[2]);
        return linear;
    }

    protected float[] toLinearRGB(float[] lab, float[] rgb) {
        // Convert from lab to transformed cone responses
        float[] lms_ = M2_INV.mul(lab, rgb);

        // Unapply non-linearity
        float[] lms = lms_;
        float l_ = lms_[0];
        float m_ = lms_[1];
        float s_ = lms_[2];
        lms[0] = l_ * l_ * l_;
        lms[1] = m_ * m_ * m_;
        lms[2] = s_ * s_ * s_;

        // Convert from approximate cone responses to linear sRGB
        return RGB_INV_M1_INV.mul(lms, rgb);


    }

    @Override
    public float @NonNull [] toRGB(float @NonNull [] colorvalue, float @NonNull [] rgb) {
        return fromLinear(toLinearRGB(colorvalue, rgb), rgb);
    }
}
