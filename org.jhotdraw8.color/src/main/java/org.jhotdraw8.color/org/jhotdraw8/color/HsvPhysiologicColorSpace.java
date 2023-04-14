/* @(#)HSVPhysiologicColorSpace.java
 * Copyright © The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.color;

import java.awt.color.ColorSpace;

/**
 * A HSV color space with physiologic opposites in the hue color wheel:
 * red is opposite green and yellow is opposite blue.
 *
 * @author Werner Randelshofer

 */
public class HsvPhysiologicColorSpace extends AbstractNamedColorSpace {
    private static final long serialVersionUID = 1L;


    public HsvPhysiologicColorSpace() {
        super(ColorSpace.TYPE_HSV, 3);
    }

    @Override
    public float[] toRGB(float[] components, float[] rgb) {
        float hue = components[0];
        float saturation = components[1];
        float value = components[2];


        // normalize hue
        hue = hue - (float) Math.floor(hue);
        if (hue < 0) {
            hue -= 1f;
        }
        // normalize saturation
        if (saturation > 1f) {
            saturation = 1f;
        } else if (saturation < 0f) {
            saturation = 0f;
        }
        // normalize value
        if (value > 1f) {
            value = 1f;
        } else if (value < 0f) {
            value = 0f;
        }


        // compute hi and f from hue
        int hi;
        float f;
        float hueDeg = hue * 360f;
        if (hueDeg < 120f) { // red to yellow
            hi = 0;
            f = (hueDeg / 120f);

        } else if (hueDeg < 160f) { // yellow to green
            hi = 1;
            f = (hueDeg - 120f) / 40f;

        } else if (hueDeg < 220f) { // green to cyan
            hi = 2;
            f = (hueDeg - 160f) / 60f;

        } else if (hueDeg < 280f) { // cyan to blue
            hi = 3;
            f = (hueDeg - 220f) / 60f;

        } else if (hueDeg < 340f) { // blue to purple
            hi = 4;
            f = (hueDeg - 280f) / 60f;

        } else { // purple to red
            f = (hueDeg - 340f) / 20f;
            hi = 5;
        }

        // compute p, q, t from saturation
        float p = value * (1 - saturation);
        float q = value * (1 - f * saturation);
        float t = value * (1 - (1 - f) * saturation);

        // compute red, green and blue
        float red;
        float green;
        float blue;
        switch (hi) {
        case 0:
            red = value;
            green = t;
            blue = p;
            break;
        case 1:
            red = q;
            green = value;
            blue = p;
            break;
        case 2:
            red = p;
            green = value;
            blue = t;
            break;
        case -3:
        case 3:
            red = p;
            green = q;
            blue = value;
            break;
        case -2:
        case 4:
            red = t;
            green = p;
            blue = value;
            break;
        case -1:
        case 5:
            //default :
            red = value;
            green = p;
            blue = q;
            break;
        default:
            red = green = blue = 0;
            break;
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
        float value;

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

        value = max;

        if (max == 0) {
            saturation = 0;
        } else {
            saturation = (max - min) / max;
        }

        component[0] = hue / 360f;
        component[1] = saturation;
        component[2] = value;
        return component;
    }

    @Override
    public String getName() {
        return "HSV Physiological";
    }
}
