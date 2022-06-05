package org.jhotdraw8.app.undo;

public interface UndoableEdit {
    boolean isSignificant();

    String getPresentationName();

    void undo();

    void redo();
}
