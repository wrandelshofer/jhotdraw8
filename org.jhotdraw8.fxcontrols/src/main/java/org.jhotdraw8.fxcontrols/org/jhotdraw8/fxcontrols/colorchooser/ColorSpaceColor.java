/*
 * @(#)ColorSpaceColor.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxcontrols.colorchooser;

import org.jhotdraw8.annotation.NonNull;

public class ColorSpaceColor {
    public static final @NonNull ColorSpaceColor WHITE = new ColorSpaceColor();
    public static final @NonNull ColorSpaceColor TRANSPARENT = new ColorSpaceColor();

    /**
     * Don't let anyone instantiate this class.
     */
    private ColorSpaceColor() {
    }
}
