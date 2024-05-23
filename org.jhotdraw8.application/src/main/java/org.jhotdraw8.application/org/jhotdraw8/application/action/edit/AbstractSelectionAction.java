/*
 * @(#)AbstractSelectionAction.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.application.action.edit;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.TextInputControl;
import org.jhotdraw8.application.Activity;
import org.jhotdraw8.application.Application;
import org.jhotdraw8.application.EditableComponent;
import org.jhotdraw8.application.action.AbstractApplicationAction;
import org.jspecify.annotations.Nullable;

/**
 * {@code AbstractSelectionAction} acts on the selection of a target component
 * or of the currently focused component in the application.
 *
 * @author Werner Randelshofer
 */
public abstract class AbstractSelectionAction extends AbstractApplicationAction {

    private final @Nullable Node target;


    /**
     * Creates a new instance.
     *
     * @param application the application
     */
    public AbstractSelectionAction(Application application) {
        this(application, null);
    }

    /**
     * Creates a new instance.
     *
     * @param application the application
     * @param target      the target node
     */
    public AbstractSelectionAction(Application application, @Nullable Node target) {
        super(application);
        this.target = target;
    }

    public @Nullable EditableComponent getEditableComponent() {
        if (target != null) {
            return tryAsEditableComponent(target);
        }

        Activity v = app.getActiveActivity();
        if (v != null && !v.isDisabled()) {
            Node n = v.getNode().getScene().getFocusOwner();
            while (n != null) {
                EditableComponent editableComponent = tryAsEditableComponent(n);
                if (editableComponent != null) {
                    return editableComponent;
                }
                n = n.getParent();
            }
        }
        return null;
    }

    private @Nullable EditableComponent tryAsEditableComponent(Node n) {
        if (n instanceof TextInputControl tic) {
            return new TextInputControlAdapter(tic);
        } else if (n instanceof EditableComponent tic) {
            return tic;
        } else if (n.getProperties().get(EditableComponent.EDITABLE_COMPONENT) instanceof EditableComponent tic) {
            return tic;
        } else {
            return null;
        }
    }

    @Override
    protected final void onActionPerformed(ActionEvent event, Application application) {
        EditableComponent ec = getEditableComponent();
        if (ec != null) {
            onActionPerformed(event, ec);
        }
    }

    protected abstract void onActionPerformed(ActionEvent event, EditableComponent ec);

    private record TextInputControlAdapter(TextInputControl control) implements EditableComponent {

        @Override
        public void clearSelection() {
            control.selectRange(control.getCaretPosition(), control.getCaretPosition());
        }

        @Override
        public void copy() {
            control.copy();
        }

        @Override
        public void cut() {
            control.cut();
        }

        @Override
        public void deleteSelection() {
            control.deleteText(control.getSelection());
        }

        @Override
        public void duplicateSelection() {
            control.insertText(control.getCaretPosition(), control.getSelectedText());
        }

        @Override
        public void paste() {
            control.paste();
        }

        @Override
        public void selectAll() {
            control.selectAll();
        }

        @Override
        public ReadOnlyBooleanProperty selectionEmptyProperty() {
            ReadOnlyBooleanWrapper p = new ReadOnlyBooleanWrapper();
            p.bind(control.selectedTextProperty().isNull());
            return p.getReadOnlyProperty();
        }

    }
}
