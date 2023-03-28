/*
 * @(#)AbstractTracker.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.tool;

import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.application.AbstractDisableable;
import org.jhotdraw8.draw.DrawingEditor;
import org.jhotdraw8.draw.DrawingView;
import org.jhotdraw8.fxbase.undo.CompositeEdit;

import javax.swing.event.UndoableEditEvent;

/**
 * AbstractAction.
 *
 * @author Werner Randelshofer
 */
public abstract class AbstractTracker extends AbstractDisableable implements Tracker {

    protected final BorderPane node = new BorderPane();
    protected CompositeEdit undoableEdit;

    /**
     * Creates a new instance.
     */
    public AbstractTracker() {

    }

    @Override
    public @NonNull Node getNode() {
        return node;
    }

    protected void startCompositeEdit(DrawingView view) {
        if (undoableEdit == null) {
            undoableEdit = new CompositeEdit();
            DrawingEditor editor = view.getEditor();
            if (editor != null) {
                editor.getUndoManager().undoableEditHappened(new UndoableEditEvent(this, undoableEdit));
            }
        }
    }

    protected void stopCompositeEdit(DrawingView view) {
        if (undoableEdit != null) {
            DrawingEditor editor = view.getEditor();
            if (editor != null) {
                editor.getUndoManager().undoableEditHappened(new UndoableEditEvent(this, undoableEdit));
            }
            undoableEdit = null;
        }
    }
}
