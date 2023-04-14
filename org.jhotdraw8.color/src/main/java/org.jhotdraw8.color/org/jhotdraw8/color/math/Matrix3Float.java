package org.jhotdraw8.color.math;

import javafx.geometry.Point3D;

import static org.jhotdraw8.color.util.MathUtil.almostEqual;

/**
 * A 3x3 matrix with float precision.
 * <p>
 * This matrix is intended for use in performance-critical code.
 *
 * @param a row 0 column 0
 * @param b row 0 column 1
 * @param c row 0 column 2
 * @param d row 1 column 0
 * @param e row 1 column 1
 * @param f row 1 column 2
 * @param g row 2 column 0
 * @param h row 2 column 1
 * @param i row 2 column 2
 */
public record Matrix3Float(float a, float b, float c,
                           float d, float e, float f,
                           float g, float h, float i) implements Matrix3 {
    public double det() {
        return toDouble().det();
    }

    @Override
    public Matrix3Float toFloat() {
        return this;
    }

    public Matrix3Float transpose() {
        return new Matrix3Float(
                a, d, g,
                b, e, h,
                c, f, i);
    }

    @Override
    public boolean equals(Matrix3 M, double eps) {
        Matrix3Float that = M.toFloat();
        return almostEqual(a, that.a, eps)
                && almostEqual(b, that.b, eps)
                && almostEqual(c, that.c, eps)
                && almostEqual(d, that.d, eps)
                && almostEqual(e, that.e, eps)
                && almostEqual(f, that.f, eps)
                && almostEqual(g, that.g, eps)
                && almostEqual(h, that.h, eps)
                && almostEqual(i, that.i, eps)
                ;
    }

    public double[] toDoubleArray() {
        return new double[]{a, b, c, d, e, f, g, h, i};
    }

    @Override
    public Point3D mul(double a1, double a2, double a3) {
        return new Point3D(
                a * a1 + b * a2 + c * a3,
                d * a1 + e * a2 + f * a3,
                g * a1 + h * a2 + i * a3
        );
    }

    /**
     * Vector multiplication.
     * <pre>
     * x1       [a1]
     * x2 = M * [a2]
     * x3       [a3]
     * </pre>
     */
    public Point3D mul(float a1, float a2, float a3) {
        return new Point3D(
                a * a1 + b * a2 + c * a3,
                d * a1 + e * a2 + f * a3,
                g * a1 + h * a2 + i * a3
        );
    }

    /**
     * Vector multiplication.
     * <pre>
     * x1       [a1]
     * x2 = M * [a2]
     * x3       [a3]
     * </pre>
     */
    public float[] mul(float[] x, float[] y) {
        float x0 = x[0];
        float x1 = x[1];
        float x2 = x[2];
        y[0] = a * x0 + b * x1 + c * x2;
        y[1] = d * x0 + e * x1 + f * x2;
        y[2] = g * x0 + h * x1 + i * x2;
        return y;
    }

    @Override
    public double[] mul(double[] x, double[] y) {
        double x0 = x[0];
        double x1 = x[1];
        double x2 = x[2];
        y[0] = a * x0 + b * x1 + c * x2;
        y[1] = d * x0 + e * x1 + f * x2;
        y[2] = g * x0 + h * x1 + i * x2;
        return y;
    }


    public Matrix3Float mul(Matrix3 M) {
        Matrix3Float that = M.toFloat();
        return new Matrix3Float(
                a * that.a + b * that.d + c * that.g,
                a * that.b + b * that.e + c * that.h,
                a * that.c + b * that.f + c * that.i,

                d * that.a + e * that.d + f * that.g,
                d * that.b + e * that.e + f * that.h,
                d * that.c + e * that.f + f * that.i,

                g * that.a + h * that.d + i * that.g,
                g * that.b + h * that.e + i * that.h,
                g * that.c + h * that.f + i * that.i
        );
    }

    public Matrix3Double toDouble() {
        return new Matrix3Double(a, b, c, d, e, f, g, h, i);
    }

    /**
     * Inverses the matrix. The inversion is performed in double precision.
     * <p>
     * References:<br>
     * <a href="https://www.wikihow.com/Find-the-Inverse-of-a-3x3-Matrix">wikihow.com</a><br>
     * <a href="https://www.chilimath.com/lessons/advanced-algebra/determinant-2x2-matrix/">chilimath.com</a>
     */
    public Matrix3Float inv() {
        return toDouble().inv().toFloat();
    }
}
