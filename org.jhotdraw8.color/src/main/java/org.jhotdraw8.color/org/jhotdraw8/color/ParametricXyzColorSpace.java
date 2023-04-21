/*
 * @(#)ParametricXyzColorSpace.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.color;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.color.math.Matrix3;

import java.awt.color.ColorSpace;

/**
 * An XYZ color space with a linear transformation matrix from/to XYZ D50.
 */
public class ParametricXyzColorSpace extends AbstractNamedColorSpace {
    private final static @NonNull SrgbColorSpace SRGB_COLOR_SPACE = new SrgbColorSpace();

    private final @NonNull Matrix3 toXyzMatrix;
    private final @NonNull Matrix3 fromXyzMatrix;

    private final @NonNull String name;

    public ParametricXyzColorSpace(@NonNull String name, @NonNull Matrix3 toXyzMatrix, @NonNull Matrix3 fromXyzMatrix) {
        super(ColorSpace.TYPE_XYZ, 3);
        this.toXyzMatrix = toXyzMatrix;
        this.fromXyzMatrix = fromXyzMatrix;
        this.name = name;
    }

    @Override
    public float @NonNull [] toCIEXYZ(float @NonNull [] colorvalue, float @NonNull [] xyz) {
        return toXyzMatrix.mul(colorvalue, xyz);
    }

    @Override
    public float @NonNull [] fromCIEXYZ(float @NonNull [] xyz, float @NonNull [] colorvalue) {
        return fromXyzMatrix.mul(xyz, colorvalue);
    }

    @Override
    public float @NonNull [] fromRGB(float @NonNull [] rgb, float @NonNull [] colorvalue) {
        return fromXyzMatrix.mul(SRGB_COLOR_SPACE.toCIEXYZ(rgb, colorvalue), colorvalue);
    }

    @Override
    public @NonNull String getName() {
        return name;
    }

    @Override
    public float @NonNull [] toRGB(float @NonNull [] colorvalue, float @NonNull [] rgb) {
        return SRGB_COLOR_SPACE.fromCIEXYZ(toXyzMatrix.mul(colorvalue, rgb), rgb);
    }
}
