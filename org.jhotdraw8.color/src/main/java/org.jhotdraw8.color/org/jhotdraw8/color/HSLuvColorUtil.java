/*
 * @(#)HSLuvColorUtil.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.color;

import javafx.scene.paint.Color;

import static org.jhotdraw8.color.MathUtil.clamp;


public class HSLuvColorUtil {
    private final HSLuvColorSpace hsluvSpace = new HSLuvColorSpace();
    private final float maxLightness = hsluvSpace.getMaxValue(2);
    private final float minLightness = hsluvSpace.getMinValue(2);
    private final float extentLightness = maxLightness - minLightness;

    public HSLuvColorUtil() {

    }

    public Color adjustLightness(Color c, float lightness) {
        float[] baseRgb = new float[]{
                (float) c.getRed(),
                (float) c.getGreen(),
                (float) c.getBlue()
        };
        float[] hsl = hsluvSpace.fromRGB(baseRgb);
        hsl[2] = extentLightness * lightness + minLightness;
        float[] srgb = hsluvSpace.toRGB(hsl);
        return new Color(clamp(srgb[0], 0, 1), clamp(srgb[1], 0, 1),
                clamp(srgb[2], 0, 1), 1f);
    }

    public float getLightness(Color c) {
        float[] baseRgb = new float[]{
                (float) c.getRed(),
                (float) c.getGreen(),
                (float) c.getBlue()
        };
        float[] hsl = hsluvSpace.fromRGB(baseRgb);
        return (hsl[2] - minLightness) / extentLightness;
    }


}
