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


public class SimpleThemeParameters implements ThemeParameters {
    private final DoubleProperty fontSize = new SimpleDoubleProperty(
            this, "fontSize", Font.getDefault().getSize());
    private final StringProperty customCss = new SimpleStringProperty(
            this, "customCss", null);
    private final ObjectProperty<Color> accentColor = new SimpleObjectProperty<>(
            this, "accentColor", Color.BLACK
    );

    @Override
    public ObjectProperty<Color> accentColorProperty() {
        return accentColor;
    }

    @Override
    public StringProperty applicationSpecificCssProperty() {
        return customCss;
    }

    @Override
    public DoubleProperty fontSizeProperty() {
        return fontSize;
    }
}
