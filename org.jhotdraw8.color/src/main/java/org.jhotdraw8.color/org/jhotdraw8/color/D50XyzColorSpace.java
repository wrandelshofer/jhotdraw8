package org.jhotdraw8.color;

import org.jhotdraw8.annotation.NonNull;

import java.awt.color.ColorSpace;

/**
 * CIE XYZ Color Space with D50 white point.
 * <p>
 * The xyz color space accepts three numeric parameters, representing the X,Y and Z values.
 * <dl>
 *     <dt>CSS Color Module Level 4. The Predefined CIE XYZ Color Spaces: the xyz-d50, xyz-d65, and xyz keywords.</dt>
 *     <dd><a href="https://www.w3.org/TR/2022/CRD-css-color-4-20221101/#predefined-xyz">w3.org</a></dd>
 * </dl>
 */
public class D50XyzColorSpace extends AbstractNamedColorSpace {
    private final static @NonNull SrgbColorSpace SRGB_COLOR_SPACE = new SrgbColorSpace();

    public D50XyzColorSpace() {
        super(ColorSpace.TYPE_XYZ, 3);
    }

    @Override
    public float @NonNull [] toCIEXYZ(float @NonNull [] colorvalue, float @NonNull [] xyz) {
        System.arraycopy(colorvalue, 0, xyz, 0, 3);
        return xyz;
    }

    @Override
    public float @NonNull [] fromCIEXYZ(float @NonNull [] xyz, float @NonNull [] colorvalue) {
        System.arraycopy(xyz, 0, colorvalue, 0, 3);
        return colorvalue;
    }

    @Override
    public float @NonNull [] fromRGB(float @NonNull [] rgb, float @NonNull [] colorvalue) {
        return SRGB_COLOR_SPACE.toCIEXYZ(rgb, colorvalue);
    }

    @Override
    public @NonNull String getName() {
        return "D50 XYZ";
    }

    @Override
    public float @NonNull [] toRGB(float @NonNull [] colorvalue, float @NonNull [] rgb) {
        return SRGB_COLOR_SPACE.fromCIEXYZ(colorvalue, rgb);
    }
}