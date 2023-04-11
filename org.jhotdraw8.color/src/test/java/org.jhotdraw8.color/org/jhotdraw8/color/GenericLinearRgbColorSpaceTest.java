package org.jhotdraw8.color;

import javafx.geometry.Point2D;
import org.jhotdraw8.color.linalg.Matrix3x3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * References:
 * <dl>
 *     <dt>C. A. Bouman: Digital Image Processing - January 9, 2023, Chromacity Coordinates.</dt>
 *     <dd><a href="https://engineering.purdue.edu/~bouman/ece637/notes/pdf/ColorSpaces.pdf">purdue.edu</a></dd>
 * </dl>
 */
public class GenericLinearRgbColorSpaceTest {
    @Test
    public void shouldYieldExpectedMatrixForNtscWithEEWhitePoint() {
        GenericLinearRGBColorSpace ntsc_ee = new GenericLinearRGBColorSpace("NTSC EE",
                new Point2D(0.67, 0.33),
                new Point2D(0.21, 0.71),
                new Point2D(0.14, 0.08),
                GenericLinearRGBColorSpace.EE_ILLUMINANT
        );
        Matrix3x3 expectedMatrix = new Matrix3x3(
                0.6611, 0.1711, 0.1678,
                0.3256, 0.5785, 0.0959,
                0, 0.0652, 0.9348
        );
        Matrix3x3 actualMatrix = ntsc_ee.getM();

        assertArrayEquals(expectedMatrix.toArray(), actualMatrix.toArray(), 1e-4);
    }

    @Test
    public void shouldYieldExpectedMatrixForNtscWithCWhitePoint() {
        GenericLinearRGBColorSpace ntsc_ee = new GenericLinearRGBColorSpace("NTSC C",
                new Point2D(0.67, 0.33),
                new Point2D(0.21, 0.71),
                new Point2D(0.14, 0.08),
                GenericLinearRGBColorSpace.C_ILLUMINANT
        );
        Matrix3x3 expectedMatrix = new Matrix3x3(
                0.6070, 0.1734, 0.2006,
                0.2990, 0.5864, 0.1146,
                0, 0.0661, 1.1175
        );
        Matrix3x3 actualMatrix = ntsc_ee.getM();

        assertArrayEquals(expectedMatrix.toArray(), actualMatrix.toArray(), 1e-4);
    }

    @Test
    public void shouldYieldExpectedMatrixForsRGbLinearWithD65WhitePoint() {
        GenericLinearRGBColorSpace ntsc_ee = new GenericLinearRGBColorSpace("sRGB Linear",
                new Point2D(0.64, 0.33),
                new Point2D(0.3, 0.6),
                new Point2D(0.15, 0.06),
                GenericLinearRGBColorSpace.D65_ILLUMINANT
        );
        Matrix3x3 expectedMatrix = new Matrix3x3(
                0.4124, 0.3576, 0.1805,
                0.2126, 0.7152, 0.0722,
                0.0193, 0.1192, 0.9505
        );
        Matrix3x3 actualMatrix = ntsc_ee.getM();

        assertArrayEquals(expectedMatrix.toArray(), actualMatrix.toArray(), 1e-4);
    }
}