package org.jhotdraw8.theme;

import org.jhotdraw8.annotation.NonNull;

public abstract class AbstractTheme implements Theme {
    @NonNull
    private final String name;
    @NonNull
    private final String appearance;

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
