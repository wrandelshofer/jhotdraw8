package org.jhotdraw8.color;

import java.awt.color.ColorSpace;
import java.io.Serial;

/**
 * Implements conversions from/to linear RGB color space.
 * <p>
 * This class should give identical results as
 * {@code ColorSpace.getInstance(} {@link ColorSpace#CS_LINEAR_RGB}{@code );}
 * <p>
 * References:
 * <dl>
 *     <dt>A close look at the sRGB formula. Copyright Jason Summers.</dt><dd>
 *         <a href="https://entropymine.com/imageworsener/srgbformula/">
 *             entropymine.como</a>
 *     <dt>Color Conversion Algorithms.  Eugene Vishnevsky.</dt><dd>
 *         <a href="https://www.cs.rit.edu/~ncs/color/t_convert.html#RGB%20to%20XYZ%20&%20XYZ%20to%20RGB">
 *             www.cs.rit.edu</a>
 *     </dd>
 *     <dt>Useful Color Equations. Bruce Lindbloom.</dt><dd>
 *         <a href="http://www.brucelindbloom.com/">brucelindbloom.com</a>
 *     </dd>
 * </dl>
 */
public class RgbLinearColorSpace extends AbstractNamedColorSpace {

    @Serial
    private static final long serialVersionUID = 0L;

    public RgbLinearColorSpace() {
        super(CS_LINEAR_RGB, 3);
    }

    private static float fromLinear(float linear) {
        float scaled = linear <= 0.00313066844250063f ? linear * 12.92f : (float) (Math.pow(linear, 1 / 2.4) * 1.055 - 0.055);
        return scaled < 0 ? 0 : scaled > 1 ? 1 : scaled;
    }

    private static float toLinear(float scaled) {
        float linear = (scaled <= 0.0404482362771082f) ?
                scaled / 12.92f : (float) Math.pow((scaled + 0.055) / 1.055, 2.4);
        return linear < 0 ? 0 : linear > 1 ? 1 : linear;
    }

    @Override
    public float[] fromCIEXYZ(float[] xyz, float[] lrgb) {
        float r = (float) (3.13383065124221 * xyz[0]
                - 1.61711949411313 * lrgb[1]
                - 0.49071914111101 * xyz[2]);
        float g = (float) (-0.97847026691142 * xyz[0]
                + 1.91597856031996 * lrgb[1]
                + 0.03340430640699 * xyz[2]);
        float b = (float) (0.07203679486279 * xyz[0]
                - 0.22903073553113 * lrgb[1]
                + 1.40557835776234 * xyz[2]);
        lrgb[0] = r < 0 ? 0 : r > 1 ? 1 : r;
        lrgb[1] = g < 0 ? 0 : g > 1 ? 1 : g;
        lrgb[2] = b < 0 ? 0 : b > 1 ? 1 : b;
        return lrgb;
    }

    @Override
    public float[] fromRGB(float[] srgb, float[] lrgb) {
        lrgb[0] = toLinear(srgb[0]);
        lrgb[1] = toLinear(srgb[1]);
        lrgb[2] = toLinear(srgb[2]);
        return lrgb;
    }

    @Override
    public String getName() {
        return "RGB Linear";
    }

    @Override
    public float[] toCIEXYZ(float[] lrgb, float[] xyz) {
        xyz[0] = (float) (0.43606375022190 * lrgb[0]
                + 0.38514960146481 * lrgb[1]
                + 0.14308641888799 * lrgb[2]);
        xyz[1] = (float) (0.22245089403542 * lrgb[0]
                + 0.71692584775182 * lrgb[1]
                + 0.06062451125578 * lrgb[2]);
        xyz[2] = (float) (0.01389851860679 * lrgb[0]
                + 0.09707969011198 * lrgb[1]
                + 0.71399604572506 * lrgb[2]);
        return xyz;
    }

    @Override
    public float[] toRGB(float[] lrgb, float[] srgb) {
        srgb[0] = fromLinear(lrgb[0]);
        srgb[1] = fromLinear(lrgb[1]);
        srgb[2] = fromLinear(lrgb[2]);
        return srgb;
    }
}
