/*
 * @(#)NewFileAction.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.application.action.file;

import javafx.event.ActionEvent;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.application.Application;
import org.jhotdraw8.application.ApplicationLabels;
import org.jhotdraw8.application.FileBasedActivity;
import org.jhotdraw8.application.action.AbstractApplicationAction;

/**
 * Creates a new view.
 *
 * @author Werner Randelshofer
 */
public class NewFileAction extends AbstractApplicationAction {

    public static final @NonNull String ID = "file.new";

    /**
     * Creates a new instance.
     *
     * @param app the application
     */
    public NewFileAction(Application app) {
        this(app, ID);
    }

    public NewFileAction(Application app, String id) {
        super(app);
        ApplicationLabels.getResources().configureAction(this, ID);
    }

    @Override
    protected void onActionPerformed(@NonNull ActionEvent evt, @NonNull Application app) {
        app.createActivity().thenAccept(newView -> {
            FileBasedActivity newDOView = (FileBasedActivity) newView;
            app.getActivities().add(newDOView);
            newDOView.clear().thenRun(newDOView::clearModified);
        });
    }
}
