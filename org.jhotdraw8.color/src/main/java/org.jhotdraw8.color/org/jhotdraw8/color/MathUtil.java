/*
 * @(#)MathUtil.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.color;

class MathUtil {
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
}
