/*
 * @(#)DeleteAction.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.application.action.edit;

import javafx.event.ActionEvent;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.application.Application;
import org.jhotdraw8.application.ApplicationLabels;
import org.jhotdraw8.application.EditableComponent;

/**
 * Deletes the region at (or after) the caret position.
 *
 * @author Werner Randelshofer
 */
public class DeleteAction extends AbstractSelectionAction {

    /**
     * The ID for this action.
     */
    public static final @NonNull String ID = "edit.delete";

    /**
     * Creates a new instance which acts on the currently focused component.
     *
     * @param app the app
     */
    public DeleteAction(@NonNull Application app) {
        super(app);
        ApplicationLabels.getResources().configureAction(this, ID);
    }

    @Override
    protected void onActionPerformed(ActionEvent event, @NonNull EditableComponent c) {
        c.deleteSelection();
    }

}
