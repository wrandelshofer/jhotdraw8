package org.jhotdraw8.color;

import java.awt.color.ColorSpace;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * HSLuv is a human-friendly alternative to HSL.
 * <p>
 * References:
 * <dl>
 *     <dt>HSLuv Color Space</dt><dd><a href="https://www.hsluv.org/">www.hsluv.org</a></dd>
 *     <dt>HSLuv Reference Implementation</dt><dd><a href="https://github.com/hsluv/hsluv-java">github.com</a></dd>
 * </dl>
 *
 * @author Werner Randelshofer
 */
public class HSLuvColorSpace extends AbstractNamedColorSpace {
    private static final long serialVersionUID = 1L;

    private static HSLuvColorSpace instance;

    public static HSLuvColorSpace getInstance() {
        if (instance == null) {
            instance = new HSLuvColorSpace();
        }
        return instance;
    }

    public HSLuvColorSpace() {
        super(ColorSpace.TYPE_HSV, 3);
    }

    @Override
    public float[] toRGB(float[] hsluv, float[] sRgb) {
        double[] sRgbD = HUSLColorConverter.hsluvToRgb(new double[]{hsluv[0],
                hsluv[1], hsluv[2]});
        sRgb[0] = (float) sRgbD[0];
        sRgb[1] = (float) sRgbD[1];
        sRgb[2] = (float) sRgbD[2];
        return sRgb;
    }

    @Override
    public float[] fromRGB(float[] sRgb, float[] hsluv) {
        double[] hsluvD = HUSLColorConverter.rgbToHsluv(new double[]{sRgb[0], sRgb[1], sRgb[2]});
        hsluv[0] = (float) hsluvD[0];
        hsluv[1] = (float) hsluvD[1];
        hsluv[2] = (float) hsluvD[2];
        return hsluv;
    }

    @Override
    public String getName(int idx) {
        switch (idx) {
        case 0:
            return "Hue";
        case 1:
            return "Saturation";
        case 2:
            return "Lightness";
        default:
            throw new IllegalArgumentException("index must be between 0 and 2:" + idx);
        }
    }

    @Override
    public float getMaxValue(int component) {
        return component == 0 ? 360 : 100;
    }

    @Override
    public float getMinValue(int component) {
        return 0f;
    }

    @Override
    public String getName() {
        return "HSLuv";
    }

    private static float clamp(float v, float minv, float maxv) {
        return max(minv, min(v, maxv));
    }
}
