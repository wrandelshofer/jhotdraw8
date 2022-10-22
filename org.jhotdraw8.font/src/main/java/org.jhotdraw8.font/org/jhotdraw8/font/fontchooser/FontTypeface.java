/*
 * @(#)FontTypeface.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.font.fontchooser;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.jhotdraw8.annotation.NonNull;

/**
 * FontTypeface.
 *
 * @author Werner Randelshofer
 */
public class FontTypeface {

    private final StringProperty name = new SimpleStringProperty();
    private final BooleanProperty regular = new SimpleBooleanProperty();

    private final StringProperty style = new SimpleStringProperty();

    public FontTypeface() {
    }

    public String getName() {
        return name.get();
    }

    public void setName(String value) {
        name.set(value);
    }

    public String getStyle() {
        return style.get();
    }

    public void setStyle(String value) {
        style.set(value);
    }

    public boolean isRegular() {
        return regular.get();
    }

    public void setRegular(boolean value) {
        regular.set(value);
    }

    public @NonNull StringProperty nameProperty() {
        return name;
    }

    public @NonNull BooleanProperty regularProperty() {
        return regular;
    }

    public @NonNull StringProperty styleProperty() {
        return style;
    }

    @Override
    public String toString() {
        return getStyle();
    }

}
