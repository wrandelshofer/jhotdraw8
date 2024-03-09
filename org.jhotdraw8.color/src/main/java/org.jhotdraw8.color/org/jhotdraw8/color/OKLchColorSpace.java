/*
 * @(#)OKLchColorSpace.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.color;

public class OKLchColorSpace extends ParametricLchColorSpace {

    public OKLchColorSpace() {
        super("OKLCH", new OKLabColorSpace());
    }
}
