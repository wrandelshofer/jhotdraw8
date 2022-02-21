package org.jhotdraw8.draw.model;

import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.event.Listener;
import org.jhotdraw8.tree.TreeModelEvent;

import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

/**
 * Emits {@link UndoableEditEvent}s.
 */
public class DrawingModelUndoEventEmitter {

    protected void fire(UndoableEdit event) {
    }


    private class DrawingModelListener implements Listener<DrawingModelEvent> {
        private final static long serialVersionUID = 0L;
        @Override
        public void handle(DrawingModelEvent event) {
            switch (event.getEventType()) {
                case PROPERTY_VALUE_CHANGED:
                    fire(new AbstractUndoableEdit() {
                        private final static long serialVersionUID = 0L;
                        @Override
                        public void undo() throws CannotUndoException {
                            super.undo();
                            event.getSource().set(event.getNode(), event.getKey(), event.getOldValue());
                        }

                        @Override
                        public void redo() throws CannotRedoException {
                            super.redo();
                            event.getSource().set(event.getNode(), event.getKey(), event.getNewValue());
                        }
                    });
                    break;
                case LAYOUT_CHANGED:
                    break;
                case STYLE_CHANGED:
                    break;
                case TRANSFORM_CHANGED:
                    break;
            }
        }
    }

    private class TreeModelListener implements Listener<TreeModelEvent<Figure>> {
        private final static long serialVersionUID = 0L;

        @Override
        public void handle(TreeModelEvent<Figure> event) {
            switch (event.getEventType()) {
                case ROOT_CHANGED:
                    fire(new AbstractUndoableEdit() {
                        private final static long serialVersionUID = 0L;

                        @Override
                        public void undo() throws CannotUndoException {
                            super.undo();
                            event.getSource().setRoot(event.getParent());
                        }

                        @Override
                        public void redo() throws CannotRedoException {
                            super.redo();
                            event.getSource().setRoot(event.getRoot());
                        }
                    });
                    break;
                case SUBTREE_NODES_CHANGED:
                case NODE_ADDED_TO_PARENT:
                case NODE_REMOVED_FROM_PARENT:
                case NODE_ADDED_TO_TREE:
                case NODE_REMOVED_FROM_TREE:
                    fire(new AbstractUndoableEdit() {
                        private final static long serialVersionUID = 0L;
                        // can not undo/redo yet
                    });
                    break;
                case NODE_CHANGED:
                    break;
            }

        }
    }
}
