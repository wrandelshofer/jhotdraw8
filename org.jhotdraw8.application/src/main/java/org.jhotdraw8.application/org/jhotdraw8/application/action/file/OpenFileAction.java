/*
 * @(#)OpenFileAction.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.application.action.file;

import org.jhotdraw8.application.ApplicationLabels;
import org.jhotdraw8.application.FileBasedApplication;
import org.jhotdraw8.fxcollection.typesafekey.Key;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Presents an {@code URIChooser} and loads the selected URI into an empty view.
 * If no empty view is available, a new view is created.
 *
 */
public class OpenFileAction extends AbstractOpenFileAction {

    public static final String ID = "file.open";
    private final boolean reuseEmptyViews = true;

    /**
     * Creates a new instance.
     *
     * @param app the application
     */
    @SuppressWarnings("this-escape")
    public OpenFileAction(FileBasedApplication app) {
        super(app);
        ApplicationLabels.getResources().configureAction(this, ID);
    }


    @Override
    protected boolean isReuseEmptyViews() {
        return reuseEmptyViews;
    }

    @Override
    protected Map<Key<?>, Object> getReadOptions() {
        return new LinkedHashMap<>();
    }
}
