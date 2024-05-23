/*
 * @(#)EditableComponent.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.application;

import javafx.beans.property.ReadOnlyBooleanProperty;
import org.jspecify.annotations.Nullable;

/**
 * EditableComponent defines an API for objects which can perform clipboard operations
 * and have a selection.
 *
 * @author Werner Randelshofer
 */
public interface EditableComponent {

    /**
     * Since sub-classing of a JavaFX component is not convenient, when the component is
     * defined in a FXML file, an editable component can be provided for a JavaFX node by
     * setting the property EDITABLE_COMPONENT.
     */
    String EDITABLE_COMPONENT = "editableComponent";

    /**
     * The name of the selectionEmpty property.
     */
    String SELECTION_EMPTY = "selectionEmpty";

    // ---
    // selection actions
    // ---

    /**
     * Selects all.
     */
    void selectAll();

    /**
     * Clears the selection.
     */
    void clearSelection();

    /**
     * Returns true if the selection is empty. This is a bound property.
     *
     * @return true if empty
     */
    @Nullable ReadOnlyBooleanProperty selectionEmptyProperty();

    // ---
    // edit actions on selection
    // ---

    /**
     * Deletes the selected region or the component at (or after) the caret
     * position.
     */
    void deleteSelection();

    /**
     * Duplicates the selected region.
     */
    void duplicateSelection();

    /**
     * Transfers the contents of the current selection to the clipboard,
     * removing the current selection.
     */
    void cut();

    /**
     * Transfers the contents of the current selection to the clipboard, leaving
     * the current selection.
     */
    void copy();

    /**
     * Transfers the contents from the clipboard replacing the current
     * selection. If there is no selection, the contents in the clipboard is
     * inserted at the current insertion point.
     */
    void paste();
}
