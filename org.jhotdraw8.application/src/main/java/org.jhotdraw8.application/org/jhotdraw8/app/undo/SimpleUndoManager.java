package org.jhotdraw8.app.undo;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.app.ApplicationLabels;

import java.util.ArrayList;
import java.util.List;

public class SimpleUndoManager implements UndoManager {
    private final @NonNull ReadOnlyBooleanWrapper undoable = new ReadOnlyBooleanWrapper(this, "undoable", false);
    private final @NonNull ReadOnlyBooleanWrapper redoable = new ReadOnlyBooleanWrapper(this, "redoable", false);
    private final @NonNull IntegerProperty limit = new SimpleIntegerProperty(this, "limit", 100);
    private final @NonNull ReadOnlyStringWrapper undoPresentationName = new ReadOnlyStringWrapper(this, "undoPresentationName", ApplicationLabels.getResources().getString("edit.undo.text"));
    private final @NonNull ReadOnlyStringWrapper redoPresentationName = new ReadOnlyStringWrapper(this, "redoPresentationName", ApplicationLabels.getResources().getString("edit.redo.text"));

    private final @NonNull List<UndoableEdit> events = new ArrayList<>();

    @Override
    public void handle(UndoableEdit event) {

    }

    @Override
    public IntegerProperty limitProperty() {
        return limit;
    }

    @Override
    public void redo() {

    }

    public final ReadOnlyStringProperty redoPresentationNameProperty() {
        return undoPresentationName.getReadOnlyProperty();
    }

    public final ReadOnlyBooleanProperty redoableProperty() {
        return redoable.getReadOnlyProperty();
    }

    @Override
    public void undo() {

    }

    public final ReadOnlyStringProperty undoPresentationNameProperty() {
        return undoPresentationName.getReadOnlyProperty();
    }

    public final ReadOnlyBooleanProperty undoableProperty() {
        return undoable.getReadOnlyProperty();
    }

}
