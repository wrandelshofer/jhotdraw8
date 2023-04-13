/* @(#)CIELCHabColorSpace.java
 * Copyright © The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.color;


public class OKLchColorSpace extends ParametricLchColorSpace {

    public OKLchColorSpace() {
        super("OKLCH", new OKLabColorSpace());
    }
/*
    @NonNull
    protected float[] lchToLab(float[] lch, float[] lab) {
        float[] expandedLCH = lab;
        expandedLCH[0] = lch[0];//* 0.8f / 255;
        expandedLCH[1] = lch[1];//* 0.8f / 255;
        expandedLCH[2] = lch[2];
        return super.lchToLab(expandedLCH, lab);
    }

    protected float[] labToLch(float[] lab, float[] lch) {
        super.labToLch(lab, lch);
        float[] compactedLCH = lch;
        compactedLCH[0] = lch[0] ;//* 255 / 0.8f;
        compactedLCH[1] = lch[1] ;//* 255 / 0.8f;
        compactedLCH[2] = lch[2];
        return compactedLCH;
    }
*/
@Override
public float getMaxValue(int component) {
    switch (component) {
        case 0:
            return 1f;
        case 1:
            return 0.4f;
        case 2:
            return 360f;
    }
    throw new IllegalArgumentException("Illegal component:" + component);
}
}
