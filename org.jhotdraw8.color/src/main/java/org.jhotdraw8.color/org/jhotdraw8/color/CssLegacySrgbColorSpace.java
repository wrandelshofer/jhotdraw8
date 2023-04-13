package org.jhotdraw8.color;

/**
 * The {@code sRGB} color space with components in the range from 0 to 255.
 * <p>
 * <p>
 * References:
 * <dl>
 *     <dt>Wikipedia. sRGB.</dt>
 *     <dd><a href="https://en.wikipedia.org/wiki/SRGB">wikipedia</a></dd>
 *
 *     <dt>CSS Color Module Level 4. The Predefined sRGB Color Space: the sRGB keyword.</dt>
 *     <dd><a href="https://www.w3.org/TR/2022/CRD-css-color-4-20221101/#predefined-sRGB">w3.org</a></dd>
 *
 *     <dt>CSS Color Module Level 4. The Predefined Linear-light sRGB Color Space: the srgb-linear keyword.</dt>
 *     <dd><a href="https://www.w3.org/TR/2022/CRD-css-color-4-20221101/#predefined-sRGB-linear">w3.org</a></dd>
 * </dl>
 */
public class CssLegacySrgbColorSpace extends ParametricNonLinearRgbColorSpace {

    public CssLegacySrgbColorSpace() {
        super("srgb", new CssLegacyLinearSrgbColorSpace(),
                CssLegacySrgbColorSpace::toLinear,
                CssLegacySrgbColorSpace::fromLinear
        );
    }

    @Override
    public float[] toRGB(float[] colorvalue, float[] rgb) {
        System.arraycopy(colorvalue, 0, rgb, 0, 3);
        return rgb;
    }

    @Override
    public float[] fromRGB(float[] rgb, float[] colorvalue) {
        System.arraycopy(rgb, 0, colorvalue, 0, 3);
        return colorvalue;
    }

    /**
     * Inverse "Gamma" transfer function.
     */
    public static float fromLinear(float linear) {
        return LinearSrgbColorSpace.fromLinear(linear);
    }

    /**
     * "Gamma" transfer function.
     */
    public static float toLinear(float corrected) {
        return LinearSrgbColorSpace.toLinear(corrected);
    }
}
