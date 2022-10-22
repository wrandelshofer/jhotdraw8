package org.jhotdraw8.application.undo;

public interface UndoableEdit {
    boolean isSignificant();

    String getPresentationName();

    void undo();

    void redo();
}
