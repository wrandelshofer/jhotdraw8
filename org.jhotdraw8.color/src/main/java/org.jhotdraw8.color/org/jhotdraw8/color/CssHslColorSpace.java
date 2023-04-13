package org.jhotdraw8.color;

public class CssHslColorSpace extends AbstractNamedColorSpace {
    private static final HlsColorSpace HLS_COLOR_SPACE = new HlsColorSpace();

    public CssHslColorSpace() {
        super(NamedColorSpace.TYPE_HSL, 3);
    }

    @Override
    public float[] fromRGB(float[] rgb, float[] colorvalue) {
        float[] hls = HLS_COLOR_SPACE.fromRGB(rgb, colorvalue);
        float h = hls[0];
        float l = hls[1];
        float s = hls[2];
        colorvalue[0] = h;
        colorvalue[1] = s;
        colorvalue[2] = l;
        return colorvalue;
    }

    @Override
    public String getName() {
        return "hsl";
    }

    @Override
    public float[] toRGB(float[] colorvalue, float[] rgb) {
        float h = colorvalue[0];
        float s = colorvalue[1];
        float l = colorvalue[2];
        float[] hls = rgb;
        hls[0] = h;
        hls[1] = l;
        hls[2] = s;
        return HLS_COLOR_SPACE.toRGB(hls, rgb);
    }

    @Override
    public float[] toCIEXYZ(float[] colorvalue, float[] xyz) {
        float h = colorvalue[0];
        float s = colorvalue[1];
        float l = colorvalue[2];
        float[] hls = xyz;
        hls[0] = h;
        hls[1] = l;
        hls[2] = s;
        return HLS_COLOR_SPACE.toCIEXYZ(hls, xyz);
    }

    @Override
    public float[] fromCIEXYZ(float[] xyz, float[] colorvalue) {
        float[] hls = HLS_COLOR_SPACE.fromCIEXYZ(xyz, colorvalue);
        float h = hls[0];
        float l = hls[1];
        float s = hls[2];
        colorvalue[0] = h;
        colorvalue[1] = s;
        colorvalue[2] = l;
        return colorvalue;
    }
}
