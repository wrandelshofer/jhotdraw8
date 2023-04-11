/*
 * @(#)FXColorUtil.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.color.tmp;

import javafx.scene.paint.Color;

import java.awt.color.ColorSpace;


public class FXColorUtil {
    /**
     * Don't let anyone instantiate this class.
     */
    private FXColorUtil() {
    }

    /**
     * Returns the color components in the specified color space for the given
     * color.
     *
     * @param colorSpace the desired color space
     * @param c          the color
     * @return the color components in the specified color space
     */
    public static float[] fromColor(ColorSpace colorSpace, Color c) {
        return colorSpace.fromRGB(new float[]{(float) c.getRed(), (float) c.getGreen(), (float) c.getBlue()});
    }

    /**
     * Returns the color for the given color components in the specified color space.
     *
     * @param colorSpace the desired color space
     * @param components the color components
     * @return the color components in the specified color space
     */
    public static Color toColor(ColorSpace colorSpace, float[] components) {
        float[] srgb = colorSpace.toRGB(components);
        return new Color(MathUtil.clamp(srgb[0], 0, 1), MathUtil.clamp(srgb[1], 0, 1), MathUtil.clamp(srgb[2], 0, 1), 1);
    }

    /**
     * Returns a web color representation of the specified color.
     *
     * @param color
     * @return the web color string
     */
    public static String toWebColor(Color color) {
        if (color == null) {
            return "none";
        }
        int r = (int) Math.round(color.getRed() * 255.0);
        int g = (int) Math.round(color.getGreen() * 255.0);
        int b = (int) Math.round(color.getBlue() * 255.0);
        int o = (int) Math.round(color.getOpacity() * 255.0);
        return String.format("#%02x%02x%02x%02x", r, g, b, o);
    }
}
