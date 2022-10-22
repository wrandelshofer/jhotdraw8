package org.jhotdraw8.theme;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.paint.Color;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

/**
 * Provides a set of parameters to a {@link Theme}.
 */
public interface ThemeParameters {
    /**
     * The accent color.
     * <p>
     * A {@link Theme} integrates the accent color into its user agent stylesheet.
     *
     * @return accent color property
     */
    @NonNull ObjectProperty<Color> accentColorProperty();

    /**
     * Application specific CSS.
     * <p>
     * A {@link Theme} integrates this CSS into its user agent stylesheet.
     *
     * @return application specific CSS property
     */
    @NonNull StringProperty applicationSpecificCssProperty();

    /**
     * The base font size.
     * <p>
     * A {@link Theme} integrates the font size into its user agent stylesheet.
     *
     * @return font size property
     */
    @NonNull DoubleProperty fontSizeProperty();

    default @Nullable Color getAccentColor() {
        return accentColorProperty().get();
    }

    default void setAccentColor(@Nullable Color accentColor) {
        this.accentColorProperty().set(accentColor);
    }

    default @Nullable String getApplicationSpecificCss() {
        return applicationSpecificCssProperty().get();
    }

    default void setApplicationSpecificCss(@Nullable String css) {
        this.applicationSpecificCssProperty().set(css);
    }

    default double getFontSize() {
        return fontSizeProperty().get();
    }

    default void setFontSize(double fontSize) {
        this.fontSizeProperty().set(fontSize);
    }

}
