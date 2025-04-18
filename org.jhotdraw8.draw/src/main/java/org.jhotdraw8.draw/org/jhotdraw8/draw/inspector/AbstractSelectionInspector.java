/*
 * @(#)AbstractSelectionInspector.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.inspector;

import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.SetChangeListener;
import org.jhotdraw8.draw.DrawingEditor;
import org.jhotdraw8.draw.DrawingView;
import org.jhotdraw8.draw.figure.Drawing;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.draw.model.DrawingModel;
import org.jhotdraw8.fxbase.undo.UndoableEditHelper;
import org.jspecify.annotations.Nullable;

import javax.swing.event.UndoableEditEvent;
import java.util.Collections;
import java.util.Set;

/**
 * AbstractSelectionInspector.
 *
 */
@SuppressWarnings("this-escape")
public abstract class AbstractSelectionInspector extends AbstractInspector<DrawingView> {

    {
        subject.addListener(this::onDrawingViewChanged);
    }

    public AbstractSelectionInspector() {
    }

    protected @Nullable Drawing getDrawing() {
        DrawingView subject = getSubject();
        return subject == null ? null : subject.getDrawing();
    }

    protected @Nullable DrawingModel getModel() {
        DrawingView subject = getSubject();
        return subject == null ? null : subject.getModel();
    }


    private final SetChangeListener<Figure> selectionListener = change -> {
        DrawingView drawingView = getSubject();
        onSelectionChanged(drawingView == null ? FXCollections.emptyObservableSet() : drawingView.getSelectedFigures());
    };


    protected void onDrawingViewChanged(ObservableValue<? extends DrawingView> observable, @Nullable DrawingView oldValue, @Nullable DrawingView newValue) {
        if (oldValue != null) {
            oldValue.selectedFiguresProperty().removeListener(selectionListener);
        }
        if (newValue != null) {
            newValue.selectedFiguresProperty().addListener(selectionListener);
        }
    }

    protected Set<Figure> getSelectedFigures() {
        DrawingView drawingView = getSubject();
        return drawingView == null ? Collections.emptySet() : drawingView.getSelectedFigures();
    }

    /**
     * Must be implemented by subclasses.
     *
     * @param newValue the new selection
     */
    protected abstract void onSelectionChanged(Set<Figure> newValue);

    protected final UndoableEditHelper undoHelper = new UndoableEditHelper(this, this::forwardUndoableEdit);

    protected void forwardUndoableEdit(UndoableEditEvent event) {
        DrawingView s = getSubject();
        DrawingEditor editor = s == null ? null : s.getEditor();
        if (editor != null) {
            editor.getUndoManager().undoableEditHappened(event);
        }
    }
}
