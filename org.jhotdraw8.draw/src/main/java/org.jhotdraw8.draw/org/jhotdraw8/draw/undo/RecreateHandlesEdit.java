package org.jhotdraw8.draw.undo;

import org.jhotdraw8.draw.DrawingView;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.UndoableEdit;

/**
 * This undoable edit can be used to recreate handles of a drawing view
 * as part of an undo/redo operation.
 */
public class RecreateHandlesEdit extends AbstractUndoableEdit {
    private final DrawingView drawingView;

    public RecreateHandlesEdit(DrawingView drawingView) {
        this.drawingView = drawingView;
    }

    @Override
    public boolean isSignificant() {
        return false;
    }

    @Override
    public boolean addEdit(UndoableEdit anEdit) {
        return (anEdit instanceof RecreateHandlesEdit that)
                && this.drawingView == that.drawingView;
    }

    public void redo() {
        drawingView.recreateHandles();
    }

    public void undo() {
        drawingView.recreateHandles();
    }
}
