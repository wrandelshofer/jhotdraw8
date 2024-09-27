package org.jhotdraw8.geom;

public class Scalars {
    /**
     * Don't let anyone instantiate this class.
     */
    private Scalars() {
    }

    public static boolean almostEqual(double a, double b) {
        return almostEqual(a, b, Rectangles.REAL_THRESHOLD);
    }

    public static boolean almostEqual(double a, double b, double epsilon) {
        return Math.abs(a - b) < epsilon;
    }

    public static boolean almostZero(double a) {
        return almostZero(a, Rectangles.REAL_THRESHOLD);
    }

    public static boolean almostZero(double a, double epsilon) {
        return Math.abs(a) < epsilon;
    }
}
