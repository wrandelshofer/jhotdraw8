/*
 * @(#)ThemeManager.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.theme;

import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import org.jspecify.annotations.Nullable;

public interface ThemeManager {
    ObjectProperty<ThemeManager> instance = new SimpleObjectProperty<>(new SimpleThemeManager());

    static ObjectProperty<ThemeManager> instanceProperty() {
        return instance;
    }

    static ThemeManager getInstance() {
        return instance.get();
    }

    static void setInstance(ThemeManager newInstance) {
        instance.set(newInstance);
    }

    ReadOnlyListProperty<Theme> themesProperty();

    ObjectProperty<ThemeParameters> themeParametersProperty();

    default ThemeParameters getThemeParameters() {
        return themeParametersProperty().get();
    }

    default void setThemeParameters(@Nullable ThemeParameters ThemeParameters) {
        this.themeParametersProperty().set(ThemeParameters);
    }

    default ObservableList<Theme> getThemes() {
        return themesProperty().get();
    }

    ObjectProperty<Theme> themeProperty();


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
