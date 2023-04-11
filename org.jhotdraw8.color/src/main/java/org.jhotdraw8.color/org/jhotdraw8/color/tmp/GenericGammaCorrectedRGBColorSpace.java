package org.jhotdraw8.color.tmp;

import javafx.geometry.Point2D;
import org.jhotdraw8.annotation.NonNull;

import java.awt.color.ColorSpace;

/**
 * A generic color space with 'gamma'-corrected RGB values.
 * <p>
 * RGB_gamma_corrected = fromLinear(RGB_linear).
 * <p>
 * RGB_linear = toLinear(RGB_gamma_corrected).
 * <p>
 * The 'gamma' transfer function is a slight tweaking of {@code x<sup>2.2</sup>}.
 * To avoid a zero slope at {@code x=0}, the formula uses a higher exponent {@code 2.4}.
 * <p>
 * The instantaneous gamma (the slope when plotted on a log:log scale) varies from 1 in the linear
 * section to {@code 2.4} at maximum intensity, with a median value being close to {@code 2.2}.
 */
public class GenericGammaCorrectedRGBColorSpace extends AbstractNamedColorSpace {
    private @NonNull
    final NamedColorSpace linearCS;
    private final @NonNull String name;

    public static final @NonNull GenericLinearRGBColorSpace LINEAR_DISPLAY_P3_INSTANCE = new GenericLinearRGBColorSpace("Display P3 Linear",
            new Point2D(0.68, 0.32),
            new Point2D(0.265, 0.69),
            new Point2D(0.15, 0.06),
            GenericLinearRGBColorSpace.D65_ILLUMINANT
    );
    public final static @NonNull GenericGammaCorrectedRGBColorSpace DISPLAY_P3_INSTANCE = new GenericGammaCorrectedRGBColorSpace("Display P3",
            LINEAR_DISPLAY_P3_INSTANCE);

    public GenericGammaCorrectedRGBColorSpace(@NonNull String name, @NonNull NamedColorSpace linearCS) {
        super(ColorSpace.TYPE_RGB, 3);
        this.linearCS = linearCS;
        this.name = name;
    }

    public static float fromLinear(float linear) {
        float corrected = linear <= 0.0031308f//0.00313066844250063f
                ? linear * 12.92f
                : (float) Math.pow(linear, 1 / 2.4) * 1.055f - 0.055f;
        return corrected;
    }

    public static float[] fromLinear(float linear[], float corrected[]) {
        corrected[0] = fromLinear(linear[0]);
        corrected[1] = fromLinear(linear[1]);
        corrected[2] = fromLinear(linear[2]);
        return corrected;
    }

    public static float toLinear(float corrected) {
        float linear = (corrected <= 0.04045f/*0.0404482362771082f*/)
                ? (corrected / 12.92f)
                : (float) Math.pow((corrected + 0.055f) / 1.055f, 2.4);
        return linear;
    }

    public static float[] toLinear(float corrected[], float linear[]) {
        linear[0] = toLinear(corrected[0]);
        linear[1] = toLinear(corrected[1]);
        linear[2] = toLinear(corrected[2]);
        return linear;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public float[] toRGB(float[] colorvalue, float[] rgb) {
        return linearCS.toRGB(toLinear(colorvalue, rgb), rgb);
    }

    @Override
    public float[] fromRGB(float[] rgb, float[] colorvalue) {
        return fromLinear(linearCS.fromRGB(rgb, colorvalue), colorvalue);
    }

    @Override
    public float[] toCIEXYZ(float[] colorvalue, float[] xyz) {
        return linearCS.toCIEXYZ(toLinear(colorvalue, xyz), xyz);
    }

    @Override
    public float[] fromCIEXYZ(float[] xyz, float[] colorvalue) {
        return fromLinear(linearCS.fromCIEXYZ(xyz, colorvalue), colorvalue);
    }
}
