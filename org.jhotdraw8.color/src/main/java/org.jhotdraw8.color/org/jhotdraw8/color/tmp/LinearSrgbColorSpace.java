package org.jhotdraw8.color.tmp;

import javafx.geometry.Point2D;
import org.jhotdraw8.annotation.NonNull;

/**
 * Linear SRGB Color Space.
 */
public class LinearSrgbColorSpace extends GenericLinearRGBColorSpace {
    private static final @NonNull LinearSrgbColorSpace instance = new LinearSrgbColorSpace();

    public static @NonNull LinearSrgbColorSpace getInstance() {
        return instance;
    }

    private LinearSrgbColorSpace() {
        super("sRGB Linear",
                new Point2D(0.64, 0.33),
                new Point2D(0.3, 0.6),
                new Point2D(0.15, 0.06),
                GenericLinearRGBColorSpace.D65_ILLUMINANT
        );
    }

    @Override
    public float[] toRGB(float[] colorvalue, float[] rgb) {
        return GenericGammaCorrectedRGBColorSpace.fromLinear(colorvalue, rgb);
    }

    @Override
    public float[] fromRGB(float[] rgb, float[] colorvalue) {
        return GenericGammaCorrectedRGBColorSpace.toLinear(rgb, colorvalue);
    }
}
