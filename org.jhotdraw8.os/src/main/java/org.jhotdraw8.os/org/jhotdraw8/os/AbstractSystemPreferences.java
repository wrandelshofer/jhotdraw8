package org.jhotdraw8.os;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.paint.Color;
import org.jhotdraw8.annotation.NonNull;

/**
 * Abstract base class for system preferences.
 */
public abstract class AbstractSystemPreferences implements SystemPreferences {
    protected final @NonNull ReadOnlyObjectWrapper<Color> accentColor = new ReadOnlyObjectWrapper<>();
    protected final @NonNull ReadOnlyObjectWrapper<Appearance> appearance = new ReadOnlyObjectWrapper<>();
    protected final @NonNull ReadOnlyDoubleWrapper fontSize = new ReadOnlyDoubleWrapper();

    protected AbstractSystemPreferences() {
    }

    @Override
    public @NonNull ReadOnlyObjectProperty<Color> accentColorProperty() {
        return accentColor.getReadOnlyProperty();
    }

    @Override
    public @NonNull ReadOnlyObjectProperty<Appearance> appearanceProperty() {
        return appearance.getReadOnlyProperty();
    }

    @Override
    public @NonNull ReadOnlyDoubleProperty fontSizeProperty() {
        return fontSize.getReadOnlyProperty();
    }
}
