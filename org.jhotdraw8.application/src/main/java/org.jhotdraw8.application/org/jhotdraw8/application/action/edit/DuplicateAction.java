/*
 * @(#)DuplicateAction.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.application.action.edit;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import org.jhotdraw8.application.Application;
import org.jhotdraw8.application.ApplicationLabels;
import org.jhotdraw8.application.EditableComponent;
import org.jspecify.annotations.Nullable;

/**
 * Duplicates the selected region.
 *
 */
public class DuplicateAction extends AbstractSelectionAction {

    public static final String ID = "edit.duplicate";

    /**
     * Creates a new instance which acts on the currently focused component.
     *
     * @param app the application
     */
    public DuplicateAction(Application app) {
        this(app, null);
    }

    /**
     * Creates a new instance which acts on the specified component.
     *
     * @param app    the application
     * @param target The target of the action. Specify null for the currently
     *               focused component.
     */
    @SuppressWarnings("this-escape")
    public DuplicateAction(Application app, @Nullable Node target) {
        super(app, target);
        ApplicationLabels.getResources().configureAction(this, ID);
    }

    @Override
    protected void onActionPerformed(ActionEvent event, EditableComponent c) {
        c.duplicateSelection();
    }

}
