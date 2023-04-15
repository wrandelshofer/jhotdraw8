package org.jhotdraw8.color;

import org.jhotdraw8.annotation.NonNull;

/**
 * The {@code sRGB} color space.
 * <p>
 * sRGB is a standard RGB (red, green, blue) color space that HP and Microsoft created cooperatively in 1996 to use
 * on monitors, printers, and the World Wide Web.
 * <p>
 * This class defines the color space with the non-linear "gamma" transfer correction.
 * You can obtain a linear version of this colors space without gamma correction using the following code:
 * <pre>
 *     new SrgbColorSpace().getLinearColorSpace();
 * </pre>
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
public class SrgbColorSpace extends ParametricNonLinearRgbColorSpace {

    public SrgbColorSpace() {
        super("sRGB", new LinearSrgbColorSpace(),
                SrgbColorSpace::toLinear,
                SrgbColorSpace::fromLinear
        );
    }

    @Override
    public float @NonNull [] toRGB(float @NonNull [] colorvalue, float @NonNull [] rgb) {
        System.arraycopy(colorvalue, 0, rgb, 0, 3);
        return rgb;
    }

    @Override
    public float @NonNull [] fromRGB(float @NonNull [] rgb, float @NonNull [] colorvalue) {
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
