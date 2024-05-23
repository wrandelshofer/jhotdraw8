/*
 * @(#)RedoAction.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.application.action.edit;

import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import org.jhotdraw8.application.Activity;
import org.jhotdraw8.application.ApplicationLabels;
import org.jhotdraw8.application.action.AbstractActivityAction;
import org.jhotdraw8.application.action.Action;
import org.jhotdraw8.application.resources.Resources;
import org.jhotdraw8.fxbase.undo.FXUndoManager;

/**
 * Redoes the last user action on the active view.
 *
 * @author Werner Randelshofer
 */
public class RedoAction extends AbstractActivityAction<Activity> {

    public static final String ID = "edit.redo";
    private final Resources labels = ApplicationLabels.getResources();
    private final FXUndoManager manager;

    /**
     * Creates a new instance.
     *
     * @param view the view
     */
    @SuppressWarnings("this-escape")
    public RedoAction(Activity view, FXUndoManager manager) {
        super(view);
        this.manager = manager;
        ApplicationLabels.getResources().configureAction(this, ID);
        manager.redoableProperty().addListener((ChangeListener<? super Boolean>) (o, oldv, newv) -> {
            if (!newv) {
                disablers.add(this);
            } else {
                disablers.remove(this);
            }
        });
        if (!manager.canRedo()) {
            disablers.add(this);
        }
        manager.redoPresentationNameProperty().addListener((ChangeListener<? super String>) (o, oldv, newv) -> set(Action.LABEL, newv));
    }

    @Override
    protected void onActionPerformed(ActionEvent event, Activity activity) {
        manager.redo();
    }
}
