/*
 * @(#)AbstractDrawingViewInspector.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.inspector;

import javafx.beans.value.ObservableValue;
import org.jhotdraw8.draw.DrawingEditor;
import org.jhotdraw8.draw.DrawingView;
import org.jhotdraw8.draw.model.DrawingModel;
import org.jhotdraw8.fxbase.undo.UndoableEditHelper;
import org.jspecify.annotations.Nullable;

import javax.swing.event.UndoableEditEvent;

/**
 * AbstractDrawingInspector.
 *
 */
public abstract class AbstractDrawingViewInspector extends AbstractInspector<DrawingView> {


    protected final UndoableEditHelper undoHelper = new UndoableEditHelper(this, this::forwardUndoableEdit);

    {
        subject.addListener(this::onDrawingViewChanged);
    }

    public AbstractDrawingViewInspector() {
    }

    private void forwardUndoableEdit(UndoableEditEvent event) {
        final DrawingView s = getSubject();
        final DrawingEditor editor = s == null ? null : s.getEditor();
        if (editor != null) {
            editor.getUndoManager().undoableEditHappened(event);
        }
    }

    protected DrawingModel getDrawingModel() {
        return getSubject().getModel();
    }

    /**
     * Can be overridden by subclasses. This implementation is empty.
     *
     * @param observable
     * @param oldValue   the old drawing view
     * @param newValue   the new drawing view
     */
    protected void onDrawingViewChanged(@Nullable ObservableValue<? extends DrawingView> observable, @Nullable DrawingView oldValue, @Nullable DrawingView newValue) {

    }
}
