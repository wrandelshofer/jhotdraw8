/*
 * @(#)MathUtil.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.color.util;

public class MathUtil {
    /**
     * Don't let anyone instantiate this class.
     */
    private MathUtil() {
    }

    /**
     * Clamps a value to the given range.
     *
     * @param value the value
     * @param min   the lower bound of the range
     * @param max   the upper bound of the range
     * @return the constrained value
     */
    public static float clamp(float value, float min, float max) {
        if (Float.isNaN(value) || value < min) {
            return min;
        } else if (value > max) {
            return max;
        }
        return value;
    }

    /**
     * Clamps a value to the given range.
     *
     * @param value the value
     * @param min   the lower bound of the range
     * @param max   the upper bound of the range
     * @return the constrained value
     */
    public static double clamp(double value, double min, double max) {
        if (Double.isNaN(value) || value < min) {
            return min;
        } else if (value > max) {
            return max;
        }
        return value;
    }

    /**
     * Clamps a value to the given range.
     *
     * @param value the value
     * @param min   the lower bound of the range
     * @param max   the upper bound of the range
     * @return the constrained value
     */
    public static int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    public static boolean almostEqual(float a, float b, float eps) {
        return Math.abs(a - b) < eps;
    }

    public static boolean almostEqual(double a, double b, double eps) {
        return Math.abs(a - b) < eps;
    }
}
