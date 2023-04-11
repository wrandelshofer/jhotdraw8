package org.jhotdraw8.color;

import java.awt.color.ColorSpace;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * HLSuv is a human-friendly alternative to HLS.
 * <p>
 * References:
 * <dl>
 *     <dt>HSLuv Color Space</dt><dd><a href="https://www.hsluv.org/">www.hsluv.org</a></dd>
 *     <dt>HSLuv Reference Implementation</dt><dd><a href="https://github.com/hsluv/hsluv-java">github.com</a></dd>
 * </dl>
 *
 * @author Werner Randelshofer
 */
public class HlsuvColorSpace extends AbstractNamedColorSpace {
    private static final long serialVersionUID = 1L;

    private static HlsuvColorSpace instance;

    public static HlsuvColorSpace getInstance() {
        if (instance == null) {
            instance = new HlsuvColorSpace();
        }
        return instance;
    }

    public HlsuvColorSpace() {
        super(ColorSpace.TYPE_HLS, 3);
    }

    @Override
    public float[] toRGB(float[] hlsuv, float[] sRgb) {
        double[] sRgbD = HlsuvColorConverter.hlsuvToRgb(new double[]{hlsuv[0],
                hlsuv[1], hlsuv[2]});
        sRgb[0] = (float) sRgbD[0];
        sRgb[1] = (float) sRgbD[1];
        sRgb[2] = (float) sRgbD[2];
        return sRgb;
    }

    @Override
    public float[] fromRGB(float[] sRgb, float[] hlsuv) {
        double[] hlsuvD = HlsuvColorConverter.rgbToHlsuv(new double[]{sRgb[0], sRgb[1], sRgb[2]});
        hlsuv[0] = (float) hlsuvD[0];
        hlsuv[1] = (float) hlsuvD[1];
        hlsuv[2] = (float) hlsuvD[2];
        return hlsuv;
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
