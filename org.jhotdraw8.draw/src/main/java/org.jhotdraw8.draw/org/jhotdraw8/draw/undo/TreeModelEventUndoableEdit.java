package org.jhotdraw8.draw.undo;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.fxbase.tree.TreeModelEvent;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

public class TreeModelEventUndoableEdit<T> extends AbstractUndoableEdit {
    private final static long serialVersionUID = 0L;
    private final @NonNull TreeModelEvent<T> event;

    public TreeModelEventUndoableEdit(@NonNull TreeModelEvent<T> event) {
        this.event = event;
    }

    @Override
    public void undo() throws CannotUndoException {
        switch (event.getEventType()) {
        case ROOT_CHANGED:
            event.getSource().setRoot(event.getOldRoot());
            break;
        case SUBTREE_NODES_CHANGED:
            break;
        case NODE_ADDED_TO_PARENT:
            event.getSource().removeFromParent(event.getChild());
            break;
        case NODE_REMOVED_FROM_PARENT:
            event.getSource().insertChildAt(event.getChild(), event.getParent(), event.getChildIndex());
            break;
        case NODE_ADDED_TO_TREE:
            break;
        case NODE_REMOVED_FROM_TREE:
            break;
        case NODE_CHANGED:
            break;
        }
    }

    @Override
    public void redo() throws CannotRedoException {
        switch (event.getEventType()) {
        case ROOT_CHANGED:
            event.getSource().setRoot(event.getNewRoot());
            break;
        case SUBTREE_NODES_CHANGED:
            break;
        case NODE_ADDED_TO_PARENT:
            event.getSource().insertChildAt(event.getChild(), event.getParent(), event.getChildIndex());
            break;
        case NODE_REMOVED_FROM_PARENT:
            event.getSource().removeFromParent(event.getChild());
            break;
        case NODE_ADDED_TO_TREE:
            break;
        case NODE_REMOVED_FROM_TREE:
            break;
        case NODE_CHANGED:
            break;
        }
    }

    @Override
    public boolean isSignificant() {
        switch (event.getEventType()) {
        case ROOT_CHANGED:
            return true;
        case SUBTREE_NODES_CHANGED:
            return false;
        case NODE_ADDED_TO_PARENT:
            return true;
        case NODE_REMOVED_FROM_PARENT:
            return true;
        case NODE_ADDED_TO_TREE:
            return false;
        case NODE_REMOVED_FROM_TREE:
            return false;
        case NODE_CHANGED:
            return false;
        default:
            throw new IllegalArgumentException();
        }
    }

    @Override
    public String getPresentationName() {
        return "Tree Structure";
    }


}
