package org.jhotdraw8.color.math;

public class Scalars {
    /**
     * Absolute threshold to be used for comparing reals generally.
     */
    public static final double REAL_THRESHOLD = 1e-8;

    /**
     * Don't let anyone instantiate this class.
     */
    private Scalars() {
    }

    public static boolean almostEqual(double a, double b) {
        return almostEqual(a, b, REAL_THRESHOLD);
    }

    public static boolean almostEqual(double a, double b, double epsilon) {
        return Math.abs(a - b) < epsilon;
    }

    public static boolean almostZero(double a) {
        return almostZero(a, REAL_THRESHOLD);
    }

    public static boolean almostZero(double a, double epsilon) {
        return Math.abs(a) < epsilon;
    }


    /**
     * Returns {@code abs(a - b) < eps ? b : a}
     *
     * @param a   value 'a'
     * @param b   value 'b'
     * @param eps epsilon value
     * @return value 'a' or 'b'
     */
    public static float approximate(float a, float b, float eps) {
        return Math.abs(a - b) < eps ? b : a;
    }

    /**
     * Returns {@code abs(a - b) < eps ? b : a}
     *
     * @param a   value 'a'
     * @param b   value 'b'
     * @param eps epsilon value
     * @return value 'a' or 'b'
     */
    public static double approximate(double a, double b, double eps) {
        return Math.abs(a - b) < eps ? b : a;
    }
}
