package org.jhotdraw8.color;

import javafx.geometry.Point2D;
import org.jhotdraw8.color.linalg.Matrix3;
import org.jhotdraw8.color.linalg.Matrix3Double;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * References:
 * <dl>
 *     <dt>C. A. Bouman: Digital Image Processing - January 9, 2023, Chromacity Coordinates.</dt>
 *     <dd><a href="https://engineering.purdue.edu/~bouman/ece637/notes/pdf/ColorSpaces.pdf">purdue.edu</a></dd>
 * </dl>
 */
public class ParameterizedLinearRgbColorSpaceTest {
    @Test
    public void shouldYieldExpectedMatrixForNtscWithEWhitePoint() {
        ParametricLinearRgbColorSpace cs = new ParametricLinearRgbColorSpace("NTSC EE",
                new Point2D(0.67, 0.33),
                new Point2D(0.21, 0.71),
                new Point2D(0.14, 0.08),
                ParametricLinearRgbColorSpace.ILLUMINANT_E
        );
        Matrix3Double expectedMatrix = new Matrix3Double(
                0.6611, 0.1711, 0.1678,
                0.3256, 0.5785, 0.0959,
                0, 0.0652, 0.9348
        );
        Matrix3 actualMatrix = cs.getToXyzMatrix();
        assertArrayEquals(expectedMatrix.toDoubleArray(), actualMatrix.toDoubleArray(), 1e-4);
    }

    @Test
    public void shouldYieldExpectedMatrixForNtscWithCWhitePoint() {
        ParametricLinearRgbColorSpace cs = new ParametricLinearRgbColorSpace("NTSC C",
                new Point2D(0.67, 0.33),
                new Point2D(0.21, 0.71),
                new Point2D(0.14, 0.08),
                ParametricLinearRgbColorSpace.ILLUMINANT_C
        );
        Matrix3Double expectedMatrix = new Matrix3Double(
                0.6070, 0.1734, 0.2006,
                0.2990, 0.5864, 0.1146,
                0, 0.0661, 1.1175
        );
        Matrix3 actualMatrix = cs.getToXyzMatrix();
        assertArrayEquals(expectedMatrix.toDoubleArray(), actualMatrix.toDoubleArray(), 1e-4);
    }

    @Test
    public void shouldYieldExpectedMatrixForsRGbLinearWithD65WhitePoint() {
        ParametricLinearRgbColorSpace cs = new ParametricLinearRgbColorSpace("sRGB Linear",
                new Point2D(0.64, 0.33),
                new Point2D(0.3, 0.6),
                new Point2D(0.15, 0.06),
                ParametricLinearRgbColorSpace.ILLUMINANT_D65
        );
        Matrix3Double expectedMatrix = new Matrix3Double(
                0.4124, 0.3576, 0.1805,
                0.2126, 0.7152, 0.0722,
                0.0193, 0.1192, 0.9505
        );
        Matrix3 actualMatrix = cs.getToXyzMatrix();

        assertArrayEquals(expectedMatrix.toDoubleArray(), actualMatrix.toDoubleArray(), 1e-4);
    }

}