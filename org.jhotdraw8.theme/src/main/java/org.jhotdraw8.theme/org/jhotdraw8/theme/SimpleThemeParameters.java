/*
 * @(#)SimpleThemeParameters.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.theme;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.jhotdraw8.annotation.NonNull;


public class SimpleThemeParameters implements ThemeParameters {
    private final @NonNull DoubleProperty fontSize = new SimpleDoubleProperty(
            this, "fontSize", Font.getDefault().getSize());
    private final @NonNull StringProperty customCss = new SimpleStringProperty(
            this, "customCss", null);
    private final @NonNull ObjectProperty<Color> accentColor = new SimpleObjectProperty<>(
            this, "accentColor", Color.BLACK
    );

    @Override
    public @NonNull ObjectProperty<Color> accentColorProperty() {
        return accentColor;
    }

    @Override
    public @NonNull StringProperty applicationSpecificCssProperty() {
        return customCss;
    }

    @Override
    public @NonNull DoubleProperty fontSizeProperty() {
        return fontSize;
    }
}
