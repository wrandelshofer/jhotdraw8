/* @(#)NamedColorSpace.java
 * Copyright © The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.color;

import java.awt.color.ColorSpace;

/**
 * Interface for {@code ColorSpace} classes which have a name.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public interface NamedColorSpace {
    /**
     * A color space with Luminance, Chroma, Hue components.
     */
    int TYPE_LCH = 32;
    /**
     * A color space with Hue, Saturation, Brightness components.
     */
    int TYPE_HSB = 33;

    /**
     * See {@link ColorSpace#getNumComponents()}.
     */
    int getNumComponents();

    /**
     * See {@link ColorSpace#getType()}.
     */
    int getType();

    String getName();

    /**
     * Faster toRGB method which uses the provided output array.
     */
    float[] toRGB(float[] colorvalue, float[] rgb);

    /**
     * Faster fromRGB method which uses the provided output array.
     */
    float[] fromRGB(float[] rgb, float[] colorvalue);

    /**
     * Faster toCIEXYZ method which uses the provided output array.
     */
    float[] toCIEXYZ(float[] colorvalue, float[] xyz);

    /**
     * Faster fromCIEXYZ method which uses the provided output array.
     */
    float[] fromCIEXYZ(float[] xyz, float[] colorvalue);

    default float[] fromRGB(float[] rgb) {
        return fromRGB(rgb, new float[getNumComponents()]);
    }

    default float[] toCIEXYZ(float[] colorvalue) {
        return toCIEXYZ(colorvalue, new float[3]);
    }

    default float[] toRGB(float[] colorvalue) {
        return toRGB(colorvalue, new float[3]);
    }

    default float[] fromCIEXYZ(float[] colorvalue) {
        return fromCIEXYZ(colorvalue, new float[getNumComponents()]);
    }


    default float[] fromRgb24(int rgb, float[] rgbf, float[] componentf) {
        rgbf[0] = ((rgb >>> 16) & 0xff) / 255f;
        rgbf[1] = ((rgb >>> 8) & 0xff) / 255f;
        rgbf[2] = (rgb & 0xff) / 255f;
        return fromRGB(rgbf, componentf);
    }

    default int toRgb24(float[] componentf, float[] rgbf) {
        toRGB(componentf, rgbf);
        return (MathUtil.clamp((int) (rgbf[0] * 255f), 0, 255) << 16)
                | (MathUtil.clamp((int) (rgbf[1] * 255f), 0, 255) << 8)
                | MathUtil.clamp((int) (rgbf[2] * 255f), 0, 255);
    }

    /**
     * See {@link ColorSpace#getMinValue(int)} ()}.
     */
    float getMinValue(int component);

    /**
     * See {@link ColorSpace#getMaxValue(int)} ()}.
     */
    float getMaxValue(int component);

    /**
     * See {@link ColorSpace#getName(int)} ()}.
     */
    String getName(int component);
}
