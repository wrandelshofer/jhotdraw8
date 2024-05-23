/*
 * @(#)DirectoryURIChooser.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.application.controls.urichooser;

import javafx.stage.DirectoryChooser;
import javafx.stage.Window;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.net.URI;

/**
 * FileURIChooser.
 *
 * @author Werner Randelshofer
 */
public class DirectoryURIChooser implements URIChooser {

    /**
     * The associated file chooser object.
     */
    private final DirectoryChooser chooser = new DirectoryChooser();

    public DirectoryURIChooser() {
    }

    public DirectoryChooser getDirectoryChooser() {
        return chooser;
    }

    @Override
    public @Nullable URI showDialog(Window parent) {
        File f = chooser.showDialog(parent);

        return f == null ? null : f.toURI();
    }
}
