/*
 * @(#)ParametricNonLinearRgbColorSpace.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.color;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.color.util.FloatFunction;

import java.awt.color.ColorSpace;

/**
 * Adds a non-linear transfer function to a linear {@code RGB} color space.
 */
public class ParametricNonLinearRgbColorSpace extends AbstractNamedColorSpace {
    private final @NonNull FloatFunction fromLinear;
    private @NonNull
    final NamedColorSpace linearCS;
    private final @NonNull String name;
    private final @NonNull FloatFunction toLinear;

    public ParametricNonLinearRgbColorSpace(@NonNull String name, @NonNull NamedColorSpace linearCS,
                                            @NonNull FloatFunction toLinear,
                                            @NonNull FloatFunction fromLinear) {
        super(ColorSpace.TYPE_RGB, 3);
        this.linearCS = linearCS;
        this.name = name;
        this.toLinear = toLinear;
        this.fromLinear = fromLinear;
    }

    @Override
    public float @NonNull [] fromCIEXYZ(float @NonNull [] xyz, float @NonNull [] colorvalue) {
        return fromLinear(linearCS.fromCIEXYZ(xyz, colorvalue), colorvalue);
    }

    protected float[] fromLinear(float linear[], float corrected[]) {
        corrected[0] = fromLinear.apply(linear[0]);
        corrected[1] = fromLinear.apply(linear[1]);
        corrected[2] = fromLinear.apply(linear[2]);
        return corrected;
    }

    @Override
    public float @NonNull [] fromRGB(float @NonNull [] rgb, float @NonNull [] colorvalue) {
        return fromLinear(linearCS.fromRGB(rgb, colorvalue), colorvalue);
    }

    public @NonNull FloatFunction getFromLinear() {
        return fromLinear;
    }

    public @NonNull NamedColorSpace getLinearColorSpace() {
        return linearCS;
    }

    @Override
    public @NonNull String getName() {
        return name;
    }

    public @NonNull FloatFunction getToLinear() {
        return toLinear;
    }

    @Override
    public float @NonNull [] toCIEXYZ(float @NonNull [] colorvalue, float @NonNull [] xyz) {
        return linearCS.toCIEXYZ(toLinear(colorvalue, xyz), xyz);
    }

    protected float[] toLinear(float corrected[], float linear[]) {
        linear[0] = toLinear.apply(corrected[0]);
        linear[1] = toLinear.apply(corrected[1]);
        linear[2] = toLinear.apply(corrected[2]);
        return linear;
    }

    @Override
    public float @NonNull [] toRGB(float @NonNull [] colorvalue, float @NonNull [] rgb) {
        return linearCS.toRGB(toLinear(colorvalue, rgb), rgb);
    }

    @Override
    public float getMinValue(int component) {
        return linearCS.getMinValue(component);
    }

    @Override
    public float getMaxValue(int component) {
        return linearCS.getMaxValue(component);
    }
}
