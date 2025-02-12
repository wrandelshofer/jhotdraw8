/*
 * @(#)SaveFileAction.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.application.action.file;

import javafx.scene.input.DataFormat;
import org.jhotdraw8.application.FileBasedActivity;

import java.net.URI;

/**
 * Saves the changes in the active view. If the active view has not an URI, an
 * {@code URIChooser} is presented.
 *
 */
public class SaveFileAction extends AbstractSaveFileAction {


    public static final String ID = "file.save";

    /**
     * Creates a new instance.
     *
     * @param view the view
     */
    public SaveFileAction(FileBasedActivity view) {
        this(view, false);
    }

    /**
     * Creates a new instance.
     *
     * @param view   the view
     * @param saveAs whether to force a file dialog
     */
    public SaveFileAction(FileBasedActivity view, boolean saveAs) {
        this(view, ID, saveAs);
    }

    /**
     * Creates a new instance.
     *
     * @param view   the view
     * @param id     the id
     * @param saveAs whether to force a file dialog
     */
    public SaveFileAction(FileBasedActivity view, String id, boolean saveAs) {
        super(view, id, saveAs);
    }


    @Override
    protected void onSaveSucceeded(FileBasedActivity v, URI uri, DataFormat format) {
        v.setURI(uri);
        v.clearModified();
        v.setDataFormat(format);
        app.getRecentUris().put(uri, format);
    }

}
