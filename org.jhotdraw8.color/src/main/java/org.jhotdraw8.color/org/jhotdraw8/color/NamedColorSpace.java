/*
 * @(#)NamedColorSpace.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.color;

import org.jhotdraw8.annotation.NonNull;

import java.awt.color.ColorSpace;

/**
 * Interface for {@code ColorSpace} classes which have a name.
 *
 * @author Werner Randelshofer

 */
public interface NamedColorSpace {
    /**
     * A color space with Luminance, Chroma, Hue components.
     */
    int TYPE_LCH = 32;


    /**
     * Faster fromCIEXYZ method which uses the provided output array.
     */
    float @NonNull [] fromCIEXYZ(float @NonNull [] xyz, float @NonNull [] colorvalue);

    /**
     * See {@link ColorSpace#fromCIEXYZ(float[])}.
     */
    default float @NonNull [] fromCIEXYZ(float @NonNull [] colorvalue) {
        return fromCIEXYZ(colorvalue, new float[getNumComponents()]);
    }

    /**
     * Faster fromRGB method which uses the provided output array.
     */
    float @NonNull [] fromRGB(float @NonNull [] rgb, float @NonNull [] colorvalue);

    /**
     * See {@link ColorSpace#fromRGB(float[])}.
     */
    default float @NonNull [] fromRGB(float @NonNull [] rgb) {
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

    @NonNull String getName();

    /**
     * See {@link ColorSpace#getName(int)} ()}.
     */
    @NonNull String getName(int component);

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
    float @NonNull [] toCIEXYZ(float @NonNull [] colorvalue, float @NonNull [] xyz);

    /**
     * See {@link ColorSpace#toCIEXYZ(float[])}.
     */
    default float @NonNull [] toCIEXYZ(float @NonNull [] colorvalue) {
        return toCIEXYZ(colorvalue, new float[3]);
    }

    /**
     * Faster toRGB method which uses the provided output array.
     */
    float @NonNull [] toRGB(float @NonNull [] colorvalue, float @NonNull [] rgb);

    /**
     * See {@link ColorSpace#toRGB(float[])}.
     */
    default float @NonNull [] toRGB(float @NonNull [] colorvalue) {
        return toRGB(colorvalue, new float[3]);
    }
}
