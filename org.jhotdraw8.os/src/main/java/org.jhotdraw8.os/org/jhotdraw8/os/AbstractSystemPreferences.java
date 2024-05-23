/*
 * @(#)AbstractSystemPreferences.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.os;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.paint.Color;

/**
 * Abstract base class for system preferences.
 */
public abstract class AbstractSystemPreferences implements SystemPreferences {
    protected final ReadOnlyObjectWrapper<Color> accentColor = new ReadOnlyObjectWrapper<>();
    protected final ReadOnlyObjectWrapper<String> appearance = new ReadOnlyObjectWrapper<>();
    protected final ReadOnlyDoubleWrapper fontSize = new ReadOnlyDoubleWrapper();

    protected AbstractSystemPreferences() {
    }

    @Override
    public ReadOnlyObjectProperty<Color> accentColorProperty() {
        return accentColor.getReadOnlyProperty();
    }

    @Override
    public ReadOnlyObjectProperty<String> appearanceProperty() {
        return appearance.getReadOnlyProperty();
    }

    @Override
    public ReadOnlyDoubleProperty fontSizeProperty() {
        return fontSize.getReadOnlyProperty();
    }
}
