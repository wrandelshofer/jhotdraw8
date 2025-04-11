/*
 * @(#)StyleAttributesInspector.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.inspector;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.base.event.Listener;
import org.jhotdraw8.css.manager.StylesheetsManager;
import org.jhotdraw8.css.model.SelectorModel;
import org.jhotdraw8.draw.DrawingEditor;
import org.jhotdraw8.draw.DrawingView;
import org.jhotdraw8.draw.css.model.FigureSelectorModel;
import org.jhotdraw8.draw.figure.Drawing;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.draw.model.DrawingModel;
import org.jhotdraw8.fxbase.styleable.WritableStyleableMapAccessor;
import org.jhotdraw8.fxbase.tree.TreeModelEvent;
import org.jspecify.annotations.Nullable;

import javax.swing.event.UndoableEditEvent;

/**
 * FXML Controller class
 *
 */
public class StyleAttributesInspector extends AbstractStyleAttributesInspector<Figure>
        implements Inspector<DrawingView> {

    protected final ObjectProperty<DrawingView> subject = new SimpleObjectProperty<>(this, SUBJECT_PROPERTY);

    public StyleAttributesInspector() {
    }



    @Override
    public ObjectProperty<DrawingView> subjectProperty() {
        return subject;
    }

    private final InvalidationListener modelInvalidationHandler = this::invalidateTextArea;

    private final Listener<TreeModelEvent<Figure>> treeModelListener = event -> invalidateTextArea(event.getSource());

    private final ChangeListener<DrawingModel> modelChangeHandler = (ObservableValue<? extends DrawingModel> observable, DrawingModel oldValue, DrawingModel newValue) -> {
        if (oldValue != null) {
            oldValue.removeListener(modelInvalidationHandler);
            oldValue.removeTreeModelListener(treeModelListener);
        }
        if (newValue != null) {
            newValue.addListener(modelInvalidationHandler);
            newValue.addTreeModelListener(treeModelListener);
        }
    };

    {
        subject.addListener(this::onDrawingViewChanged);
    }

    @Override
    protected void fireInvalidated(Figure f) {
        DrawingModel m = getDrawingModel();
        m.fireNodeInvalidated(f);
        m.fireTransformInvalidated(f);
        m.fireLayoutInvalidated(f);

    }

    @Override
    protected @Nullable Object get(Figure f, WritableStyleableMapAccessor<Object> finalSelectedAccessor) {
        return getDrawingModel().get(f, finalSelectedAccessor);
    }

    @Override
    protected @Nullable WritableStyleableMapAccessor<?> getAccessor(SelectorModel<Figure> selectorModel, Figure f, String propertyNamespace, String propertyName) {
        if (selectorModel instanceof FigureSelectorModel m) {
            return m.getAccessor(f, propertyNamespace, propertyName);
        }
        return null;
    }

    @Override
    protected @Nullable Converter<?> getConverter(SelectorModel<Figure> selectorModel, Figure f, String namespace, String name) {
        if (selectorModel instanceof FigureSelectorModel m) {
            return m.getConverter(f, namespace, name);
        }
        return null;
    }

    protected @Nullable Drawing getDrawing() {
        DrawingView view = getSubject();
        return view == null ? null : view.getDrawing();
    }

    protected @Nullable DrawingModel getDrawingModel() {
        DrawingView view = getSubject();
        return view == null ? null : view.getModel();
    }

    @Override
    protected Iterable<Figure> getEntities() {
        return getDrawing().breadthFirstIterable();
    }

    @Override
    protected @Nullable Figure getRoot() {
        DrawingView subject = getSubject();
        return subject == null ? null : subject.getDrawing();
    }

    @Override
    protected @Nullable StylesheetsManager<Figure> getStyleManager() {
        Drawing d = getDrawing();
        return d == null ? null : d.getStyleManager();
    }

    /**
     * Can be overridden by subclasses. This implementation is empty.
     *
     * @param observable
     * @param oldValue   the old drawing view
     * @param newValue   the new drawing view
     */
    protected void onDrawingViewChanged(ObservableValue<? extends DrawingView> observable, @Nullable DrawingView oldValue, @Nullable DrawingView newValue) {
        if (oldValue != null) {
            oldValue.modelProperty().removeListener(modelChangeHandler);
            modelChangeHandler.changed(oldValue.modelProperty(), oldValue.getModel(), null);
            selectionProperty().unbind();
        }
        if (newValue != null) {
            newValue.modelProperty().addListener(modelChangeHandler);
            modelChangeHandler.changed(newValue.modelProperty(), null, newValue.getModel());
            invalidateTextArea(observable);
            selectionProperty().bind(newValue.selectedFiguresProperty());
        }
    }

    @Override
    protected void remove(Figure f, WritableStyleableMapAccessor<Object> finalSelectedAccessor) {
        getDrawingModel().remove(f, finalSelectedAccessor);
    }

    @Override
    protected void set(Figure f, WritableStyleableMapAccessor<Object> finalSelectedAccessor, Object o) {
        getDrawingModel().set(f, finalSelectedAccessor, o);
    }

    @Override
    protected void setHelpText(String helpText) {
        DrawingView view = getSubject();
        DrawingEditor editor = view == null ? null : view.getEditor();
        if (editor != null) {
            editor.setHelpText(helpText);
        }
    }

    @Override
    protected void showSelection() {
        DrawingView drawingView = getSubject();
        if (drawingView != null) {
            drawingView.scrollSelectedFiguresToVisible();
            drawingView.jiggleHandles();
        }
    }

    @Override
    protected void recreateHandles() {
        DrawingView drawingView = getSubject();
        if (drawingView != null) {
            drawingView.recreateHandles();
        }
    }

    @Override
    protected void forwardUndoableEdit(UndoableEditEvent event) {
        final DrawingView s = getSubject();
        final DrawingEditor editor = s == null ? null : s.getEditor();
        if (editor != null) {
            editor.getUndoManager().undoableEditHappened(event);
        }
    }
}
