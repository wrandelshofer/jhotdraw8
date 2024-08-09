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

    public static boolean almostEqual(float a, float b, float eps) {
        return Math.abs(a - b) < eps;
    }

    public static boolean almostEqual(double a, double b, double eps) {
        return Math.abs(a - b) < eps;
    }
}
