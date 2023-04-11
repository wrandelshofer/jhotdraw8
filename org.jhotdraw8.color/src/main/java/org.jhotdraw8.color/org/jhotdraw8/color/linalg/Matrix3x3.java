package org.jhotdraw8.color.linalg;

import javafx.geometry.Point3D;

public record Matrix3x3(double a, double b, double c,
                        double d, double e, double f,
                        double g, double h, double i) {
    public double det() {
        return det(
                a, b, c,
                d, e, f,
                g, h, i);
    }

    public Matrix3x3 transpose() {
        return new Matrix3x3(
                a, d, g,
                b, e, h,
                c, f, i);
    }

    public double[] toArray() {
        return new double[]{a, b, c, d, e, f, g, h, i};
    }

    /**
     * Vector multiplication.
     * <pre>
     * x1       [a1]
     * x2 = M * [a2]
     * x3       [a3]
     * </pre>
     */
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
    public float[] mul(float[] a123, float[] x123) {
        float v = a123[0];
        float w = a123[1];
        float x = a123[2];
        x123[0] = (float) (a * v + b * w + c * x);
        x123[1] = (float) (d * v + e * w + f * x);
        x123[2] = (float) (g * v + h * w + i * x);
        return x123;
    }

    public Matrix3x3 mul(Matrix3x3 M) {
        return new Matrix3x3(
                a * M.a + b * M.d + c * M.g,
                a * M.b + b * M.e + c * M.h,
                a * M.c + b * M.f + c * M.i,

                d * M.a + e * M.d + f * M.g,
                d * M.b + e * M.e + f * M.h,
                d * M.c + e * M.f + f * M.i,

                g * M.a + h * M.d + i * M.g,
                g * M.b + h * M.e + i * M.h,
                g * M.c + h * M.f + i * M.i
        );
    }

    /**
     * References:<br>
     * <a href="https://www.wikihow.com/Find-the-Inverse-of-a-3x3-Matrix">wikihow.com</a><br>
     * <a href="https://www.chilimath.com/lessons/advanced-algebra/determinant-2x2-matrix/">chilimath.com</a>
     */
    public Matrix3x3 inv() {
        double invdet = 1.0 / det();
        double ad = det(e, f, h, i);
        double bd = det(d, f, g, i);
        double cd = det(d, e, g, h);
        double dd = det(b, c, h, i);
        double ed = det(a, c, g, i);
        double fd = det(a, b, g, h);
        double gd = det(b, c, e, f);
        double hd = det(a, c, d, f);
        double id = det(a, b, d, e);
        return new Matrix3x3(
                invdet * ad, invdet * -dd, invdet * gd,
                invdet * -bd, invdet * ed, invdet * -hd,
                invdet * cd, invdet * -fd, invdet * id);
    }

    /*
     * References:<br>
     * <a href="https://www.chilimath.com/lessons/advanced-algebra/determinant-3x3-matrix/">chilimath.com</a>
     */
    private double det(double a, double b, double c,
                       double d, double e, double f,
                       double g, double h, double i) {
        return a * det(e, f, h, i)
                - b * det(d, f, g, i)
                + c * det(d, e, g, h);
    }

    /**
     * References:<br>
     * <a href="https://www.chilimath.com/lessons/advanced-algebra/determinant-2x2-matrix/">chilimath.com</a>
     *
     *
     * <pre>
     *     [a b]
     *     [c d]
     * </pre>
     */
    private double det(double a, double b, double c, double d) {
        return a * d - b * c;
    }
}
