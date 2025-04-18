/*
 * @(#)SaveFileAsAction.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.application.action.file;

import org.jhotdraw8.application.FileBasedActivity;

/**
 * Presents an {@code URIChooser} and then saves the active view to the
 * specified location.
 *
 */
public class SaveFileAsAction extends SaveFileAction {

    public static final String ID = "file.saveAs";

    /**
     * Creates a new instance.
     *
     * @param view the view
     */
    public SaveFileAsAction(FileBasedActivity view) {
        super(view, ID, true);
    }

}
