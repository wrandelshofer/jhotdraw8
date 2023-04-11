package org.jhotdraw8.color.tmp;

import java.awt.*;
import java.awt.color.ColorSpace;

/**
 * Utility methods for AWT colors.
 */
public class AwtColorUtil {
    /**
     * Don't let anyone instantiate this class.
     */
    private AwtColorUtil() {
    }

    /**
     * Returns the color components in the specified color space.
     *
     * @param colorSpace the desired color space
     * @param c          a color
     * @return the color components in the specified color space
     */
    public static float[] fromColor(ColorSpace colorSpace, Color c) {
        if (c.getColorSpace() == colorSpace) {
            float[] components = c.getComponents(null);
            return components;
        } else {
            return c.getComponents(colorSpace, null);
        }
    }

    /**
     * Blackens the specified color by casting a black shadow of the specified
     * amount on the color.
     */
    public static Color shadow(Color c, int amount) {
        return new Color(
                Math.max(0, c.getRed() - amount),
                Math.max(0, c.getGreen() - amount),
                Math.max(0, c.getBlue() - amount),
                c.getAlpha());
    }

}
