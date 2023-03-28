package org.jhotdraw8.fxbase.undo;

import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoableEdit;

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
public class CompositeEdit extends CompoundEdit {


    public CompositeEdit() {
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
}
