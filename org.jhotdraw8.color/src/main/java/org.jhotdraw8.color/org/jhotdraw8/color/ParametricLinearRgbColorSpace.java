/*
 * @(#)ParametricLinearRgbColorSpace.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.color;

import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.color.math.Matrix3;
import org.jhotdraw8.color.math.Matrix3Double;

import java.awt.color.ColorSpace;

/**
 * Parameterized linear color space based on {@code XYZ} conversion matrices.
 * <p>
 * The XYZ conversion matrices can be provided or can be computed from
 * red, green, blue and white point chromaticity points.
 * <p>
 * References:
 * <dl>
 *     <dt>C. A. Bouman: Digital Image Processing - January 9, 2023, Chromaticity Coordinates.</dt>
 *     <dd><a href="https://engineering.purdue.edu/~bouman/ece637/notes/pdf/ColorSpaces.pdf">purdue.edu</a></dd>
 *
 *     <dt>CSS Color Module Level 4. Color Terminology. White Point D50</dt>
 *     <dd><a href="https://www.w3.org/TR/2022/CRD-css-color-4-20221101/#d50-whitepoint">w3.org</a></dd>
 *
 *     <dt>CSS Color Module Level 4. Color Terminology. White Point D65</dt>
 *     <dd><a href="https://www.w3.org/TR/2022/CRD-css-color-4-20221101/#d65-whitepoint">w3.org</a></dd>
 *
 * </dl>
 */
public class ParametricLinearRgbColorSpace extends AbstractNamedColorSpace {
    /**
     * The Bradford XYZ to Cone Response Domain Matrix [M<sub>A</sub>].
     * <p>
     * <a href="http://www.brucelindbloom.com/index.html?Eqn_ChromAdapt.html">brucelindbloom.com</a>
     */
    public final static Matrix3Double BRADFORD_XYZ_TO_CONE_RESPONSE_DOMAIN = new Matrix3Double(
            0.8951000, 0.2664000, -0.1614000,
            -0.7502000, 1.7135000, 0.0367000,
            0.0389000, -0.0685000, 1.0296000);
    /**
     * The inverse Bradford XYZ to Cone Response Domain Matrix [M<sub>A</sub>]<sup>-1</sup>.
     * <p>
     * <a href="http://www.brucelindbloom.com/index.html?Eqn_ChromAdapt.html">brucelindbloom.com</a>
     */
    public final static Matrix3Double BRADFORD_CONE_RESPONSE_DOMAIN_TO_XYZ = BRADFORD_XYZ_TO_CONE_RESPONSE_DOMAIN.inv();


    /**
     * Bradford chromatic adaptation from D50 to D65.
     */
    public final static Matrix3Double FROM_D50_XYZ_TO_D65_XYZ = new Matrix3Double(
            0.9554734527042182, -0.023098536874261423, 0.0632593086610217,
            -0.028369706963208136, 1.0099954580058226, 0.021041398966943008,
            0.012314001688319899, -0.020507696433477912, 1.3303659366080753
    );
    /**
     * Bradford chromatic adaptation from D65 to D50
     * The matrix below is the result of three operations:
     * - convert from XYZ to retinal cone domain
     * - scale components from one reference white to another
     * - convert back to XYZ
     * <p>
     * <a href="http://www.brucelindbloom.com/index.html?Eqn_ChromAdapt.html">brucelindbloom.com</a>.
     */
    public static final Matrix3Double FROM_D65_TO_D50 = FROM_D50_XYZ_TO_D65_XYZ.inv();

    /**
     * The XYZ coordinates of the D50 white illuminant.
     */
    public final static @NonNull Point3D ILLUMINANT_D50_XYZ = new Point3D(0.96422, 1.00000, 0.82521);
    /**
     * The XYZ coordinates of the D65 white illuminant.
     */
    public final static @NonNull Point3D ILLUMINANT_D65_XYZ = new Point3D(0.95047, 1.00000, 1.08883);
    /**
     * The XYZ coordinates of the E white illuminant.
     */
    public final static @NonNull Point3D ILLUMINANT_E_XYZ = new Point3D(1.0, 1.0, 1.0);
    /**
     * The XYZ coordinates of the C white illuminant.
     */
    public final static @NonNull Point3D ILLUMINANT_C_XYZ = new Point3D(0.98074, 1.00000, 1.18232);
    /**
     * The chromaticity coordinates (x,y) of the D65 white illuminant.
     */
    public final static @NonNull Point2D ILLUMINANT_D65 = new Point2D(0.3127, 0.3290);

    private static final @NonNull Matrix3Double FROM_LINEAR_SRGB_TO_D65_XYZ_MATRIX = computeToXyzMatrix(new Point2D(0.64, 0.33),
            new Point2D(0.3, 0.6),
            new Point2D(0.15, 0.06),
            ParametricLinearRgbColorSpace.ILLUMINANT_D65);
    private static final @NonNull Matrix3Double FROM_D65_XYZ_TO_LINEAR_SRGB_MATRIX = FROM_LINEAR_SRGB_TO_D65_XYZ_MATRIX.inv();

    /**
     * The matrix for converting from linear RGB to XYZ.
     */
    private final @NonNull Matrix3 toXyzMatrix;
    /**
     * The matrix for converting from XYZ to linear RGB.
     */
    private final @NonNull Matrix3 fromXyzMatrix;
    /**
     * The matrix for converting from linear RGB to sRGB.
     */
    private final @NonNull Matrix3 toLinearSrgbMatrix;
    /**
     * The matrix for converting from sRGB to linear RGB.
     */
    private final @NonNull Matrix3 fromLinearSrgbMatrix;
    /**
     * The name of the color space.
     */
    private final @NonNull String name;
    private final float maxValue;
    private final float minValue;

    /**
     * Creates a new instance.
     *
     * @param name          the name of the color space
     * @param toXyzMatrix   the matrix for conversion to CIE XYZ
     * @param fromXyzMatrix the matrix for conversion from CIE XYZ
     * @param minValue
     * @param maxValue
     */
    public ParametricLinearRgbColorSpace(@NonNull String name,
                                         @NonNull Matrix3 toXyzMatrix,
                                         @NonNull Matrix3 fromXyzMatrix,
                                         float minValue, float maxValue) {
        super(ColorSpace.TYPE_RGB, 3);
        this.name = name;
        this.toXyzMatrix = toXyzMatrix;
        this.fromXyzMatrix = fromXyzMatrix;
        this.toLinearSrgbMatrix = FROM_D65_XYZ_TO_LINEAR_SRGB_MATRIX.mul(toXyzMatrix).toFloat();
        this.fromLinearSrgbMatrix = fromXyzMatrix.toDouble().mul(FROM_LINEAR_SRGB_TO_D65_XYZ_MATRIX).toFloat();
        this.maxValue = maxValue;
        this.minValue = minValue;
    }

    /**
     * Creates a new instance.
     *
     * @param name           the name of the color space
     * @param red            the CIE chroma (x,y) red primary
     * @param green          the CIE chroma (x,y) green primary
     * @param blue           the CIE chroma (x,y) blue primary
     * @param whitePoint_XYZ the white point (XYZ)
     */
    public ParametricLinearRgbColorSpace(@NonNull String name,
                                         @NonNull Point2D red,
                                         @NonNull Point2D green,
                                         @NonNull Point2D blue,
                                         @NonNull Point3D whitePoint_XYZ) {
        super(ColorSpace.TYPE_RGB, 3);
        this.name = name;
        this.minValue = 0;
        this.maxValue = 1;

        @NonNull Point2D whitePoint_xy = convertXYZToxy(whitePoint_XYZ);

        Matrix3Double toXyzMatrixDouble = computeToXyzMatrix(red, green, blue, whitePoint_xy);

        Matrix3Double toD50XyzMatrixDouble;
        Matrix3Double toLinearSrgbMatrixDouble;
        if (!whitePoint_XYZ.equals(ILLUMINANT_D50_XYZ)) {
            Matrix3Double mA = computeChromaticAdaptationMatrix(whitePoint_XYZ, ILLUMINANT_D50_XYZ);
            toD50XyzMatrixDouble = mA.mul(toXyzMatrixDouble);
            toLinearSrgbMatrixDouble = FROM_D65_XYZ_TO_LINEAR_SRGB_MATRIX.mul(toXyzMatrixDouble);
        } else {
            toD50XyzMatrixDouble = toXyzMatrixDouble;
            toLinearSrgbMatrixDouble = FROM_D65_XYZ_TO_LINEAR_SRGB_MATRIX.mul(
                    FROM_D50_XYZ_TO_D65_XYZ).mul(toXyzMatrixDouble);
        }
        this.toXyzMatrix = toD50XyzMatrixDouble;
        this.fromXyzMatrix = toD50XyzMatrixDouble.inv();
        this.toLinearSrgbMatrix = toLinearSrgbMatrixDouble;
        this.fromLinearSrgbMatrix = toLinearSrgbMatrixDouble.inv();
    }

    /**
     * Converts a point from XZY coordinates in to xyY.
     * <pre>
     *     x = X / (X + Y + Z)
     *     y = Y / (X + Y + Z)
     *     z = Z / (X + Y + Z) = 1 - x - y
     *     Y = Y
     * </pre>
     * <p>
     * References:
     * <dl>
     *     <dt>CIE 1931 color space. CIE xy chromaticity diagram and the CIE xyY color space</dt>
     *     <dd><a href="https://en.wikipedia.org/wiki/CIE_1931_color_space#CIE_xy_chromaticity_diagram_and_the_CIE_xyY_color_space>wikipedia.org</a></dd>
     * </dl>
     */
    private Point2D convertXYZToxy(Point3D XYZ) {
        double X = XYZ.getX();
        double Y = XYZ.getY();
        double sum = X + Y + XYZ.getZ();
        return new Point2D(X / sum, Y / sum);
    }

    /**
     * Computes a matrix that converts from a linear RGB color to an XYZ color.
     *
     * @param red        the CIE chroma (x,y) red primary
     * @param green      the CIE chroma (x,y) green primary
     * @param blue       the CIE chroma (x,y) blue primary
     * @param whitePoint the white point (x,y)
     */
    @NonNull
    public static Matrix3Double computeToXyzMatrix(@NonNull Point2D red, @NonNull Point2D green, @NonNull Point2D blue, @NonNull Point2D whitePoint) {
        // matrix M
        // [xr xg xb]
        // [yr yg yb]
        // [zr zg zb]
        Matrix3Double M = new Matrix3Double(
                red.getX(), green.getX(), blue.getX(),
                red.getY(), green.getY(), blue.getY(),
                1 - red.getX() - red.getY(),
                1 - green.getX() - green.getY(),
                1 - blue.getX() - blue.getY());
        Point3D whitePointCorrection = computeWhitePointCorrection(whitePoint);
        double X = whitePointCorrection.getX();
        double Y = whitePointCorrection.getY();
        double Z = whitePointCorrection.getZ();

        // solve for a1,a2,a3
        // X       [a1  0  0]
        // Y = M * [0  a2  0]
        // Z       [0   0 a3]

        Point3D a1a2a3 = M.inv().mul(X, Y, Z);
        Matrix3Double alpha = new Matrix3Double(
                a1a2a3.getX(), 0, 0,
                0, a1a2a3.getY(), 0,
                0, 0, a1a2a3.getZ());

        // M = M * alpha
        M = M.mul(alpha);
        return M;
    }

    @NonNull
    private static Point3D computeWhitePointCorrection(Point3D xyz) {
        return new Point3D(xyz.getX() / xyz.getY(), 1, xyz.getZ() / xyz.getY());
    }

    @NonNull
    private static Point3D computeWhitePointCorrection(Point2D xy) {
        return computeWhitePointCorrection(new Point3D(xy.getX(), xy.getY(),
                1 - xy.getX() - xy.getY()));
    }

    @Override
    public float @NonNull [] fromCIEXYZ(float @NonNull [] xyz, float @NonNull [] colorvalue) {
        return fromXyzMatrix.mul(xyz, colorvalue);
    }

    @Override
    public float @NonNull [] fromRGB(float @NonNull [] rgb, float @NonNull [] colorvalue) {
        return fromLinearSrgbMatrix.mul(LinearSrgbColorSpace.toLinear(rgb, colorvalue), colorvalue);


        // return fromCIEXYZ(SRGB_COLOR_SPACE.toCIEXYZ(rgb, colorvalue), colorvalue);
    }

    public @NonNull Matrix3 getToXyzMatrix() {
        return toXyzMatrix;
    }

    protected @NonNull Matrix3 getFromXyzMatrix() {
        return fromXyzMatrix;
    }

    @Override
    public @NonNull String getName() {
        return name;
    }

    @Override
    public float @NonNull [] toCIEXYZ(float @NonNull [] colorvalue, float @NonNull [] xyz) {
        return toXyzMatrix.mul(colorvalue, xyz);
    }

    @Override
    public float @NonNull [] toRGB(float @NonNull [] colorvalue, float @NonNull [] rgb) {
        return LinearSrgbColorSpace.fromLinear(toLinearSrgbMatrix.mul(colorvalue, rgb), rgb);
        // return SRGB_COLOR_SPACE.fromCIEXYZ(toCIEXYZ(colorvalue, rgb), rgb);
    }

    @Override
    public float getMinValue(int component) {
        return minValue;
    }

    @Override
    public float getMaxValue(int component) {
        return maxValue;
    }

    /**
     * Computes a chromatic adaptation matrix using the Bradford method.
     * <p>
     * The matrix adapts XYZ colors from a source XYZ color space with white point W<sub>src</sub>
     * to another XYZ color space with white point W<sub>dest</sub>.
     * <p>
     * The idea behind all of these algorithms is to follow three steps:
     * <ol>
     *     <li>Transform from XYZ into a cone response domain, (ρ, γ, β)</li>
     *     <li>Scale the vector components by factors dependent upon both the source and destination reference whites.</li>
     *     <li>Transform from (ρ, γ, β) back to XYZ using the inverse transform of step 1.</li>
     * </ol>
     * <code>
     * [M] = [M<sub>A</sub>]<sup>-1</sup> * [S] * [M<sub>A</sub>]
     * </code><br>
     * where [S] is a diagonal matrix:<br>
     * <code>
     * [ ρ<sub>D</sub>/ρ<sub>S</sub>   0      0     ]<br>
     * [ 0     γ<sub>D</sub>/γ<sub>S</sub>    0     ]<br>
     * [ 0      0      β<sub>D</sub>/β<sub>S</sub>  ]<br>
     * </code>
     *
     *
     *
     * <a href="http://www.brucelindbloom.com/index.html?Eqn_ChromAdapt.html">brucelindbloom.com</a>
     *
     * @param wSource the source white point
     * @param wDest   the destination white point
     * @return a matrix that converts XYZ coordinates from source to dest
     */
    public static Matrix3Double computeChromaticAdaptationMatrix(Point3D wSource, Point3D wDest) {
        Point3D coneS = BRADFORD_XYZ_TO_CONE_RESPONSE_DOMAIN.mul(wSource.getX(), wSource.getY(), wSource.getZ());
        Point3D coneD = BRADFORD_XYZ_TO_CONE_RESPONSE_DOMAIN.mul(wDest.getX(), wDest.getY(), wDest.getZ());
        Matrix3Double s = new Matrix3Double(
                coneD.getX() / coneS.getX(), 0, 0,
                0, coneD.getY() / coneS.getY(), 0,
                0, 0, coneD.getZ() / coneS.getZ()
        );
        return BRADFORD_CONE_RESPONSE_DOMAIN_TO_XYZ.mul(s).mul(BRADFORD_XYZ_TO_CONE_RESPONSE_DOMAIN);
    }
}
