/* @(#)HSLPhysiologicColorSpace.java
 * Copyright © The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.color;

import java.awt.color.ColorSpace;

/**
 * A HSL color space with physiologic opposites in the hue color wheel: red is
 * opposite green and yellow is opposite blue.
 *
 * @author Werner Randelshofer
 * @version $Id: HSLPhysiologicColorSpace.java 717 2010-11-21 12:30:57Z rawcoder
 * $
 */
public class HlsPhysiologicColorSpace extends AbstractNamedColorSpace {
    private static final long serialVersionUID = 1L;

    private static HlsPhysiologicColorSpace instance;

    public static HlsPhysiologicColorSpace getInstance() {
        if (instance == null) {
            instance = new HlsPhysiologicColorSpace();
        }
        return instance;
    }

    public HlsPhysiologicColorSpace() {
        super(ColorSpace.TYPE_HLS, 3);
    }

    @Override
    public float[] toRGB(float[] components, float[] rgb) {
        float hue = components[0];
        float lightness = components[1];
        float saturation = components[2];

        // normalize hue
        hue = hue - (float) Math.floor(hue);
        if (hue < 0) {
            hue = 1f + hue;
        }
        // normalize saturation
        if (saturation > 1f) {
            saturation = 1f;
        } else if (saturation < 0f) {
            saturation = 0f;
        }
        // normalize value
        if (lightness > 1f) {
            lightness = 1f;
        } else if (lightness < 0f) {
            lightness = 0f;
        }

        float hueDeg = hue * 360f;
        if (hueDeg < 0) {
            hueDeg += 360f;
        }
        // compute hi and f from hue
        // float f;
        float hk = hue - (float) Math.floor(hue); // / 360f;
        if (hueDeg < 120f) { // red to yellow
            hk /= 2f;
        } else if (hueDeg < 160f) { // yellow to green
            hk = (hk - 120f / 360f) * 3f / 2f + 60f / 360f;
        } else if (hueDeg < 220f) { // green to cyan
            hk = (hk - 160f / 360f) + 120f / 360f;
        } else if (hueDeg < 280f) { // cyan to blue
            hk = (hk - 220f / 360f) + 180f / 360f;
        } else if (hueDeg < 340f) { // blue to purple
            hk = (hk - 280f / 360f) + 240f / 360f;
        } else { // purple to red
            hk = (hk - 340f / 360f) * 3f + 300f / 360f;
        }

        // compute p and q from saturation and lightness
        float q;
        if (lightness < 0.5f) {
            q = lightness * (1f + saturation);
        } else {
            q = lightness + saturation - (lightness * saturation);
        }
        float p = 2f * lightness - q;


        // compute red, green and blue
        float red = hk + 1f / 3f;
        float green = hk;
        float blue = hk - 1f / 3f;

        if (red < 0) {
            red = red + 1f;
        }
        if (green < 0) {
            green = green + 1f;
        }
        if (blue < 0) {
            blue = blue + 1f;
        }
        if (red > 1) {
            red = red - 1f;
        }
        if (green > 1) {
            green = green - 1f;
        }
        if (blue > 1) {
            blue = blue - 1f;
        }

        if (red < 1f / 6f) {
            red = p + ((q - p) * 6 * red);
        } else if (red < 0.5f) {
            red = q;
        } else if (red < 2f / 3f) {
            red = p + ((q - p) * 6 * (2f / 3f - red));
        } else {
            red = p;
        }
        if (green < 1f / 6f) {
            green = p + ((q - p) * 6 * green);
        } else if (green < 0.5f) {
            green = q;
        } else if (green < 2f / 3f) {
            green = p + ((q - p) * 6 * (2f / 3f - green));
        } else {
            green = p;
        }

        if (blue < 1f / 6f) {
            blue = p + ((q - p) * 6 * blue);
        } else if (blue < 0.5f) {
            blue = q;
        } else if (blue < 2f / 3f) {
            blue = p + ((q - p) * 6 * (2f / 3f - blue));
        } else {
            blue = p;
        }

        rgb[0] = red;
        rgb[1] = green;
        rgb[2] = blue;
        return rgb;
    }

    @Override
    public float[] fromRGB(float[] rgbvalue, float[] component) {
        float r = rgbvalue[0];
        float g = rgbvalue[1];
        float b = rgbvalue[2];

        float max = Math.max(Math.max(r, g), b);
        float min = Math.min(Math.min(r, g), b);

        float hue;
        float saturation;
        float luminance;

        if (max == min) {
            hue = 0;
        } else if (max == r && g >= b) { // red to yellow
            hue = 120f * (g - b) / (max - min);
        } else if (max == r) { // red to purple
            hue = 20f * (g - b) / (max - min) + 360f;
        } else if (max == g && r >= b) { // yellow to green
            hue = 40f * (b - r) / (max - min) + 120f + 40f;
        } else if (max == g) { // green to cyan
            hue = 60f * (b - r) / (max - min) + 120f + 40f;
        } else if (g >= r) { // cyan to blue
            hue = 60f * (r - g) / (max - min) + 240f + 40f;
        } else { // blue to purple
            hue = 60f * (r - g) / (max - min) + 240f + 40f;
        }

        luminance = (max + min) / 2f;

        if (max == min) {
            saturation = 0;
        } else if (luminance <= 0.5f) {
            saturation = (max - min) / (max + min);
        } else /* if (lightness  > 0.5f)*/ {
            saturation = (max - min) / (2 - (max + min));
        }

        component[0] = hue / 360f;
        component[1] = luminance;
        component[2] = saturation;
        return component;
    }


    @Override
    public float getMaxValue(int component) {
        return 1f;
    }

    @Override
    public float getMinValue(int component) {
        return 0f;
    }

    @Override
    public String getName() {
        return "HLS Physiological";
    }
}
