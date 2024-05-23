/*
 * @(#)FXColorInterpolator.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.color;

import javafx.scene.paint.Color;
import org.jhotdraw8.color.util.MathUtil;

import java.awt.color.ColorSpace;

/**
 * Linear interpolation between two colors in the specified color space.
 */
public class FXColorInterpolator {
    private final NamedColorSpace cs;
    private final float[] fromColor;
    private final float[] toColor;
    private final float diff1, diff2;
    private final float hueMax, hueMin;
    private final int hueComponent;
    private final boolean wrapAroundHue;

    /**
     * Creates a new instance that interpolates between the specified colors
     * in the specified color space.
     * <p>
     * If the color space is of type {@link ColorSpace#TYPE_HSV} or of
     * type {@link ColorSpace#TYPE_HLS} then it will interpolate along
     * the shorter arc of the hue.
     *
     * @param cs        the color space
     * @param fromColor the color at interpolation time 0
     * @param toColor   the color at interpolation time 1
     */
    public FXColorInterpolator(NamedColorSpace cs, float[] fromColor, float[] toColor) {
        this(cs, fromColor, toColor,
                cs.getType() == ColorSpace.TYPE_HSV
                        || cs.getType() == ColorSpace.TYPE_HLS ? 0 : -1);
    }

    /**
     * Creates a new instance that interpolates between the specified colors
     * in the specified color space.
     * <p>
     * If {@code hasHue} is set to true, then it will interpolate along
     * the shorter arc of the hue.
     *
     * @param cs           the color space
     * @param fromColor    the color at interpolation time 0
     * @param toColor      the color at interpolation time 1
     * @param hueComponent the index of the hue component, or -1 if the color space
     *                     does not have a hue component
     */
    public FXColorInterpolator(NamedColorSpace cs, float[] fromColor, float[] toColor, int hueComponent) {
        this.cs = cs;
        this.fromColor = fromColor;
        this.toColor = toColor;
        this.hueComponent = hueComponent;
        if (hueComponent != -1) {
            // In this color space, the hue forms a circle.
            // We always interpolate along the shorter arc of the circle.
            float to = this.toColor[hueComponent];
            float from = this.fromColor[hueComponent];
            hueMax = cs.getMaxValue(hueComponent);
            hueMin = cs.getMinValue(hueComponent);
            diff1 = to - from;
            diff2 = hueMax - Math.max(from, to) + Math.min(from, to) - hueMin;
            wrapAroundHue = diff2 < Math.abs(diff1);
        } else {
            diff1 = diff2 = 0;
            hueMax = hueMin = 0;
            wrapAroundHue = false;
        }
    }

    public Color interpolate(double t) {
        float[] interpolated = new float[fromColor.length];
        for (int i = 0; i < interpolated.length; i++) {
            interpolated[i] = (float) ((1 - t) * fromColor[i] + t * toColor[i]);
        }
        if (wrapAroundHue) {
            float to = toColor[hueComponent];
            float from = fromColor[hueComponent];
            float interp = (float) (diff1 > 0 ? to + (1 - t) * diff2 : from + t * diff2);
            if (interp > hueMax) {
                interp -= hueMax - hueMin;
            }
            interpolated[hueComponent] = interp;
        }
        float[] srgb = cs.toRGB(interpolated);
        return new Color(MathUtil.clamp(srgb[0], 0, 1), MathUtil.clamp(srgb[1], 0, 1), MathUtil.clamp(srgb[2], 0, 1), 1);
    }


}
