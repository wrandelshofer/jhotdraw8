/* @(#)CIELCHabColorSpace.java
 * Copyright © The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.color;


public class OKLchColorSpace extends ParametricLchColorSpace {

    public OKLchColorSpace() {
        super("OKLCH", new OKLabColorSpace());
    }

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
