package org.jhotdraw8.draw.undo;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.tree.TreeModelEvent;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

public class TreeModelEventUndoableEdit<T> extends AbstractUndoableEdit {
    private final @NonNull TreeModelEvent<T> event;

    public TreeModelEventUndoableEdit(@NonNull TreeModelEvent<T> event) {
        this.event = event;
    }

    @Override
    public void undo() throws CannotUndoException {
        switch (event.getEventType()) {
        case ROOT_CHANGED -> {
            event.getSource().setRoot(event.getOldRoot());
        }
        case SUBTREE_NODES_CHANGED -> {
        }
        case NODE_ADDED_TO_PARENT -> {
            event.getSource().removeFromParent(event.getChild());
        }
        case NODE_REMOVED_FROM_PARENT -> {
            event.getSource().insertChildAt(event.getChild(), event.getParent(), event.getIndex());
        }
        case NODE_ADDED_TO_TREE -> {
        }
        case NODE_REMOVED_FROM_TREE -> {
        }
        case NODE_CHANGED -> {
        }
        }
    }

    @Override
    public void redo() throws CannotRedoException {
        switch (event.getEventType()) {
        case ROOT_CHANGED -> {
            event.getSource().setRoot(event.getNewRoot());
        }
        case SUBTREE_NODES_CHANGED -> {
        }
        case NODE_ADDED_TO_PARENT -> {
            event.getSource().insertChildAt(event.getChild(), event.getParent(), event.getIndex());
        }
        case NODE_REMOVED_FROM_PARENT -> {
            event.getSource().removeFromParent(event.getChild());
        }
        case NODE_ADDED_TO_TREE -> {
        }
        case NODE_REMOVED_FROM_TREE -> {
        }
        case NODE_CHANGED -> {
        }
        }
    }

    @Override
    public boolean isSignificant() {
        return switch (event.getEventType()) {
            case ROOT_CHANGED -> true;

            case SUBTREE_NODES_CHANGED -> false;
            case NODE_ADDED_TO_PARENT -> true;
            case NODE_REMOVED_FROM_PARENT -> true;
            case NODE_ADDED_TO_TREE -> false;
            case NODE_REMOVED_FROM_TREE -> false;
            case NODE_CHANGED -> false;
        };
    }

    @Override
    public String getPresentationName() {
        return "Tree Structure";
    }


}
