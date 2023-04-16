/* @(#)CIELCHabColorSpace.java
 * Copyright © The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.color;

import org.jhotdraw8.annotation.NonNull;

import java.awt.color.ColorSpace;

import static org.jhotdraw8.color.util.MathUtil.clamp;

/**
 * A parametric HLS color space computed from an RGB color space.
 * <p>
 * Components:
 * <dl>
 *     <dt>hue</dt><dd>0 to 360 degrees</dd>
 *     <dt>lightness</dt><dd>0 to 1 percentage</dd>
 *     <dt>saturation</dt><dd>0 to 1 percentage</dd>
 * </dl>
 * <p>
 * <p>
 * A color with maximal {@code lightness} is pure white.
 * <p>
 * A color with minimal {@code lightness} is pure black.
 * <p>
 * References:
 * <dl>
 *     <dt>CSS Color Module Level 4. 7. HSL Colors: hsl() and hsla() functions.</dt>
 *     <dd><a href="https://www.w3.org/TR/css-color-4/#the-hsl-notation">w3.org</a></dd>
 * </dl>
 *
 * @author Werner Randelshofer
 */
public class ParametricHlsColorSpace extends AbstractNamedColorSpace {

    private static final long serialVersionUID = 1L;
    private final @NonNull NamedColorSpace rgbColorSpace;
    private final @NonNull String name;

    /**
     * Creates a new instance that is based on the provided RGB color space.
     *
     * @param name          the name of the created color space
     * @param rgbColorSpace the base RGB color space
     * @throws IllegalArgumentException if {@code rgbColorSpace} has not type {@link ColorSpace#TYPE_RGB}.
     */
    public ParametricHlsColorSpace(@NonNull String name, @NonNull NamedColorSpace rgbColorSpace) {
        super(TYPE_HLS, 3);
        assert (rgbColorSpace.getType() == TYPE_RGB);
        this.name = name;
        this.rgbColorSpace = rgbColorSpace;
    }

    /**
     * XYZ to LCH.
     *
     * @param lch CIEXYZ color value.
     * @return LCH color value.
     */
    @Override
    public float @NonNull [] fromCIEXYZ(float @NonNull [] xyz, float @NonNull [] lch) {
        return rgbToHls(rgbColorSpace.fromCIEXYZ(xyz, lch), lch);
    }

    @Override
    public float @NonNull [] fromRGB(float @NonNull [] rgb, float @NonNull [] lch) {
        return rgbToHls(rgbColorSpace.fromRGB(rgb, lch), lch);
    }

    @Override
    public float getMaxValue(int component) {
        return component == 0 ? 360f : 1f;
    }

    @Override
    public @NonNull String getName() {
        return name;
    }

    protected float @NonNull [] hlsToRgb(float[] hls, float[] rgb) {
        float hue = hls[0] / 360f;
        float saturation = hls[2];
        float lightness = hls[1];

        // compute p and q from saturation and lightness
        float q;
        if (lightness < 0.5f) {
            q = lightness * (1f + saturation);
        } else {
            q = lightness + saturation - (lightness * saturation);
        }
        float p = 2f * lightness - q;

        // normalize hue to -1..+1

        float hk = hue - (float) Math.floor(hue);

        // compute red, green and blue
        float red = hk + 1f / 3f;
        float green = hk;
        float blue = hk - 1f / 3f;

        // normalize rgb values
        if (red < 0) {
            red = red + 1f;
        } else if (red > 1) {
            red = red - 1f;
        }

        if (green < 0) {
            green = green + 1f;
        } else if (green > 1) {
            green = green - 1f;
        }

        if (blue < 0) {
            blue = blue + 1f;
        } else if (blue > 1) {
            blue = blue - 1f;
        }


        // adjust rgb values
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

        rgb[0] = clamp(red, 0, 1);
        rgb[1] = clamp(green, 0, 1);
        rgb[2] = clamp(blue, 0, 1);
        return rgb;
    }

    protected float[] rgbToHls(float[] rgb, float[] hls) {
        float r = rgb[0];
        float g = rgb[1];
        float b = rgb[2];

        float max = Math.max(Math.max(r, g), b);
        float min = Math.min(Math.min(r, g), b);

        float hue;
        float saturation;
        float luminance;

        if (max == min) {
            hue = 0;
        } else if (max == r) {
            hue = 60f * (g - b) / (max - min);
            if (g < b) {
                hue += 360f;
            }
        } else if (max == g) {
            hue = 60f * (b - r) / (max - min) + 120f;
        } else /*if (max == b)*/ {
            hue = 60f * (r - g) / (max - min) + 240f;
        }

        luminance = (max + min) / 2f;

        if (max == min) {
            saturation = 0;
        } else if (luminance <= 0.5f) {
            saturation = (max - min) / (max + min);
        } else /* if (lightness  > 0.5f)*/ {
            saturation = (max - min) / (2 - (max + min));
        }

        hls[0] = hue;
        hls[2] = saturation;
        hls[1] = luminance;
        return hls;
    }

    /**
     * LCH to XYZ.
     *
     * @param colorvalue LCH color value.
     * @return CIEXYZ color value.
     */
    @Override
    public float @NonNull [] toCIEXYZ(float @NonNull [] colorvalue, float @NonNull [] xyz) {
        return rgbColorSpace.toCIEXYZ(hlsToRgb(colorvalue, xyz), xyz);
    }

    @Override
    public float @NonNull [] toRGB(float @NonNull [] lch, float @NonNull [] rgb) {
        return rgbColorSpace.toRGB(hlsToRgb(lch, rgb), rgb);
    }

}
