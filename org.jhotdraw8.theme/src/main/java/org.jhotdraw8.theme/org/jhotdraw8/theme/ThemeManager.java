package org.jhotdraw8.theme;

import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

public interface ThemeManager {
    @NonNull ObjectProperty<ThemeManager> instance = new SimpleObjectProperty<>(new SimpleThemeManager());

    static @NonNull ObjectProperty<ThemeManager> instanceProperty() {
        return instance;
    }

    static @Nullable ThemeManager getInstance() {
        return instance.get();
    }

    static void setInstance(@Nullable ThemeManager newInstance) {
        instance.set(newInstance);
    }

    @NonNull ReadOnlyListProperty<Theme> themesProperty();

    @NonNull ObjectProperty<ThemeParameters> themeParametersProperty();

    default @Nullable ThemeParameters getThemeParameters() {
        return themeParametersProperty().get();
    }

    default void setThemeParameters(@Nullable ThemeParameters ThemeParameters) {
        this.themeParametersProperty().set(ThemeParameters);
    }

    default @NonNull ObservableList<Theme> getThemes() {
        return themesProperty().get();
    }

    @NonNull ObjectProperty<Theme> themeProperty();


    default @Nullable Theme getTheme() {
        return themeProperty().get();
    }

    default void setTheme(@Nullable Theme theme) {
        this.themeProperty().set(theme);
    }

    /**
     * Updates {@link Application#setUserAgentStylesheet(String)} with
     * the current theme.
     */
    void updateUserAgentStylesheet();
}
