/*
 * @(#)MathUtil.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.color.util;

public class MathUtil {

    /**
     * Don't let anyone instantiate this class.
     */
    private MathUtil() {
    }

    /**
     * Returns true if {@code abs(a - b) < eps}
     *
     * @param a   value 'a'
     * @param b   value 'b'
     * @param eps epsilon value
     * @return boolean value
     */
    public static boolean almostEqual(float a, float b, float eps) {
        return Math.abs(a - b) < eps;
    }

    /**
     * Returns true if {@code abs(a - b) < eps}
     *
     * @param a   value 'a'
     * @param b   value 'b'
     * @param eps epsilon value
     * @return boolean value
     */
    public static boolean almostEqual(double a, double b, double eps) {
        return Math.abs(a - b) < eps;
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
