/*
 * @(#)SystemPreferences.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.os;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

/**
 * Encapsulates system preferences.
 */
public interface SystemPreferences {
    @NonNull ObjectProperty<SystemPreferences> instance = new SimpleObjectProperty<>();

    static @NonNull ObjectProperty<SystemPreferences> instanceProperty() {
        return instance;
    }

    static void setInstance(@Nullable SystemPreferences newInstance) {
        instance.set(newInstance);
    }

    static @Nullable SystemPreferences getInstance() {
        return instance.get();
    }

    /**
     * The accent color.
     *
     * @return accent color property
     */
    @NonNull ReadOnlyObjectProperty<Color> accentColorProperty();

    default @Nullable Color getAccentColor() {
        return accentColorProperty().get();
    }

    /**
     * The appearance.
     *
     * @return accent color property
     */
    @NonNull ReadOnlyObjectProperty<String> appearanceProperty();

    default @Nullable String getAppearance() {
        return appearanceProperty().get();
    }

    /**
     * The base font size.
     *
     * @return font size property
     */
    @NonNull ReadOnlyDoubleProperty fontSizeProperty();

    default double getFontSize() {
        return fontSizeProperty().get();
    }
}
