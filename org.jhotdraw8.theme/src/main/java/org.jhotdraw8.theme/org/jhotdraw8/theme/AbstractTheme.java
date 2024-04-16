/*
 * @(#)AbstractTheme.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.theme;

import org.jhotdraw8.annotation.NonNull;

public abstract class AbstractTheme implements Theme {
    private final @NonNull String name;
    private final @NonNull String appearance;

    protected AbstractTheme(@NonNull String name, @NonNull String appearance) {
        this.name = name;
        this.appearance = appearance;
    }

    @Override
    public @NonNull String getAppearance() {
        return appearance;
    }

    @Override
    public @NonNull String getName() {
        return name;
    }


}
