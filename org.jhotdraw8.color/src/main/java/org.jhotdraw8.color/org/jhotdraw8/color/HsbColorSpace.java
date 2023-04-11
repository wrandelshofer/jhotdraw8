/* @(#)HSBColorSpace.java
 * Copyright © The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.color;

import java.awt.*;

/**
 * A HSB color space with additive complements in the hue color wheel: red is
 * opposite cyan, magenta is opposite green, blue is opposite yellow.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class HsbColorSpace extends AbstractNamedColorSpace {
    private static final long serialVersionUID = 1L;

    private static HsbColorSpace instance;

    public static HsbColorSpace getInstance() {
        if (instance == null) {
            instance = new HsbColorSpace();
        }
        return instance;
    }

    public HsbColorSpace() {
        super(NamedColorSpace.TYPE_HSB, 3);
    }

    @Override
    public float[] toRGB(float[] c, float[] rgb) {
        int rgb24 = Color.HSBtoRGB(c[0], c[1], c[2]);

        rgb[0] = ((rgb24 & 0xff0000) >> 16) / 255f;
        rgb[1] = ((rgb24 & 0xff00) >> 8) / 255f;
        rgb[2] = (rgb24 & 0xff) / 255f;
        return rgb;
    }

    @Override
    public float[] fromRGB(float[] rgb, float[] c) {
        Color.RGBtoHSB(//
                (int) ((rgb[0] + 1f / 512) * 255f),//
                (int) ((rgb[1] + 1f / 512) * 255f),//
                (int) ((rgb[2] + 1f / 512) * 255f),//
                c);
        return c;
    }

    @Override
    public float[] toCIEXYZ(float[] colorvalue, float[] xyz) {
        return SrgbColorSpace.getInstance().toCIEXYZ(toRGB(colorvalue, xyz), xyz);
    }

    @Override
    public float[] fromCIEXYZ(float[] xyz, float[] colorvalue) {
        return fromRGB(SrgbColorSpace.getInstance().fromCIEXYZ(xyz, colorvalue), colorvalue);
    }

    @Override
    public String getName() {
        return "HSB";
    }
}
