/*
 * @(#)TreeModelEventUndoableEdit.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.draw.undo;

import org.jhotdraw8.fxbase.tree.TreeModelEvent;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import java.io.Serial;

public class TreeModelEventUndoableEdit<T> extends AbstractUndoableEdit {
    @Serial
    private static final long serialVersionUID = 0L;
    private final TreeModelEvent<T> event;

    public TreeModelEventUndoableEdit(TreeModelEvent<T> event) {
        this.event = event;
    }

    @Override
    public void undo() throws CannotUndoException {
        switch (event.getEventType()) {
            case ROOT_CHANGED:
                event.getSource().setRoot(event.getOldRoot());
                break;
            case SUBTREE_NODES_CHANGED, NODE_CHANGED, NODE_REMOVED_FROM_TREE, NODE_ADDED_TO_TREE:
                break;
            case NODE_ADDED_TO_PARENT:
                event.getSource().removeFromParent(event.getChild());
                break;
            case NODE_REMOVED_FROM_PARENT:
                event.getSource().insertChildAt(event.getChild(), event.getParent(), event.getChildIndex());
                break;
        }
    }

    @Override
    public void redo() throws CannotRedoException {
        switch (event.getEventType()) {
            case ROOT_CHANGED:
                event.getSource().setRoot(event.getNewRoot());
                break;
            case SUBTREE_NODES_CHANGED, NODE_CHANGED, NODE_REMOVED_FROM_TREE, NODE_ADDED_TO_TREE:
                break;
            case NODE_ADDED_TO_PARENT:
                event.getSource().insertChildAt(event.getChild(), event.getParent(), event.getChildIndex());
                break;
            case NODE_REMOVED_FROM_PARENT:
                event.getSource().removeFromParent(event.getChild());
                break;
        }
    }

    @Override
    public boolean isSignificant() {
        return switch (event.getEventType()) {
            case ROOT_CHANGED, NODE_REMOVED_FROM_PARENT, NODE_ADDED_TO_PARENT -> true;
            case SUBTREE_NODES_CHANGED, NODE_CHANGED, NODE_REMOVED_FROM_TREE, NODE_ADDED_TO_TREE -> false;
        };
    }

    @Override
    public String getPresentationName() {
        return "Tree Structure";
    }


}
