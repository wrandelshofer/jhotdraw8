/* @(#)CIELCHabColorSpace.java
 * Copyright © The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.color;


import org.jhotdraw8.annotation.NonNull;

public class OKLchColorSpace extends AbstractLchColorSpace {
    private static @NonNull OKLchColorSpace instance = new OKLchColorSpace();

    public static @NonNull OKLchColorSpace getInstance() {
        return instance;
    }

    public OKLchColorSpace() {
        super("OKLCH", OKLabColorSpace.getInstance());
    }

    @NonNull
    protected float[] lchToLab(float[] lch, float[] lab) {
        float[] expandedLCH = lab;
        expandedLCH[0] = lch[0] * 0.8f / 255;
        expandedLCH[1] = lch[1] * 0.8f / 255;
        expandedLCH[2] = lch[2];
        return super.lchToLab(expandedLCH, lab);
    }

    protected float[] labToLch(float[] lab, float[] lch) {
        super.labToLch(lab, lch);
        float[] compactedLCH = lch;
        compactedLCH[0] = lch[0] * 255 / 0.8f;
        compactedLCH[1] = lch[1] * 255 / 0.8f;
        compactedLCH[2] = lch[2];
        return compactedLCH;
    }
}
