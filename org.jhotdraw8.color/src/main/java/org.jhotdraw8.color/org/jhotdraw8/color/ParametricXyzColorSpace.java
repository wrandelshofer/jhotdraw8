package org.jhotdraw8.color;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.color.math.Matrix3;

import java.awt.color.ColorSpace;

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
    public float[] toCIEXYZ(float[] colorvalue, float[] xyz) {
        return toXyzMatrix.mul(colorvalue, xyz);
    }

    @Override
    public float[] fromCIEXYZ(float[] xyz, float[] colorvalue) {
        return fromXyzMatrix.mul(xyz, colorvalue);
    }

    @Override
    public float[] fromRGB(float[] rgb, float[] colorvalue) {
        return fromXyzMatrix.mul(SRGB_COLOR_SPACE.toCIEXYZ(rgb, colorvalue), colorvalue);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public float[] toRGB(float[] colorvalue, float[] rgb) {
        return SRGB_COLOR_SPACE.fromCIEXYZ(toXyzMatrix.mul(colorvalue, rgb), rgb);
    }
}
