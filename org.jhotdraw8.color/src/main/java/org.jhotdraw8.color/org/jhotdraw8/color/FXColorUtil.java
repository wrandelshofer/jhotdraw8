/*
 * @(#)FXColorUtil.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.color;

import javafx.scene.paint.Color;


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
    public static float[] fromColor(NamedColorSpace colorSpace, Color c) {
        return colorSpace.fromRGB(new float[]{(float) c.getRed(), (float) c.getGreen(), (float) c.getBlue()});
    }

    /**
     * Returns the color for the given color components in the specified color space.
     *
     * @param colorSpace the desired color space
     * @param components the color components
     * @return the color components in the specified color space
     */
    public static Color toColor(NamedColorSpace colorSpace, float[] components) {
        float[] srgb = colorSpace.toRGB(components);
        return new Color(Math.clamp(srgb[0], (float) 0, (float) 1), Math.clamp(srgb[1], (float) 0, (float) 1), Math.clamp(srgb[2], (float) 0, (float) 1), 1);
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
