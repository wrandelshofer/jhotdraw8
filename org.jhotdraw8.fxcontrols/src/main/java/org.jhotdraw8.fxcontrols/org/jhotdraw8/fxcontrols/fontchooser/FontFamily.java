/*
 * @(#)FontFamily.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxcontrols.fontchooser;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * FontFamily.
 *
 */
public class FontFamily {
    private final StringProperty name = new SimpleStringProperty();

    private final ObservableList<FontTypeface> typefaces = FXCollections.observableArrayList();

    public FontFamily() {
    }

    public ObservableList<FontTypeface> getTypefaces() {
        return typefaces;
    }

    public String getName() {
        return name.get();
    }

    public void setName(String value) {
        name.set(value);
    }

    public StringProperty nameProperty() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }

}
