/*
 * @(#)CompositeEdit.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.fxbase.undo;

import org.jhotdraw8.fxbase.text.FXBaseLabels;
import org.jspecify.annotations.Nullable;

import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;
import java.text.MessageFormat;

/**
 * A concrete subclass of AbstractUndoableEdit, used to assemble little
 * UndoableEdits into great big ones.
 * <p>
 * Usage:
 * <pre>
 *     CompositeEdit compositeEdit=new CompositeEdit();
 *     fire(compositeEdit);
 *     fire(...other edits...);
 *     fire(compositeEdit);
 * </pre>
 */
@SuppressWarnings({"serial", "RedundantSuppression"})
public class CompositeEdit extends CompoundEdit {
    private @Nullable String localizedName;

    public CompositeEdit() {
    }

    public CompositeEdit(@Nullable String localizedName) {
        this.localizedName = localizedName;
    }

    /**
     * If this edit is {@code inProgress},
     * accepts {@code anEdit} and returns true.
     * <p>
     * If an {@code anEdit} is this edit, then
     * accepts self, and sets {@code inProgress}
     * to false.
     *
     * @param anEdit the edit to be added
     * @return
     */
    @Override
    public boolean addEdit(UndoableEdit anEdit) {
        if (isInProgress() && anEdit == this) {
            end();
            return true;
        }
        return super.addEdit(anEdit);
    }

    @Override
    public String getPresentationName() {
        return localizedName;
    }

    @Override
    public String getUndoPresentationName() {
        return MessageFormat.format(FXBaseLabels.getResources().getString("undo.representationName.text"), localizedName);
    }

    @Override
    public String getRedoPresentationName() {
        return MessageFormat.format(FXBaseLabels.getResources().getString("redo.representationName.text"), localizedName);
    }
}
