/*
 * @(#)HSLuvColorUtil.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.color.tmp;

import javafx.scene.paint.Color;

import static org.jhotdraw8.color.tmp.MathUtil.clamp;


public class HlsuvColorUtil {
    private final HlsuvColorSpace hlsuvSpace = new HlsuvColorSpace();
    private final float maxLightness = hlsuvSpace.getMaxValue(2);
    private final float minLightness = hlsuvSpace.getMinValue(2);
    private final float extentLightness = maxLightness - minLightness;

    public HlsuvColorUtil() {

    }

    public Color adjustLightness(Color c, float lightness) {
        float[] baseRgb = new float[]{
                (float) c.getRed(),
                (float) c.getGreen(),
                (float) c.getBlue()
        };
        float[] hsl = hlsuvSpace.fromRGB(baseRgb);
        hsl[2] = extentLightness * lightness + minLightness;
        float[] srgb = hlsuvSpace.toRGB(hsl);
        return new Color(clamp(srgb[0], 0, 1), clamp(srgb[1], 0, 1),
                clamp(srgb[2], 0, 1), 1f);
    }

    public float getLightness(Color c) {
        float[] baseRgb = new float[]{
                (float) c.getRed(),
                (float) c.getGreen(),
                (float) c.getBlue()
        };
        float[] hsl = hlsuvSpace.fromRGB(baseRgb);
        return (hsl[2] - minLightness) / extentLightness;
    }


}
