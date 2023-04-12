package org.jhotdraw8.color;

import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.color.linalg.Matrix3;
import org.jhotdraw8.color.linalg.Matrix3Double;

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
     * The chromaticity coordinates (x,y) of the C white illuminant.
     */
    public final static @NonNull Point2D ILLUMINANT_C = new Point2D(0.310, 0.316);
    /**
     * The chromaticity coordinates (x,y) of the D50 white illuminant.
     */
    public final static @NonNull Point2D ILLUMINANT_D50 = new Point2D(0.3457, 0.3585);
    /**
     * The chromaticity coordinates (x,y) of the D65 white illuminant.
     */
    public final static @NonNull Point2D ILLUMINANT_D65 = new Point2D(0.3127, 0.3290);
    /**
     * The chromaticity coordinates (x,y) of the equal energy white illuminant.
     */
    public final static @NonNull Point2D ILLUMINANT_E = new Point2D(1d / 3, 1d / 3);
    private static final @NonNull Matrix3Double LINEAR_SRGB_TO_XYZ_MATRIX = computeToXyzMatrix(new Point2D(0.64, 0.33),
            new Point2D(0.3, 0.6),
            new Point2D(0.15, 0.06),
            ParametricLinearRgbColorSpace.ILLUMINANT_D65);
    private static final @NonNull Matrix3Double XYZ_TO_LINEAR_SRGB_MATRIX = LINEAR_SRGB_TO_XYZ_MATRIX.inv();

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

    /**
     * Creates a new instance.
     *
     * @param name          the name of the color space
     * @param toXyzMatrix   the matrix for conversion to CIE XYZ
     * @param fromXyzMatrix the matrix for conversion from CIE XYZ
     */
    public ParametricLinearRgbColorSpace(@NonNull String name,
                                         @NonNull Matrix3 toXyzMatrix,
                                         @NonNull Matrix3 fromXyzMatrix
    ) {
        super(ColorSpace.TYPE_RGB, 3);
        this.name = name;
        this.toXyzMatrix = toXyzMatrix;
        this.fromXyzMatrix = fromXyzMatrix;
        this.toLinearSrgbMatrix = XYZ_TO_LINEAR_SRGB_MATRIX.mul(toXyzMatrix).toFloat();
        this.fromLinearSrgbMatrix = fromXyzMatrix.toDouble().mul(LINEAR_SRGB_TO_XYZ_MATRIX).toFloat();
    }

    /**
     * Creates a new instance.
     *
     * @param name       the name of the color space
     * @param red        the CIE chroma (x,y) red primary
     * @param green      the CIE chroma (x,y) green primary
     * @param blue       the CIE chroma (x,y) blue primary
     * @param whitePoint the white point (x,y)
     */
    public ParametricLinearRgbColorSpace(@NonNull String name,
                                         @NonNull Point2D red,
                                         @NonNull Point2D green,
                                         @NonNull Point2D blue,
                                         @NonNull Point2D whitePoint
    ) {
        super(ColorSpace.TYPE_RGB, 3);
        this.name = name;

        Matrix3Double toXyzMatrixDouble = computeToXyzMatrix(red, green, blue, whitePoint);
        Matrix3Double fromXyzMatrixDouble = toXyzMatrixDouble.inv();

        this.toXyzMatrix = toXyzMatrixDouble.toFloat();
        this.fromXyzMatrix = fromXyzMatrixDouble.toFloat();
        this.toLinearSrgbMatrix = XYZ_TO_LINEAR_SRGB_MATRIX.mul(toXyzMatrixDouble).toFloat();
        this.fromLinearSrgbMatrix = fromXyzMatrixDouble.toDouble().mul(LINEAR_SRGB_TO_XYZ_MATRIX).toFloat();
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
    private static Matrix3Double computeToXyzMatrix(@NonNull Point2D red, @NonNull Point2D green, @NonNull Point2D blue, @NonNull Point2D whitePoint) {
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
    public float[] fromCIEXYZ(float[] xyz, float[] colorvalue) {
        return fromXyzMatrix.mul(xyz, colorvalue);
    }

    @Override
    public float[] fromRGB(float[] rgb, float[] colorvalue) {
        return fromLinearSrgbMatrix.mul(LinearSrgbColorSpace.toLinear(rgb, colorvalue), colorvalue);


        // return fromCIEXYZ(SRGB_COLOR_SPACE.toCIEXYZ(rgb, colorvalue), colorvalue);
    }

    public Matrix3 getToXyzMatrix() {
        return toXyzMatrix;
    }

    protected Matrix3 getFromXyzMatrix() {
        return fromXyzMatrix;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public float[] toCIEXYZ(float[] colorvalue, float[] xyz) {
        return toXyzMatrix.mul(colorvalue, xyz);
    }

    @Override
    public float[] toRGB(float[] colorvalue, float[] rgb) {
        return LinearSrgbColorSpace.fromLinear(toLinearSrgbMatrix.mul(colorvalue, rgb), rgb);
        // return SRGB_COLOR_SPACE.fromCIEXYZ(toCIEXYZ(colorvalue, rgb), rgb);
    }
}
