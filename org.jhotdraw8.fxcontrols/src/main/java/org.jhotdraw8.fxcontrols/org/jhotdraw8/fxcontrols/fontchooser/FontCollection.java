/*
 * @(#)FontCollection.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxcontrols.fontchooser;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jhotdraw8.annotation.NonNull;

import java.util.List;

/**
 * FontCollection.
 *
 * @author Werner Randelshofer
 */
public class FontCollection {

    private final @NonNull StringProperty name = new SimpleStringProperty();

    private final @NonNull ObservableList<FontFamily> families = FXCollections.observableArrayList();
    private final @NonNull BooleanProperty smartCollection = new SimpleBooleanProperty();

    public FontCollection() {
    }

    public FontCollection(String name, @NonNull List<FontFamily> families) {
        this(name, false, families);
    }

    public FontCollection(String name, boolean isSmart, @NonNull List<FontFamily> families) {
        setName(name);
        setSmartCollection(isSmart);
        this.families.addAll(families);
    }

    public @NonNull ObservableList<FontFamily> getFamilies() {
        return families;
    }

    public String getName() {
        return name.get();
    }

    public void setName(String value) {
        name.set(value);
    }

    public boolean isSmartCollection() {
        return smartCollection.get();
    }

    public void setSmartCollection(boolean value) {
        smartCollection.set(value);
    }

    public @NonNull StringProperty nameProperty() {
        return name;
    }

    public @NonNull BooleanProperty smartCollectionProperty() {
        return smartCollection;
    }

    @Override
    public String toString() {
        return getName();
    }

}
