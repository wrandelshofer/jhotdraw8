/*
 * @(#)UndoAction.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.app.action.edit;

import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.app.Activity;
import org.jhotdraw8.app.Application;
import org.jhotdraw8.app.ApplicationLabels;
import org.jhotdraw8.app.action.AbstractActivityAction;
import org.jhotdraw8.app.action.Action;
import org.jhotdraw8.app.undo.UndoManager;

/**
 * Undoes the last user action.
 *
 * @author Werner Randelshofer
 */
public class UndoAction extends AbstractActivityAction<Activity> {

    public static final String ID = "edit.undo";
    private final @NonNull UndoManager manager;

    /**
     * Creates a new instance.
     *
     * @param app     the application
     * @param view    the view
     * @param manager
     */
    public UndoAction(@NonNull Application app, Activity view, @NonNull UndoManager manager) {
        super(view);
        this.manager = manager;
        ApplicationLabels.getResources().configureAction(this, ID);
        manager.undoableProperty().addListener((ChangeListener<? super Boolean>) (o, oldv, newv) -> {
            if (newv) {
                disablers.add(this);
            } else {
                disablers.remove(this);
            }
        });
        manager.undoPresentationNameProperty().addListener((ChangeListener<? super String>) (o, oldv, newv) -> {
            set(Action.LABEL, newv);
        });
    }

    @Override
    protected void onActionPerformed(ActionEvent event, Activity activity) {
        manager.undo();
    }
}
