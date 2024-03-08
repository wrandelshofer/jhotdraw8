/*
 * @(#)FileURIChooser.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.application.controls.urichooser;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.input.DataFormat;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.List;

/**
 * FileURIChooser.
 *
 * @author Werner Randelshofer
 */
public class FileURIChooser implements URIChooser {

    /**
     * The associated file chooser object.
     */
    private final FileChooser chooser = new FileChooser();

    private final ObservableList<URIExtensionFilter> filters = FXCollections.observableArrayList();

    private void updateFilters() {
        ObservableList<FileChooser.ExtensionFilter> cfilters = chooser.getExtensionFilters();
        cfilters.clear();
        for (URIExtensionFilter f : filters) {
            cfilters.add(f.getFileChooserExtensionFilter());
        }
    }

    public enum Mode {

        OPEN, SAVE
    }

    private Mode mode;

    public FileURIChooser() {
        this(Mode.OPEN);
    }

    public FileURIChooser(@NonNull Mode newValue) {
        this(newValue, Collections.emptyList());
    }

    public FileURIChooser(@NonNull Mode newValue, @NonNull List<URIExtensionFilter> extensionFilters) {
        mode = newValue;
        this.filters.setAll(extensionFilters);
    }

    public void setMode(Mode newValue) {
        mode = newValue;
    }

    public Mode getMode() {
        return mode;
    }

    public @NonNull FileChooser getFileChooser() {
        return chooser;
    }

    @Override
    public @Nullable URI showDialog(Window parent) {
        updateFilters();
        File f = switch (mode) {
            case OPEN -> chooser.showOpenDialog(parent);
            case SAVE -> chooser.showSaveDialog(parent);
        };
        return f == null ? null : f.toURI();
    }

    public void setExtensionFilters(List<URIExtensionFilter> filters) {
        this.filters.setAll(filters);
    }

    @Override
    public @Nullable DataFormat getDataFormat() {
        for (URIExtensionFilter f : filters) {
            if (f.getFileChooserExtensionFilter() == chooser.getSelectedExtensionFilter()) {
                return f.getDataFormat();
            }
        }
        return null;
    }
}
