package org.jhotdraw8.theme;

import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.paint.Color;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.binding.CustomBinding;

public class SimpleThemeManager implements ThemeManager {
    private final @NonNull ObjectProperty<Theme> theme = new SimpleObjectProperty<>(this, "theme", null);
    private final @NonNull ObjectProperty<ThemeParameters> themeParameters = new SimpleObjectProperty<>(
            this, "themeParameters", new SimpleThemeOptions());
    private final @NonNull ReadOnlyListWrapper<Theme> themes = new ReadOnlyListWrapper<>(
            this, "themes", FXCollections.observableArrayList());
    private final @NonNull ObservableValue<Number> viaFontSize = CustomBinding.via(themeParameters, ThemeParameters::fontSizeProperty);
    private final @NonNull ObservableValue<Color> viaAccentColor = CustomBinding.via(themeParameters, ThemeParameters::accentColorProperty);

    public SimpleThemeManager() {
        InvalidationListener invalidationListener = o -> updateUserAgentStylesheet();
        theme.addListener(invalidationListener);
        viaFontSize.addListener(invalidationListener);
        viaAccentColor.addListener(invalidationListener);
    }

    public void updateUserAgentStylesheet() {
        Theme theme = getTheme();
        if (theme == null) {
            Application.setUserAgentStylesheet(null);
        } else {
            Application.setUserAgentStylesheet(theme.createUserAgentStylesheet(getThemeParameters()));
        }
    }

    @Override
    public @NonNull ReadOnlyListProperty<Theme> themesProperty() {
        return themes;
    }

    @Override
    public @NonNull ObjectProperty<Theme> themeProperty() {
        return theme;
    }


    @Override
    public @NonNull ObjectProperty<ThemeParameters> themeParametersProperty() {
        return themeParameters;
    }
}
