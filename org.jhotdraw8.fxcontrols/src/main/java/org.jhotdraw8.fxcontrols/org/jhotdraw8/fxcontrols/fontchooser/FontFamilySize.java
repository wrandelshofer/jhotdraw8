/*
 * @(#)FontFamilySize.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxcontrols.fontchooser;

public class FontFamilySize {
    private final String family;
    private final double size;

    public FontFamilySize(String family, double size) {
        this.family = family;
        this.size = size;
    }

    public String getFamily() {
        return family;
    }

    public double getSize() {
        return size;
    }
}
