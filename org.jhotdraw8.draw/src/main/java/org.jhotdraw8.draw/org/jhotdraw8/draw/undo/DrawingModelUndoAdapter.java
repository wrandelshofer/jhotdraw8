/*
 * @(#)DrawingModelUndoAdapter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.draw.undo;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.event.Listener;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.draw.model.DrawingModel;
import org.jhotdraw8.draw.model.DrawingModelEvent;
import org.jhotdraw8.fxbase.tree.TreeModelUndoAdapter;
import org.jhotdraw8.fxcollection.typesafekey.Key;

import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import java.text.MessageFormat;

/**
 * Emits {@link UndoableEditEvent}s.
 */
public class DrawingModelUndoAdapter extends TreeModelUndoAdapter<Figure> {

    private final @NonNull Listener<DrawingModelEvent> drawingModelListener = new Listener<DrawingModelEvent>() {
        @Override
        public void handle(DrawingModelEvent event) {
            if (event.getSource().isValidating()) {
                return;
            }
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
        private @Nullable Object newValue;
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
            return MessageFormat.format(getResourceBundle().getString("edit.changePropertyValue"), key.getName());
        }

        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            if (wasRemoved && !wasAdded) {
                model.remove(figure, key);
            } else {
                model.set(figure, key, newValue);
            }
        }

        @Override
        public boolean addEdit(UndoableEdit anEdit) {
            if (anEdit instanceof PropertyChangedEdit<?> that
                    && this.figure.equals(that.figure)
                    && this.key.equals(that.key)
                    && !this.wasAdded && !this.wasRemoved
                    && !that.wasAdded && !that.wasRemoved) {
                this.newValue = that.newValue;
                return true;
            }
            return false;
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            if (wasAdded && !wasRemoved) {
                model.remove(figure, key);
            } else {
                model.set(figure, key, oldValue);
            }
        }

        @Override
        public String toString() {
            return "PropertyChangedEdit{" +
                    "figure=" + figure.getId() +
                    ", " + key.getName() +
                    "=" + newValue +
                    '}';
        }
    }
}
