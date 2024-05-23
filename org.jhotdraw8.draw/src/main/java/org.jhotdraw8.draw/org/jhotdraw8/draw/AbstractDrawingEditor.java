/*
 * @(#)AbstractDrawingEditor.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SetProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleSetProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.SetChangeListener;
import org.jspecify.annotations.Nullable;
import org.jhotdraw8.base.event.Listener;
import org.jhotdraw8.draw.css.value.CssColor;
import org.jhotdraw8.draw.handle.HandleType;
import org.jhotdraw8.draw.tool.Tool;
import org.jhotdraw8.draw.tool.ToolEvent;
import org.jhotdraw8.fxbase.beans.NonNullObjectProperty;
import org.jhotdraw8.fxbase.undo.FXUndoManager;

import java.util.HashSet;

public abstract class AbstractDrawingEditor implements DrawingEditor {
    @SuppressWarnings("this-escape")
    private final ObjectProperty<String> helpText = new SimpleObjectProperty<>(this, HELP_TEXT_PROPERTY);
    @SuppressWarnings("this-escape")
    private final NonNullObjectProperty<FXUndoManager> undoManager = new NonNullObjectProperty<>(this, UNDO_MANAGER_PROPERTY, new FXUndoManager());
    private final DoubleProperty handleSize = new SimpleDoubleProperty(
            this, HANDLE_SIZE_PROPERTY, 5.0) {
        @Override
        public void set(double newValue) {
            super.set(newValue);
            recreateHandles();
        }
    };
    private final DoubleProperty tolerance = new SimpleDoubleProperty(
            this, TOLERANCE_PROPERTY, 5.0) {
        @Override
        public void set(double newValue) {
            super.set(newValue);
            recreateHandles();
        }
    };
    private final DoubleProperty handleStrokeWidth = new SimpleDoubleProperty(
            this, HANDLE_STROKE_WDITH_PROPERTY, 1.0) {
        @Override
        public void set(double newValue) {
            super.set(newValue);
            recreateHandles();
        }
    };
    private final NonNullObjectProperty<CssColor> handleColor = new NonNullObjectProperty<>(this, HANDLE_COLOR_PROPERTY,
            CssColor.valueOf("blue")) {
        @Override
        public void set(CssColor newValue) {
            super.set(newValue);
            recreateHandles();
        }
    };
    @SuppressWarnings("this-escape")
    private final NonNullObjectProperty<HandleType> handleType = new NonNullObjectProperty<>(this, HANDLE_TYPE_PROPERTY, HandleType.RESIZE);
    @SuppressWarnings("this-escape")
    private final ObjectProperty<HandleType> leadHandleType = new SimpleObjectProperty<>(this, HANDLE_TYPE_PROPERTY, HandleType.RESIZE);

    @SuppressWarnings("this-escape")
    private final ObjectProperty<HandleType> anchorHandleType = new SimpleObjectProperty<>(this, HANDLE_TYPE_PROPERTY, HandleType.RESIZE);

    @SuppressWarnings("this-escape")
    private final NonNullObjectProperty<HandleType> multiHandleType = new NonNullObjectProperty<>(this, MULTI_HANDLE_TYPE_PROPERTY, HandleType.SELECT);
    @SuppressWarnings("this-escape")
    private final SetProperty<DrawingView> drawingViews = new SimpleSetProperty<>(this, DRAWING_VIEWS_PROPERTY, FXCollections.observableSet(new HashSet<>()));
    private final ChangeListener<Boolean> focusListener = (ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
        if (newValue) {
            setActiveDrawingView((DrawingView) ((ReadOnlyProperty<?>) observable).getBean());
        }
    };
    private final @Nullable Listener<ToolEvent> defaultToolActivator = (event) -> {
        switch (event.getEventType()) {
        case TOOL_DONE:
            if (getDefaultTool() != event.getSource() && getDefaultTool() != null) {
                setActiveTool(getDefaultTool());
            }
            break;
        default:
            break;
        }
    };
    private final ObjectProperty<DrawingView> activeDrawingView = new SimpleObjectProperty<>(this, ACTIVE_DRAWING_VIEW_PROPERTY);
    private final ObjectProperty<Tool> activeTool = new SimpleObjectProperty<>(this, ACTIVE_TOOL_PROPERTY);
    private final ObjectProperty<Tool> defaultTool = new SimpleObjectProperty<>(this, DEFAULT_TOOL_PROPERTY);

    {
        ChangeListener<Object> recreateHandles = (observable, oldValue, newValue) -> recreateHandles();
        multiHandleType.addListener(recreateHandles);
        handleType.addListener(recreateHandles);
    }

    {
        drawingViews.addListener((SetChangeListener.Change<? extends DrawingView> change) -> {
            if (change.wasRemoved()) {
                DrawingView removed = change.getElementRemoved();
                removed.setEditor(null);
                removed.focusedProperty().removeListener(focusListener);
                if (getActiveDrawingView() != null && getActiveDrawingView() == removed) {
                    setActiveDrawingView(drawingViews.isEmpty() ? null : drawingViews.get().iterator().next());
                }
                removed.setTool(null);
            } else if (change.wasAdded()) {
                DrawingView added = change.getElementAdded();
                added.focusedProperty().addListener(focusListener);
                if (added.getEditor() != null) {
                    added.getEditor().removeDrawingView(added);
                }
                added.setEditor(this);
                final Tool theActiveTool = getActiveTool();
                added.setTool(theActiveTool);
                if (drawingViews.size() == 1) {
                    setActiveDrawingView(added);
                }
            }

        });
    }

    {
        activeTool.addListener((o, oldValue, newValue) -> {

            if (getActiveDrawingView() != null) {
                getActiveDrawingView().setTool(newValue);
            }
            if (oldValue != null) {
                oldValue.deactivate(this);
                oldValue.removeToolListener(defaultToolActivator);
            }
            if (newValue != null) {
                newValue.addToolListener(defaultToolActivator);
                newValue.setDrawingEditor(this);
                newValue.activate(this);
            }
        });
    }

    public AbstractDrawingEditor() {
    }

    @Override
    public ObjectProperty<DrawingView> activeDrawingViewProperty() {
        return activeDrawingView;
    }

    @Override
    public ObjectProperty<Tool> activeToolProperty() {
        return activeTool;
    }

    @Override
    public ObjectProperty<HandleType> anchorHandleTypeProperty() {
        return anchorHandleType;
    }

    @Override
    public ObjectProperty<Tool> defaultToolProperty() {
        return defaultTool;
    }

    @Override
    public SetProperty<DrawingView> drawingViewsProperty() {
        return drawingViews;
    }

    @Override
    public NonNullObjectProperty<CssColor> handleColorProperty() {
        return handleColor;
    }

    @Override
    public DoubleProperty handleSizeProperty() {
        return handleSize;
    }

    @Override
    public DoubleProperty handleStrokeWidthProperty() {
        return handleStrokeWidth;
    }

    @Override
    public NonNullObjectProperty<HandleType> handleTypeProperty() {
        return handleType;
    }

    @Override
    public ObjectProperty<String> helpTextProperty() {
        return helpText;
    }

    @Override
    public ObjectProperty<HandleType> leadHandleTypeProperty() {
        return leadHandleType;
    }

    @Override
    public NonNullObjectProperty<HandleType> multiHandleTypeProperty() {
        return multiHandleType;
    }

    @Override
    public DoubleProperty toleranceProperty() {
        return tolerance;
    }

    @Override
    public NonNullObjectProperty<FXUndoManager> undoManagerProperty() {
        return undoManager;
    }
}
