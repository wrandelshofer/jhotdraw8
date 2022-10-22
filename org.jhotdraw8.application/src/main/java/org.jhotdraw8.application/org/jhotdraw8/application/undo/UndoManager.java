package org.jhotdraw8.application.undo;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.base.event.Listener;

public interface UndoManager extends Listener<UndoableEdit> {
    default boolean isUndoable() {
        return undoableProperty().get();
    }

    default boolean isRedoable() {
        return redoableProperty().get();
    }

    @NonNull ReadOnlyBooleanProperty undoableProperty();

    @NonNull ReadOnlyBooleanProperty redoableProperty();

    @NonNull ReadOnlyStringProperty undoPresentationNameProperty();

    @NonNull ReadOnlyStringProperty redoPresentationNameProperty();

    void undo();

    void redo();

    @NonNull IntegerProperty limitProperty();

    default int getLimit() {
        return limitProperty().get();
    }

    default void setLimit(int newValue) {
        limitProperty().set(newValue);
    }
}
