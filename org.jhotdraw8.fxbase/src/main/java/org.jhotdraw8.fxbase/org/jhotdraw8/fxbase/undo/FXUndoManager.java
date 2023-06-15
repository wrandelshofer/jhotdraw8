/*
 * @(#)FXUndoManager.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.fxbase.undo;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.jhotdraw8.annotation.NonNull;

import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.UndoManager;

public class FXUndoManager implements UndoableEditListener {
    private final javax.swing.undo.@NonNull UndoManager manager;
    private final @NonNull BooleanProperty canUndo = new SimpleBooleanProperty(this, "canUndo", false);
    private final @NonNull BooleanProperty canRedo = new SimpleBooleanProperty(this, "canRedo", false);
    private final @NonNull StringProperty undoName = new SimpleStringProperty(this, "undoName", "Undo");
    private final @NonNull StringProperty redoName = new SimpleStringProperty(this, "redoName", "Redo");

    private boolean isBusy;

    public FXUndoManager() {
        this.manager = new UndoManager();
    }

    public StringProperty undoPresentationNameProperty() {
        return undoName;
    }

    public StringProperty redoPresentationNameProperty() {
        return redoName;
    }

    @Override
    public void undoableEditHappened(@NonNull UndoableEditEvent e) {
        if (!isBusy) {
            manager.undoableEditHappened(e);
            updateProperties();
        }
    }

    public @NonNull BooleanProperty undoableProperty() {
        return canUndo;
    }

    public @NonNull BooleanProperty redoableProperty() {
        return canRedo;
    }

    private void updateProperties() {
        canUndo.set(manager.canUndo());
        canRedo.set(manager.canRedo());
        undoPresentationNameProperty().set(manager.getUndoPresentationName());
        redoPresentationNameProperty().set(manager.getRedoPresentationName());
    }

    public void redo() {
        if (manager.canRedo()) {
            isBusy = true;
            try {
                manager.redo();
            } finally {
                updateProperties();
                isBusy = false;
            }
        }
    }

    public void undo() {
        if (manager.canUndo()) {
            isBusy = true;
            try {
                manager.undo();
            } finally {
                updateProperties();
                isBusy = false;
            }
        }
    }

    public boolean canUndo() {
        return canUndo.get();
    }

    public boolean canRedo() {
        return canRedo.get();
    }

    public void discardAllEdits() {
        manager.discardAllEdits();
        updateProperties();
    }
}
