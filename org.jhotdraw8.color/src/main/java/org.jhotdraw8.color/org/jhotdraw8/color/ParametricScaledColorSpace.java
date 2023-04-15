/* @(#)CIELCHabColorSpace.java
 * Copyright © The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.color;

import org.jhotdraw8.annotation.NonNull;

/**
 * A parametric color space with scaled components computed from an RGB color space
 * with component values in the range {@code [0, 1]}.
 *
 * @author Werner Randelshofer
 */
public class ParametricScaledColorSpace extends AbstractNamedColorSpace {

    private static final long serialVersionUID = 1L;
    private final @NonNull NamedColorSpace labColorSpace;
    private final @NonNull String name;
    private final float scale;
    private final float inverseScale;

    public ParametricScaledColorSpace(@NonNull String name, float scale, @NonNull NamedColorSpace rgbColorSpace) {
        super(TYPE_RGB, 3);
        assert (rgbColorSpace.getType() == TYPE_RGB);
        this.name = name;
        this.labColorSpace = rgbColorSpace;
        this.scale = scale;
        this.inverseScale = 1 / scale;
    }

    /**
     * XYZ to LCH.
     *
     * @param lch CIEXYZ color value.
     * @return LCH color value.
     */
    @Override
    public float @NonNull [] fromCIEXYZ(float @NonNull [] xyz, float @NonNull [] lch) {
        return rgbToScaled(labColorSpace.fromCIEXYZ(xyz, lch), lch);
    }

    @Override
    public float @NonNull [] fromRGB(float @NonNull [] rgb, float @NonNull [] lch) {
        return rgbToScaled(labColorSpace.fromRGB(rgb, lch), lch);
    }

    @Override
    public float getMaxValue(int component) {
        return scale;
    }

    @Override
    public @NonNull String getName() {
        return name;
    }

    protected float @NonNull [] scaledToRgb(float @NonNull [] scaled, float @NonNull [] rgb) {
        rgb[0] = scaled[0] * inverseScale;
        rgb[1] = scaled[1] * inverseScale;
        rgb[2] = scaled[2] * inverseScale;
        return rgb;
    }

    protected float @NonNull [] rgbToScaled(float @NonNull [] rgb, float @NonNull [] scaled) {
        scaled[0] = rgb[0] * scale;
        scaled[1] = rgb[1] * scale;
        scaled[2] = rgb[2] * scale;
        return rgb;
    }

    /**
     * LCH to XYZ.
     *
     * @param colorvalue LCH color value.
     * @return CIEXYZ color value.
     */
    @Override
    public float @NonNull [] toCIEXYZ(float @NonNull [] colorvalue, float @NonNull [] xyz) {
        return labColorSpace.toCIEXYZ(scaledToRgb(colorvalue, xyz), xyz);
    }

    @Override
    public float @NonNull [] toRGB(float @NonNull [] lch, float @NonNull [] rgb) {
        return labColorSpace.toRGB(scaledToRgb(lch, rgb), rgb);
    }

}
