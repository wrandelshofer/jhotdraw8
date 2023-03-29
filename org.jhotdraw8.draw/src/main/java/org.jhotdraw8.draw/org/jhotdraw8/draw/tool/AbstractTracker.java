/*
 * @(#)AbstractTracker.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.tool;

import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
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

    protected final @NonNull BorderPane node = new BorderPane();
    protected @Nullable CompositeEdit compositeEdit;

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
        if (compositeEdit == null) {
            compositeEdit = new CompositeEdit();
            DrawingEditor editor = view.getEditor();
            if (editor != null) {
                editor.getUndoManager().undoableEditHappened(new UndoableEditEvent(this, compositeEdit));
            }
        }
    }

    protected void stopCompositeEdit(DrawingView view) {
        if (compositeEdit != null) {
            DrawingEditor editor = view.getEditor();
            if (editor != null) {
                editor.getUndoManager().undoableEditHappened(new UndoableEditEvent(this, compositeEdit));
            }
            compositeEdit = null;
        }
    }
}
