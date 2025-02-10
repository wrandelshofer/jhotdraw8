/*
 * @(#)AbstractDrawingInspector.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.inspector;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.jhotdraw8.draw.DrawingView;
import org.jhotdraw8.draw.figure.Drawing;
import org.jhotdraw8.draw.model.DrawingModel;
import org.jhotdraw8.fxbase.undo.UndoableEditHelper;
import org.jspecify.annotations.Nullable;

import javax.swing.event.UndoableEditEvent;

/**
 * AbstractDrawingInspector.
 *
 */
public abstract class AbstractDrawingInspector extends AbstractInspector<DrawingView> {
    protected @Nullable DrawingModel drawingModel;
    protected @Nullable Drawing drawing;

    private final ChangeListener<Drawing> drawingListener = this::onDrawingChanged;
    private final ChangeListener<DrawingModel> modelListener = this::onDrawingModelChanged;

    {
        subject.addListener(this::onDrawingViewChanged);
    }

    public AbstractDrawingInspector() {
    }

    protected void onDrawingViewChanged(ObservableValue<? extends DrawingView> observable, @Nullable DrawingView oldValue, @Nullable DrawingView newValue) {
        Drawing oldDrawing = drawing;
        DrawingModel oldModel = drawingModel;
        if (oldValue != null) {
            oldValue.modelProperty().removeListener(modelListener);
            oldValue.drawingProperty().removeListener(drawingListener);
            oldDrawing = oldValue.getDrawing();
        }
        Drawing newDrawing = null;
        if (newValue != null) {
            newValue.drawingProperty().addListener(drawingListener);
            newValue.modelProperty().addListener(modelListener);
            newDrawing = newValue.getDrawing();
            drawingModel = newValue.getModel();
        }
        onDrawingModelChanged(null, oldModel, drawingModel);
        onDrawingChanged(null, oldDrawing, newDrawing);
    }

    protected DrawingModel getDrawingModel() {
        return getSubject().getModel();
    }

    protected Drawing getDrawing() {
        return getSubject().getDrawing();
    }

    protected DrawingModel getModel() {
        return getSubject().getModel();
    }


    /**
     * Must be implemented by subclasses.
     *
     * @param observable
     * @param oldValue   the old drawing
     * @param newValue   the new drawing
     */
    protected abstract void onDrawingChanged(@Nullable ObservableValue<? extends Drawing> observable, @Nullable Drawing oldValue, @Nullable Drawing newValue);

    /**
     * Can be overriden by subclasses.
     * This implementation is empty.
     *
     * @param observable
     * @param oldValue   the old drawing model
     * @param newValue   the new drawing model
     */
    protected void onDrawingModelChanged(@Nullable ObservableValue<? extends DrawingModel> observable, @Nullable DrawingModel oldValue, @Nullable DrawingModel newValue) {

    }

    protected final UndoableEditHelper undoHelper = new UndoableEditHelper(this, this::forwardUndoableEditEvent);

    protected void forwardUndoableEditEvent(UndoableEditEvent undoableEditEvent) {

    }

    public UndoableEditHelper getUndoHelper() {
        return undoHelper;
    }
}
