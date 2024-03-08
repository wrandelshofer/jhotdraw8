/*
 * @(#)TextInputControlUndoAdapter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.fxbase.control;

import javafx.scene.control.IndexRange;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextInputControl;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.fxbase.undo.FXUndoManager;

import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.UnaryOperator;

/**
 * This adapter can be bound to a {@link TextInputControl}
 * to support undo/redo with a {@link FXUndoManager}.
 * <p>
 * This text filter can be added to multiple text input controls.
 * If you do this, make sure that you add the {@link FXUndoManager}
 * only once as a listener.
 */
public class TextInputControlUndoAdapter implements UnaryOperator<TextFormatter.Change> {
    private final @NonNull CopyOnWriteArrayList<UndoableEditListener> listeners = new CopyOnWriteArrayList<>();

    public TextInputControlUndoAdapter() {
    }

    public TextInputControlUndoAdapter(@NonNull TextInputControl control) {
        bind(control);
    }

    public void bind(@NonNull TextInputControl control) {
        control.setTextFormatter(new TextFormatter<>(this));
    }

    public void unbind(@NonNull TextInputControl control) {
        control.setTextFormatter(null);
    }

    public void addUndoEditListener(@NonNull UndoableEditListener listener) {
        listeners.add(listener);
    }

    @Override
    public TextFormatter.@NonNull Change apply(TextFormatter.@NonNull Change change) {
        if (!listeners.isEmpty()) {
            String deletedText = change.isDeleted() ? change.getControlText().substring(change.getRangeStart(), change.getRangeEnd()) : null;
            String addedText = change.isAdded() ? change.getText() : null;
            UndoableEditEvent event = new UndoableEditEvent(change.getControl(),
                    new UndoableTextEdit((TextInputControl) change.getControl(),
                            addedText,
                            deletedText,
                            change.getRangeStart(),
                            change.getRangeEnd(),
                            change.getAnchor(),
                            change.getCaretPosition(),
                            change.getControlAnchor(),
                            change.getControlCaretPosition()));
            listeners.forEach(l -> l.undoableEditHappened(event));
        }
        return change;
    }

    public void removeUndoEditListener(@NonNull UndoableEditListener listener) {
        listeners.remove(listener);
    }


    static class UndoableTextEdit extends AbstractUndoableEdit {
        private @Nullable TextInputControl control;
        private @NonNull String addedText;
        private @NonNull String deletedText;
        private int start;
        private int end;

        private int newAnchor;
        private int newCaret;

        private final int oldAnchor;
        private final int oldCaret;

        public UndoableTextEdit(@NonNull TextInputControl control, @Nullable String addedText, @Nullable String deletedText, int start, int end, int newAnchor, int newCaret, int oldAnchor, int oldCaret) {
            this.control = control;
            this.addedText = addedText == null ? "" : addedText;
            this.deletedText = deletedText == null ? "" : deletedText;
            this.start = start;
            this.end = end;
            this.newAnchor = newAnchor;
            this.newCaret = newCaret;
            this.oldAnchor = oldAnchor;
            this.oldCaret = oldCaret;
        }

        private boolean isDeleted() {
            return start != end;
        }

        private boolean isAdded() {
            return !addedText.isEmpty();
        }

        @Override
        public void undo() throws CannotUndoException {
            super.undo();
            TextInputControl c = control;
            assert c != null;
            if (isAdded()) {
                c.deleteText(start, start + addedText.length());
            }
            if (isDeleted()) {
                c.insertText(start, deletedText);
            }
            c.selectRange(oldAnchor, oldCaret);
        }


        @Override
        public void redo() throws CannotRedoException {
            super.redo();
            TextInputControl c = control;
            assert c != null;
            if (isDeleted()) {
                c.deleteText(new IndexRange(start, end));
            }
            if (isAdded()) {
                c.insertText(start, addedText);
            }
            c.selectRange(newAnchor, newCaret);
        }


        @Override
        public void die() {
            super.die();
            control = null;
        }

        @Override
        public boolean addEdit(UndoableEdit anEdit) {
            if (anEdit instanceof UndoableTextEdit e
                    && e.control == this.control) {
                if (!isSignificant() && !e.isSignificant()) {
                    newAnchor = e.newAnchor;
                    newCaret = e.newCaret;
                    e.die();
                    return true;
                } else if (isAdded() && !isDeleted() && e.isAdded() && !e.isDeleted()
                        && start + addedText.length() == e.start
                        && Character.isWhitespace(addedText.charAt(addedText.length() - 1))
                        == Character.isWhitespace(e.addedText.charAt(0))) {
                    addedText = addedText + e.addedText;
                    newAnchor = e.newAnchor;
                    newCaret = e.newCaret;
                    e.die();
                    return true;
                } else if (!isAdded() && isDeleted() && !e.isAdded() && e.isDeleted()) {
                    // Forward delete:
                    if (start == e.start
                            && Character.isWhitespace(deletedText.charAt(deletedText.length() - 1))
                            == Character.isWhitespace(e.deletedText.charAt(0))) {
                        deletedText = deletedText + e.deletedText;
                        end = start + deletedText.length();
                        newAnchor = e.newAnchor;
                        newCaret = e.newCaret;
                        e.die();
                        return true;
                    }
                    // Backward delete:
                    if (start == e.end
                            && Character.isWhitespace(deletedText.charAt(deletedText.length() - 1))
                            == Character.isWhitespace(e.deletedText.charAt(0))) {
                        deletedText = e.deletedText + deletedText;
                        start = end - deletedText.length();
                        newAnchor = e.newAnchor;
                        newCaret = e.newCaret;
                        e.die();
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public boolean replaceEdit(UndoableEdit anEdit) {
            return false;
        }

        @Override
        public boolean isSignificant() {
            return isDeleted() || isAdded();
        }

        @Override
        public String getPresentationName() {
            return "Typing";
        }

        @Override
        public String toString() {
            return super.toString()
                    + " control: " + control
                    + " addedText: '" + addedText + '\''
                    + " deletedText: '" + deletedText + '\''
                    + " start: " + start
                    + " end: " + end
                    + " newAnchor: " + newAnchor
                    + " newCaret: " + newCaret
                    + " oldAnchor: " + oldAnchor
                    + " oldCaret: " + oldCaret;
        }
    }
}
