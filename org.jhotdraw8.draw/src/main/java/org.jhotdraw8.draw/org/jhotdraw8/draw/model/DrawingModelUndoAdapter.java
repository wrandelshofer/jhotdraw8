/*
 * @(#)DrawingModelUndoEventEmitter.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.draw.model;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.event.Listener;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.fxbase.tree.TreeModelUndoAdapter;
import org.jhotdraw8.fxcollection.typesafekey.Key;

import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

/**
 * Emits {@link UndoableEditEvent}s.
 */
public class DrawingModelUndoAdapter extends TreeModelUndoAdapter<Figure> {
    private @NonNull
    final Listener<DrawingModelEvent> drawingModelListener = new Listener<DrawingModelEvent>() {
        @Override
        public void handle(DrawingModelEvent event) {
            UndoableEdit edit = switch (event.getEventType()) {
                case PROPERTY_VALUE_CHANGED ->
                        new PropertyChangedEdit<>(event.getSource(), event.getNode(), event.getKey(), event.getOldValue(), event.getNewValue(),
                                event.wasAdded(), event.wasRemoved());
                case LAYOUT_CHANGED, STYLE_CHANGED, TRANSFORM_CHANGED -> null;
            };
            if (edit != null) {
                fireUndoableEdit(event.getSource(), edit);
            }
        }
    };

    public DrawingModelUndoAdapter() {
    }

    public DrawingModelUndoAdapter(@NonNull DrawingModel model) {
        bind(model);
    }


    public void bind(@NonNull DrawingModel model) {
        super.bind(model);
        model.addDrawingModelListener(drawingModelListener);
    }


    public void unbind(@NonNull DrawingModel model) {
        super.unbind(model);
        model.removeDrawingModelListener(drawingModelListener);
    }


    class PropertyChangedEdit<E> extends AbstractUndoableEdit {
        private final @NonNull DrawingModel model;
        private final @NonNull Figure figure;
        private final @NonNull Key<Object> key;
        private final @Nullable Object oldValue;
        private final @Nullable Object newValue;
        /**
         * True if the change is the result of an add operation.
         */
        private final boolean wasAdded;
        /**
         * True if the change is the result of a remove operation.
         */
        private final boolean wasRemoved;


        public PropertyChangedEdit(@NonNull DrawingModel model, @NonNull Figure figure, @NonNull Key<Object> key, @Nullable Object oldValue, @Nullable Object newValue, boolean wasAdded, boolean wasRemoved) {
            this.model = model;
            this.figure = figure;
            this.key = key;
            this.oldValue = oldValue;
            this.newValue = newValue;
            this.wasAdded = wasAdded;
            this.wasRemoved = wasRemoved;
        }

        @Override
        public String getPresentationName() {
            return getResourceBundle().getString("edit.Property");
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            if (wasRemoved) {
                model.remove(figure, key);
            } else {
                model.set(figure, key, newValue);
            }
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            if (wasAdded) {
                model.remove(figure, key);
            } else {
                model.set(figure, key, oldValue);
            }
        }
    }
}
