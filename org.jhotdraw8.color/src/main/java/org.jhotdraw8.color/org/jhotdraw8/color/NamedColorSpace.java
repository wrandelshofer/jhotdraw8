/*
 * @(#)NamedColorSpace.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.color;


import java.awt.color.ColorSpace;

/**
 * Interface for {@code ColorSpace} classes which have a name.
 */
public interface NamedColorSpace {
    /**
     * A color space with Luminance, Chroma, Hue components.
     */
    int TYPE_LCH = 32;


    /**
     * Faster fromCIEXYZ method which uses the provided output array.
     */
    float[] fromCIEXYZ(float[] xyz, float[] colorvalue);

    /**
     * See {@link ColorSpace#fromCIEXYZ(float[])}.
     */
    default float[] fromCIEXYZ(float[] colorvalue) {
        return fromCIEXYZ(colorvalue, new float[getNumComponents()]);
    }

    /**
     * Faster fromRGB method which uses the provided output array.
     */
    float[] fromRGB(float[] rgb, float[] colorvalue);

    /**
     * See {@link ColorSpace#fromRGB(float[])}.
     */
    default float[] fromRGB(float[] rgb) {
        return fromRGB(rgb, new float[getNumComponents()]);
    }

    /**
     * See {@link ColorSpace#getMaxValue(int)} ()}.
     */
    float getMaxValue(int component);

    /**
     * See {@link ColorSpace#getMinValue(int)} ()}.
     */
    float getMinValue(int component);

    String getName();

    /**
     * See {@link ColorSpace#getName(int)} ()}.
     */
    String getName(int component);

    /**
     * See {@link ColorSpace#getNumComponents()}.
     */
    int getNumComponents();

    /**
     * See {@link ColorSpace#getType()}.
     */
    int getType();

    /**
     * Faster toCIEXYZ method which uses the provided output array.
     */
    float[] toCIEXYZ(float[] colorvalue, float[] xyz);

    /**
     * See {@link ColorSpace#toCIEXYZ(float[])}.
     */
    default float[] toCIEXYZ(float[] colorvalue) {
        return toCIEXYZ(colorvalue, new float[3]);
    }

    /**
     * Faster toRGB method which uses the provided output array.
     */
    float[] toRGB(float[] colorvalue, float[] rgb);

    /**
     * See {@link ColorSpace#toRGB(float[])}.
     */
    default float[] toRGB(float[] colorvalue) {
        return toRGB(colorvalue, new float[3]);
    }
}
