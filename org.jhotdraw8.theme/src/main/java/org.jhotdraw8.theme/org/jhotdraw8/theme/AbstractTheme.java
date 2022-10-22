package org.jhotdraw8.theme;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.os.Appearance;

public abstract class AbstractTheme implements Theme {
    @NonNull
    private final String name;
    @NonNull
    private final Appearance appearance;

    protected AbstractTheme(@NonNull String name, @NonNull Appearance appearance) {
        this.name = name;
        this.appearance = appearance;
    }

    @Override
    public @NonNull Appearance getAppearance() {
        return appearance;
    }

    @Override
    public @NonNull String getName() {
        return name;
    }


}
