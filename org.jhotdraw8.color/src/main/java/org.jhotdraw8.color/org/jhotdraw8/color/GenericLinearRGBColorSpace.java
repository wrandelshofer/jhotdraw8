package org.jhotdraw8.color;

import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.color.linalg.Matrix3x3;

import java.awt.color.ColorSpace;

/**
 * Generic linear color space based on red, green, blue and a white point.
 * <p>
 * References:
 * <dl>
 *     <dt>C. A. Bouman: Digital Image Processing - January 9, 2023, Chromacity Coordinates.</dt>
 *     <dd><a href="https://engineering.purdue.edu/~bouman/ece637/notes/pdf/ColorSpaces.pdf">purdue.edu</a></dd>
 * </dl>
 */
public class GenericLinearRGBColorSpace extends AbstractNamedColorSpace {

    /**
     * C illuminant (x,y) (specified for NTSC).
     */
    public final static Point2D C_ILLUMINANT = new Point2D(0.310, 0.316);
    /**
     * D65 illuminant (x,y) (specified for PAL).
     */
    public final static Point2D D65_ILLUMINANT = new Point2D(0.3127, 0.3290);
    /**
     * Equal energy white illuminant (x,y).
     */
    public final static Point2D EE_ILLUMINANT = new Point2D(1d / 3, 1d / 3);
    /**
     * The matrix for converting from linear RGB to XYZ.
     */
    private final @NonNull Matrix3x3 M;
    /**
     * The matrix for converting from XYZ to linear RGB.
     */
    private final @NonNull Matrix3x3 MInv;
    /**
     * The name of the color space.
     */
    private final @NonNull String name;

    /**
     * Creates a new instance.
     *
     * @param name       the name of the color space
     * @param red        the CIE chroma (x,y) red primary
     * @param green      the CIE chroma (x,y) green primary
     * @param blue       the CIE chroma (x,y) blue primary
     * @param whitePoint the white point (x,y)
     */
    public GenericLinearRGBColorSpace(@NonNull String name,
                                      @NonNull Point2D red,
                                      @NonNull Point2D green,
                                      @NonNull Point2D blue,
                                      @NonNull Point2D whitePoint
    ) {
        super(ColorSpace.TYPE_RGB, 3);
        this.name = name;

        // matrix M
        // [xr xg xb]
        // [yr yg yb]
        // [zr zg zb]
        Matrix3x3 M = new Matrix3x3(
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
        Matrix3x3 alpha = new Matrix3x3(
                a1a2a3.getX(), 0, 0,
                0, a1a2a3.getY(), 0,
                0, 0, a1a2a3.getZ());

        // M = M * alpha
        M = M.mul(alpha);

        this.M = M;
        this.MInv = M.inv();
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
        return MInv.mul(xyz, colorvalue);
    }

    @Override
    public float[] fromRGB(float[] rgb, float[] colorvalue) {
        return fromCIEXYZ(SrgbColorSpace.getInstance().toCIEXYZ(rgb, colorvalue), colorvalue);
    }

    public Matrix3x3 getM() {
        return M;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public float[] toCIEXYZ(float[] colorvalue, float[] xyz) {
        return M.mul(colorvalue, xyz);
    }

    @Override
    public float[] toRGB(float[] colorvalue, float[] rgb) {
        return SrgbColorSpace.getInstance().fromCIEXYZ(toCIEXYZ(colorvalue, rgb), rgb);
    }
}
