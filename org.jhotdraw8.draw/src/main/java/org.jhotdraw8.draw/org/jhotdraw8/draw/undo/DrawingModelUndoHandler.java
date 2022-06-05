package org.jhotdraw8.draw.undo;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.draw.model.DrawingModel;
import org.jhotdraw8.draw.model.DrawingModelEvent;
import org.jhotdraw8.event.Listener;
import org.jhotdraw8.tree.TreeModelEvent;

import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.UndoableEdit;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This class generates undoable edit events for a {@link DrawingModel}.
 */
public class DrawingModelUndoHandler {
    private final @NonNull Listener<DrawingModelEvent> drawingModelListener = this::handleDrawingModelEvent;
    private final @NonNull Listener<TreeModelEvent<Figure>> treeModelListener = this::handleTreeModelEvent;
    private final @NonNull ObjectProperty<DrawingModel> model = new SimpleObjectProperty<>();
    private final @NonNull CopyOnWriteArrayList<UndoableEditListener> listeners = new CopyOnWriteArrayList<UndoableEditListener>();

    {
        model.addListener((o, oldv, newv) -> {
            if (oldv != null) {
                newv.removeDrawingModelListener(drawingModelListener);
                newv.removeTreeModelListener(treeModelListener);
            }
            if (newv != null) {
                newv.addDrawingModelListener(drawingModelListener);
                newv.addTreeModelListener(treeModelListener);
            }
        });
    }

    public DrawingModel getModel() {
        return model.get();
    }

    public @NonNull ObjectProperty<DrawingModel> modelProperty() {
        return model;
    }

    public void setModel(DrawingModel model) {
        this.model.set(model);
    }

    protected void handleDrawingModelEvent(DrawingModelEvent event) {
        fire(new DrawingModelEventUndoableEdit(event));
    }

    private void fire(@NonNull UndoableEdit edit) {
        if (listeners.isEmpty()) {
            return;
        }
        UndoableEditEvent event = new UndoableEditEvent(this, edit);
        for (UndoableEditListener listener : listeners) {
            listener.undoableEditHappened(event);
        }
    }

    public void handleTreeModelEvent(TreeModelEvent<Figure> event) {
        fire(new TreeModelEventUndoableEdit<>(event));
    }

    public void addUndoableEditListener(final @NonNull UndoableEditListener listener) {
        listeners.add(listener);
    }

    public void removeUndoableEditListener(final @NonNull UndoableEditListener listener) {
        listeners.remove(listener);
    }

}
