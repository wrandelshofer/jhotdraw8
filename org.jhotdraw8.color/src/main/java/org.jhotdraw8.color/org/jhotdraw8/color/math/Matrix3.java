package org.jhotdraw8.color.math;

import javafx.geometry.Point3D;

public interface Matrix3 {
    double det();

    Matrix3Float toFloat();

    Matrix3Double toDouble();


    Matrix3 transpose();


    boolean equals(Matrix3 that, double eps);

    double[] toDoubleArray();

    /**
     * Vector multiplication.
     * <pre>
     * x1       [a1]
     * x2 = M * [a2]
     * x3       [a3]
     * </pre>
     */
    Point3D mul(double a1, double a2, double a3);

    /**
     * Vector multiplication.
     * <pre>
     * x1       [a1]
     * x2 = M * [a2]
     * x3       [a3]
     * </pre>
     */
    float[] mul(float[] x, float[] y);

    /**
     * Vector multiplication.
     * <pre>
     * x1       [a1]
     * x2 = M * [a2]
     * x3       [a3]
     * </pre>
     */
    double[] mul(double[] x, double[] y);

    /**
     * Matrix multiplication.
     * <pre>
     * result = this * M*
     * </pre>
     */
    Matrix3 mul(Matrix3 M);

    /**
     * Returns the inverse of the matrix.
     *
     * @throws ArithmeticException if the matrix is not invertible
     */
    Matrix3 inv();

}
