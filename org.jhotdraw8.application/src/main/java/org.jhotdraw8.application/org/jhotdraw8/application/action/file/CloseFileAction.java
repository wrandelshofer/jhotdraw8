/*
 * @(#)CloseFileAction.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.application.action.file;

import org.jhotdraw8.application.ApplicationLabels;
import org.jhotdraw8.application.FileBasedActivity;
import org.jhotdraw8.application.action.AbstractSaveUnsavedChangesAction;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Closes the active view after letting the user save unsaved changes.
 *
 * @author Werner Randelshofer
 */
public class CloseFileAction extends AbstractSaveUnsavedChangesAction {

    public static final String ID = "file.close";

    /**
     * Creates a new instance.
     *
     * @param activity the view
     */
    @SuppressWarnings("this-escape")
    public CloseFileAction(FileBasedActivity activity) {
        super(activity);
        ApplicationLabels.getResources().configureAction(this, ID);
    }


    @Override
    protected CompletionStage<Void> doIt(@Nullable FileBasedActivity view) {
        if (view != null) {
            app.getActivities().remove(view);
        }
        return CompletableFuture.completedFuture(null);
    }
}
