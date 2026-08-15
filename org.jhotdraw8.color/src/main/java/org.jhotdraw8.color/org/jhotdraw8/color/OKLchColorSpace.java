/*
 * @(#)OKLchColorSpace.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */
package org.jhotdraw8.color;

import java.io.Serial;

public class OKLchColorSpace extends ParametricLchColorSpace {
    @Serial
    private static final long serialVersionUID = 0L;

    public static OKLchColorSpace getInstance() {
        class Holder {
            private static final OKLchColorSpace INSTANCE = new OKLchColorSpace();
        }
        return Holder.INSTANCE;
    }


    public OKLchColorSpace() {
        super("OKLCH", new OKLabColorSpace());
    }
}
