/* @(#)CIELCHabColorSpace.java
 * Copyright © The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.color;

import org.jhotdraw8.annotation.NonNull;

import static java.lang.Math.PI;

/**
 * The 1976 CIE L*CHa*b* color space (CIELCH).
 * <p>
 * The {@code L*} coordinate of an object is the lightness intensity as measured on a
 * scale from 0 to 100, where 0 represents black and 100 represents white.
 * <p>
 * The {@code C} and {@code H} coordinates are projections of the
 * {@code a*} and {@code b*} colors of the CIE
 * {@code L*a*b*} color space into polar coordinates.
 * <pre>
 * a = C * cos(H)
 * b = C * sin(H)
 * </pre>
 *
 * @author Werner Randelshofer
 */
public class AbstractLchColorSpace extends AbstractNamedColorSpace {

    private static final long serialVersionUID = 1L;
    private final @NonNull AbstractNamedColorSpace labColorSpace;
    private final @NonNull String name;

    public AbstractLchColorSpace(String name, AbstractNamedColorSpace labColorSpace) {
        super(TYPE_LCH, 3);
        this.name = name;
        this.labColorSpace = labColorSpace;
    }

    /**
     * LCH to XYZ.
     *
     * @param colorvalue LCH color value.
     * @return CIEXYZ color value.
     */
    @Override
    public float[] toCIEXYZ(float[] colorvalue, float[] xyz) {
        return labColorSpace.toCIEXYZ(lchToLab(colorvalue, xyz), xyz);
    }

    @NonNull
    protected float[] lchToLab(float[] lch, float[] lab) {
        double L = lch[0];
        double C = lch[1];
        double H = lch[2] * PI / 180;

        double a = C * Math.cos(H);
        double b = C * Math.sin(H);
        lab[0] = (float) L;
        lab[1] = (float) a;
        lab[2] = (float) b;
        return lab;
    }

    /**
     * XYZ to LCH.
     *
     * @param lch CIEXYZ color value.
     * @return LCH color value.
     */
    @Override
    public float[] fromCIEXYZ(float[] xyz, float[] lch) {
        return labToLch(labColorSpace.fromCIEXYZ(xyz, lch), lch);
    }


    protected float[] labToLch(float[] lab, float[] lch) {
        double L = lab[0];
        double a = lab[1];
        double b = lab[2];

        double C = Math.sqrt(a * a + b * b);
        double H = Math.atan2(b, a);

        lch[0] = (float) L;
        lch[1] = (float) C;
        lch[2] = (float) (H * 180 / PI);
        return lch;
    }

    @Override
    public String getName() {
        return "CIE 1976 L*CHa*b*";
    }

    @Override
    public float[] toRGB(float[] lch, float[] rgb) {
        return labColorSpace.toRGB(lchToLab(lch, rgb), rgb);
    }

    @Override
    public float getMinValue(int component) {
        switch (component) {
            case 0:
                return 0f;
            case 1:
                return 0f;
            case 2:
                return 0f;
        }
        throw new IllegalArgumentException("Illegal component:" + component);
    }

    @Override
    public float getMaxValue(int component) {
        switch (component) {
        case 0:
            return 100f;
        case 1:
            return 127f;
        case 2:
            return 360f;
        }
        throw new IllegalArgumentException("Illegal component:" + component);
    }


    @Override
    public float[] fromRGB(float[] rgb, float[] lch) {
        return labToLch(labColorSpace.fromRGB(rgb, lch), lch);
    }
}
