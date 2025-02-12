/*
 * @(#)FontChooserModel.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxcontrols.fontchooser;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jspecify.annotations.Nullable;

/**
 * FontChooserModel.
 *
 */
public class FontChooserModel {

    private final ListProperty<FontCollection> fontCollections = new SimpleListProperty<>(FXCollections.observableArrayList());

    public FontChooserModel() {
    }

    public ListProperty<FontCollection> fontCollectionsProperty() {
        return fontCollections;
    }

    public ObservableList<FontCollection> getFontCollections() {
        return fontCollections.get();
    }

    public void setFontCollections(ObservableList<FontCollection> value) {
        fontCollections.set(value);
    }

    public @Nullable FontCollection getAllFonts() {
        return fontCollections.isEmpty() ? null : fontCollections.getFirst();
    }
}
