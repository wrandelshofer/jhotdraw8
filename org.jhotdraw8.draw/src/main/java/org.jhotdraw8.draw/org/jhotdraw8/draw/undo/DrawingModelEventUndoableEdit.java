/*
 * @(#)DrawingModelEventUndoableEdit.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.draw.undo;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.draw.model.DrawingModelEvent;

import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import java.io.Serial;

public class DrawingModelEventUndoableEdit extends AbstractUndoableEdit {
    @Serial
    private static final long serialVersionUID = 0L;
    private final @NonNull DrawingModelEvent event;

    public DrawingModelEventUndoableEdit(@NonNull DrawingModelEvent event) {
        this.event = event;
    }

    @Override
    public void undo() throws CannotUndoException {
        if (event.getEventType() == DrawingModelEvent.EventType.PROPERTY_VALUE_CHANGED) {
            event.getSource().set(event.getNode(), event.getKey(), event.getOldValue());
        }
    }

    @Override
    public void redo() throws CannotRedoException {
        if (event.getEventType() == DrawingModelEvent.EventType.PROPERTY_VALUE_CHANGED) {
            event.getSource().set(event.getNode(), event.getKey(), event.getNewValue());
        }
    }

    @Override
    public boolean isSignificant() {
        return event.getEventType() == DrawingModelEvent.EventType.PROPERTY_VALUE_CHANGED;
    }

    @Override
    public String getPresentationName() {
        return "Property Value";
    }


}
