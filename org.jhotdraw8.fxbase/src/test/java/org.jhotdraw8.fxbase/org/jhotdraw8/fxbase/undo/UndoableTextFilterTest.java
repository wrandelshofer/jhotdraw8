package org.jhotdraw8.fxbase.undo;

import javafx.application.Platform;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import org.jhotdraw8.fxbase.concurrent.PlatformUtil;
import org.jhotdraw8.fxbase.control.TextInputControlUndoAdapter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UndoableTextFilterTest {
    @Test
    public void shouldFireChangeEvents() {
        Platform.startup(() -> {
        });

        PlatformUtil.invokeAndWait(10000, () -> {
            TextField textField = new TextField();
            TextInputControlUndoAdapter filter = new TextInputControlUndoAdapter(textField);
            textField.setTextFormatter(new TextFormatter<Object>(filter));
            FXUndoManager undoManager = new FXUndoManager();
            filter.addUndoEditListener(undoManager);

            assertFalse(undoManager.canUndo());
            assertFalse(undoManager.canRedo());

            textField.setText("Lorem ipsum dolor sit amet");
            assertTrue(undoManager.canUndo());
            assertFalse(undoManager.canRedo());
            assertEquals("Lorem ipsum dolor sit amet", textField.getText());

            textField.deleteText(6, 12);
            assertTrue(undoManager.canUndo());
            assertFalse(undoManager.canRedo());
            assertEquals("Lorem dolor sit amet", textField.getText());

            textField.insertText(6, "ipsum ");
            assertTrue(undoManager.canUndo());
            assertFalse(undoManager.canRedo());
            assertEquals("Lorem ipsum dolor sit amet", textField.getText());

            // WHEN The undo manager undoes edits
            undoManager.undo();
            assertEquals("Lorem dolor sit amet", textField.getText());
            assertTrue(undoManager.canUndo());
            assertTrue(undoManager.canRedo());

            undoManager.undo();
            assertEquals("Lorem ipsum dolor sit amet", textField.getText());
            assertTrue(undoManager.canUndo());
            assertTrue(undoManager.canRedo());

            undoManager.undo();
            assertEquals("", textField.getText());
            assertFalse(undoManager.canUndo());
            assertTrue(undoManager.canRedo());

            // WHEN The undo manager redoes edits
            undoManager.redo();
            assertEquals("Lorem ipsum dolor sit amet", textField.getText());
            undoManager.redo();
            assertEquals("Lorem dolor sit amet", textField.getText());
            assertTrue(undoManager.canUndo());
            assertTrue(undoManager.canRedo());

            // WHEN The user inserts text
            textField.insertText(6, "opossum ");
            assertEquals("Lorem opossum dolor sit amet", textField.getText());
            assertTrue(undoManager.canUndo());
            assertFalse(undoManager.canRedo());
        });
    }

}