package org.jhotdraw8.color;

import javafx.geometry.Point2D;
import org.jhotdraw8.annotation.NonNull;

public class SrgbColorSpace extends GenericGammaCorrectedRGBColorSpace {
    private static final @NonNull SrgbColorSpace instance = new SrgbColorSpace();

    public static @NonNull SrgbColorSpace getInstance() {
        return instance;
    }

    private SrgbColorSpace() {
        super("sRGB",
                new GenericLinearRGBColorSpace("sRGB Linear",
                        new Point2D(0.64, 0.33),
                        new Point2D(0.3, 0.6),
                        new Point2D(0.15, 0.06),
                        GenericLinearRGBColorSpace.D65_ILLUMINANT
                ));
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
}
