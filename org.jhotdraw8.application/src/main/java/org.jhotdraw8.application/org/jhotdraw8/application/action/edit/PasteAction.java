/*
 * @(#)PasteAction.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.application.action.edit;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.application.Application;
import org.jhotdraw8.application.ApplicationLabels;
import org.jhotdraw8.application.EditableComponent;

/**
 * Pastes the contents of the system clipboard at the caret position.
 *
 * @author Werner Randelshofer
 */
public class PasteAction extends AbstractSelectionAction {

    public static final @NonNull String ID = "edit.paste";

    /**
     * Creates a new instance which acts on the currently focused component.
     *
     * @param app the application
     */
    public PasteAction(@NonNull Application app) {
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
    public PasteAction(@NonNull Application app, Node target) {
        super(app, target);
        ApplicationLabels.getResources().configureAction(this, ID);
    }

    @Override
    protected void onActionPerformed(ActionEvent event, @NonNull EditableComponent c) {
        c.paste();
    }
}
