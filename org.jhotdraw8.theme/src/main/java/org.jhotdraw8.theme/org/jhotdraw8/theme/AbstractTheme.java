/*
 * @(#)AbstractTheme.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.theme;


public abstract class AbstractTheme implements Theme {
    private final String name;
    private final String appearance;

    protected AbstractTheme(String name, String appearance) {
        this.name = name;
        this.appearance = appearance;
    }

    @Override
    public String getAppearance() {
        return appearance;
    }

    @Override
    public String getName() {
        return name;
    }


}
