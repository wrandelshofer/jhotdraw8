/*
 * @(#)SimpleSystemPreferences.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.os;

import javafx.scene.paint.Color;
import org.jhotdraw8.annotation.Nullable;

/**
 * Simple implementation of system preferences with setter methods.
 */
public class SimpleSystemPreferences extends AbstractSystemPreferences {
    public SimpleSystemPreferences() {
    }

    public void setAccentColor(@Nullable Color value) {
        accentColor.set(value);
    }

    public void setAppearance(@Nullable String value) {
        appearance.set(value);
    }

    public void setFontSize(double value) {
        fontSize.set(value);
    }
}
