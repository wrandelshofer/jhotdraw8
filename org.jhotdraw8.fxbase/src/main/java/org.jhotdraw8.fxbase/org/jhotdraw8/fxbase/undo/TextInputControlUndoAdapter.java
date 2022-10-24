package org.jhotdraw8.fxbase.undo;

import javafx.scene.control.IndexRange;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextInputControl;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.UnaryOperator;

/**
 * This text filter can be set on a {@link javafx.scene.control.TextInputControl}
 * to support undo/redo with a {@link FXUndoManager}.
 */
public class TextInputControlUndoAdapter implements UnaryOperator<TextFormatter.Change> {
    private final @NonNull CopyOnWriteArrayList<UndoableEditListener> listeners = new CopyOnWriteArrayList<>();

    TextInputControlUndoAdapter() {
    }

    /**
     * Attaches an undo manager to the specified text input control
     * by setting a {@link TextFormatter} on it.
     *
     * @param control the control
     * @param manager the manager
     */
    public static void attach(@NonNull TextInputControl control, @NonNull FXUndoManager manager) {
        TextInputControlUndoAdapter filter = new TextInputControlUndoAdapter();
        control.setTextFormatter(new TextFormatter<Object>(filter));
        filter.addUndoEditListener(manager);
    }

    public void addUndoEditListener(@NonNull UndoableEditListener listener) {
        listeners.add(listener);
    }

    @Override
    public @NonNull TextFormatter.Change apply(@NonNull TextFormatter.Change change) {
        if (!listeners.isEmpty()) {
            String deletedText = change.isDeleted() ? change.getControlText().substring(change.getRangeStart(), change.getRangeEnd()) : null;
            String addedText = change.isAdded() ? change.getText() : null;
            UndoableEditEvent event = new UndoableEditEvent(this, new UndoableTextEdit((TextInputControl) change.getControl(),
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

    public void removeUndoEditListener(UndoableEditListener listener) {
        listeners.remove(listener);
    }

    static class UndoableTextEdit implements UndoableEdit {
        private @Nullable TextInputControl control;
        private @NonNull String addedText;
        private @NonNull String deletedText;
        private int start;
        private int end;

        private int newAnchor;
        private int newCaret;

        private final int oldAnchor;
        private final int oldCaret;

        private boolean hasBeenDone = true;

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
            if (!canUndo()) {
                throw new CannotUndoException();
            }
            assert control != null;
            if (isAdded()) {
                control.deleteText(start, start + addedText.length());
            }
            if (isDeleted()) {
                control.insertText(start, deletedText);
            }
            control.selectRange(oldAnchor, oldCaret);
            hasBeenDone = false;
        }

        @Override
        public boolean canUndo() {
            return isAlive() && hasBeenDone;
        }

        @Override
        public void redo() throws CannotRedoException {
            if (!canRedo()) {
                throw new CannotRedoException();
            }
            assert control != null;
            if (isDeleted()) {
                control.deleteText(new IndexRange(start, end));
            }
            if (isAdded()) {
                control.insertText(start, addedText);
            }
            control.selectRange(newAnchor, newCaret);
            hasBeenDone = true;
        }

        @Override
        public boolean canRedo() {
            return isAlive() && !hasBeenDone;
        }

        private boolean isAlive() {
            return control != null;
        }

        @Override
        public void die() {
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
        public String getUndoPresentationName() {
            return "Undo Typing";
        }

        @Override
        public String getRedoPresentationName() {
            return "Redo Typing";
        }

        @Override
        public String toString() {
            return "UndoableTextEdit{" +
                    "control=" + control +
                    ", addedText='" + addedText + '\'' +
                    ", deletedText='" + deletedText + '\'' +
                    ", start=" + start +
                    ", end=" + end +
                    ", newAnchor=" + newAnchor +
                    ", newCaret=" + newCaret +
                    ", oldAnchor=" + oldAnchor +
                    ", oldCaret=" + oldCaret +
                    ", hasBeenDone=" + hasBeenDone +
                    '}';
        }
    }
}
