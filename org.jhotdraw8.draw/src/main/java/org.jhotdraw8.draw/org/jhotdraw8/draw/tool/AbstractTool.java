/*
 * @(#)AbstractTool.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.tool;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ZoomEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.application.AbstractDisableable;
import org.jhotdraw8.application.EditableComponent;
import org.jhotdraw8.application.resources.Resources;
import org.jhotdraw8.base.event.Listener;
import org.jhotdraw8.draw.DrawingEditor;
import org.jhotdraw8.draw.DrawingView;
import org.jhotdraw8.fxbase.undo.UndoableEditHelper;
import org.jhotdraw8.fxcollection.typesafekey.Key;

import javax.swing.event.UndoableEditEvent;
import java.util.LinkedList;

/**
 * AbstractAction.
 *
 * @author Werner Randelshofer
 */
public abstract class AbstractTool extends AbstractDisableable implements Tool {

    /**
     * The getProperties.
     */
    private ObservableMap<Key<?>, Object> properties;
    /**
     * The active view.
     */
    private final ObjectProperty<DrawingView> drawingView = new SimpleObjectProperty<>(this, DRAWING_VIEW_PROPERTY);
    /**
     * The active editor.
     */
    private final ObjectProperty<DrawingEditor> drawingEditor = new SimpleObjectProperty<>(this, DRAWING_EDITOR_PROPERTY);

    {
        drawingView.addListener((ObservableValue<? extends DrawingView> observable, DrawingView oldValue, DrawingView newValue) -> {
            stopEditing();
        });
    }


    private class EventPane extends BorderPane implements EditableComponent {

        public EventPane() {
            setId("toolEventPane");
        }

        private @Nullable EditableComponent getEditableParent() {

            DrawingView dv = getDrawingView();
            if (dv != null) {
                if (dv.getNode() instanceof EditableComponent) {
                    return (EditableComponent) dv.getNode();
                }
            }
            return null;
        }

        @Override
        public void selectAll() {
            EditableComponent p = getEditableParent();
            if (p != null) {
                p.selectAll();
            }
        }

        @Override
        public void clearSelection() {
            EditableComponent p = getEditableParent();
            if (p != null) {
                p.clearSelection();
            }
        }

        @Override
        public @Nullable ReadOnlyBooleanProperty selectionEmptyProperty() {
            EditableComponent p = getEditableParent();
            if (p != null) {
                return p.selectionEmptyProperty();
            }
            return null;
        }

        @Override
        public void deleteSelection() {
            EditableComponent p = getEditableParent();
            if (p != null) {
                p.deleteSelection();
            }
        }

        @Override
        public void duplicateSelection() {
            EditableComponent p = getEditableParent();
            if (p != null) {
                p.duplicateSelection();
            }
        }

        @Override
        public void cut() {
            EditableComponent p = getEditableParent();
            if (p != null) {
                p.cut();
            }
        }

        @Override
        public void copy() {
            EditableComponent p = getEditableParent();
            if (p != null) {
                p.copy();
            }
        }

        @Override
        public void paste() {
            EditableComponent p = getEditableParent();
            if (p != null) {
                p.paste();
            }
        }

    }

    protected final BorderPane eventPane = new EventPane();
    protected final BorderPane drawPane = new BorderPane();
    protected final StackPane node = new StackPane();

    {
        eventPane.addEventHandler(MouseEvent.ANY, (MouseEvent event) -> {
            try {
                DrawingView dv = drawingView.get();
                if (dv != null) {
                    EventType<? extends MouseEvent> type = event.getEventType();
                    if (type == MouseEvent.MOUSE_MOVED) {
                        onMouseMoved(event, dv);
                    } else if (type == MouseEvent.MOUSE_DRAGGED) {
                        onMouseDragged(event, dv);
                    } else if (type == MouseEvent.MOUSE_EXITED) {
                        onMouseExited(event, dv);
                    } else if (type == MouseEvent.MOUSE_ENTERED) {
                        onMouseEntered(event, dv);
                    } else if (type == MouseEvent.MOUSE_RELEASED) {
                        onMouseReleased(event, dv);
                    } else if (type == MouseEvent.MOUSE_PRESSED) {
                        onMousePressed(event, dv);
                    } else if (type == MouseEvent.MOUSE_CLICKED) {
                        onMouseClicked(event, dv);
                    }
                    event.consume();
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        });
        eventPane.addEventHandler(KeyEvent.ANY, (KeyEvent event) -> {
            DrawingView dv = drawingView.get();
            if (dv != null) {
                EventType<? extends KeyEvent> type = event.getEventType();
                if (type == KeyEvent.KEY_PRESSED) {
                    onKeyPressed(event, dv);
                } else if (type == KeyEvent.KEY_RELEASED) {
                    onKeyReleased(event, dv);
                } else if (type == KeyEvent.KEY_TYPED) {
                    onKeyTyped(event, dv);
                }
                event.consume();
            }
        });
        eventPane.addEventHandler(ZoomEvent.ANY, (ZoomEvent event) -> {
            DrawingView dv = drawingView.get();
            if (dv != null) {
                EventType<? extends ZoomEvent> type = event.getEventType();
                if (type == ZoomEvent.ZOOM) {
                    onZoom(event, dv);
                } else if (type == ZoomEvent.ZOOM_STARTED) {
                    onZoomStarted(event, dv);
                } else if (type == ZoomEvent.ZOOM_FINISHED) {
                    onZoomFinished(event, dv);
                }
                event.consume();

            }

        });

    }

    /**
     * Listeners.
     */
    private final LinkedList<Listener<ToolEvent>> toolListeners = new LinkedList<>();

    // ---
    // Constructors
    // ---


    /**
     * Creates a new instance.
     *
     * @param name the id of the tool
     * @param rsrc iff nonnull, the resource is applied to the tool
     */
    public AbstractTool(@NonNull String name, @Nullable Resources rsrc) {
        set(NAME, name);
        if (rsrc != null) {
            applyResources(rsrc);
        }

        node.getChildren().addAll(drawPane, eventPane);
    }

    // ---
    // Properties
    // ---
    @Override
    public final @NonNull ObservableMap<Key<?>, Object> getProperties() {
        if (properties == null) {
            properties = FXCollections.observableHashMap();
        }
        return properties;
    }

    @Override
    public @NonNull ObjectProperty<DrawingView> drawingViewProperty() {
        return drawingView;
    }

    @Override
    public @NonNull ObjectProperty<DrawingEditor> drawingEditorProperty() {
        return drawingEditor;
    }

    // ---
    // Behaviors
    // ---
    protected void applyResources(@NonNull Resources rsrc) {
        String name = get(NAME);
        set(LABEL, rsrc.getTextProperty(name));
        set(LARGE_ICON_KEY, rsrc.getLargeIconProperty(name, getClass()));
        set(SHORT_DESCRIPTION, rsrc.getToolTipTextProperty(name));
    }

    @Override
    public @NonNull Node getNode() {
        return node;
    }

    protected void stopEditing() {
    }

    /**
     * Deletes the selection. Depending on the tool, this could be selected
     * figures, selected points or selected text.
     */
    @Override
    public void editDelete() {
        if (getDrawingView() != null) {
            DrawingView v = getDrawingView();
            v.getDrawing().getChildren().removeAll(v.getSelectedFigures());
        }
    }

    /**
     * Cuts the selection into the clipboard. Depending on the tool, this could
     * be selected figures, selected points or selected text.
     */
    @Override
    public void editCut() {
    }

    /**
     * Copies the selection into the clipboard. Depending on the tool, this
     * could be selected figures, selected points or selected text.
     */
    @Override
    public void editCopy() {
    }

    /**
     * Duplicates the selection. Depending on the tool, this could be selected
     * figures, selected points or selected text.
     */
    @Override
    public void editDuplicate() {
    }

    /**
     * Pastes the contents of the clipboard. Depending on the tool, this could
     * be selected figures, selected points or selected text.
     */
    @Override
    public void editPaste() {
    }

    // ---
    // Event handlers
    // ----
    protected void onMouseMoved(@NonNull MouseEvent event, @NonNull DrawingView view) {
    }

    protected void onMouseDragged(@NonNull MouseEvent event, @NonNull DrawingView view) {
    }

    protected void onMouseExited(@NonNull MouseEvent event, @NonNull DrawingView view) {
    }

    protected void onMouseEntered(@NonNull MouseEvent event, @NonNull DrawingView view) {
    }

    protected void onMouseReleased(@NonNull MouseEvent event, @NonNull DrawingView view) {
    }

    protected void onMousePressed(@NonNull MouseEvent event, @NonNull DrawingView view) {
    }

    protected void onMouseClicked(@NonNull MouseEvent event, @NonNull DrawingView view) {
    }

    protected void onKeyPressed(@NonNull KeyEvent event, @NonNull DrawingView view) {
        if (event.getCode() == KeyCode.ESCAPE) {
            fireToolDone();
        } else if (event.getCode() == KeyCode.ENTER) {
            stopEditing();
        }
    }

    protected void onKeyReleased(@NonNull KeyEvent event, @NonNull DrawingView view) {
    }

    protected void onKeyTyped(@NonNull KeyEvent event, @NonNull DrawingView view) {
    }

    /**
     * This implementation sets the help text on the drawing view.
     */
    @Override
    public void activate(@NonNull DrawingEditor editor) {
        editor.setHelpText(getHelpText());
    }

    /**
     * This implementation is empty.
     */
    @Override
    public void deactivate(@NonNull DrawingEditor editor) {
    }

    // ---
    // Listeners
    // ---
    @Override
    public void addToolListener(@NonNull Listener<ToolEvent> listener) {
        toolListeners.add(listener);
    }

    @Override
    public void removeToolListener(@NonNull Listener<ToolEvent> listener) {
        toolListeners.remove(listener);
    }

    protected void fire(@NonNull ToolEvent event) {
        for (Listener<ToolEvent> l : toolListeners) {
            l.handle(event);
        }
    }

    protected void onZoom(@NonNull ZoomEvent event, @NonNull DrawingView dv) {
    }

    protected void onZoomStarted(@NonNull ZoomEvent event, @NonNull DrawingView dv) {
    }

    protected void onZoomFinished(@NonNull ZoomEvent event, @NonNull DrawingView dv) {
    }

    protected void fireToolStarted() {
        fire(new ToolEvent(this, ToolEvent.EventType.TOOL_STARTED));
    }

    protected void fireToolDone() {
        fire(new ToolEvent(this, ToolEvent.EventType.TOOL_DONE));
    }

    protected void requestFocus() {
        Platform.runLater(eventPane::requestFocus);
    }

    @Override
    public @NonNull ReadOnlyBooleanProperty focusedProperty() {
        return eventPane.focusedProperty();
    }

    protected final @NonNull UndoableEditHelper undoHelper = new UndoableEditHelper(this, this::forwardUndoableEdit);

    protected void forwardUndoableEdit(@NonNull UndoableEditEvent event) {
        DrawingEditor editor = getDrawingEditor();
        if (editor != null) {
            editor.getUndoManager().undoableEditHappened(event);
        }
    }
}
