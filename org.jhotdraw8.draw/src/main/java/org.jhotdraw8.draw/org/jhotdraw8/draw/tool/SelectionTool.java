/*
 * @(#)SelectionTool.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.tool;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Cursor;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ZoomEvent;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.application.ApplicationLabels;
import org.jhotdraw8.application.resources.Resources;
import org.jhotdraw8.draw.DrawingEditor;
import org.jhotdraw8.draw.DrawingView;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.draw.handle.Handle;
import org.jhotdraw8.draw.handle.HandleType;

import java.util.Map;

/**
 * A tool to select and manipulate figures.
 * <p>
 * A selection tool is in one of three states:
 * <ol>
 * <li>area selection</li>
 * <li>figure dragging</li>
 * <li>handle manipulation</li>
 * </ol>
 * The different states are handled by different tracker objects: the
 * <code>SimpleSelectAreaTracker</code>, the <code>SimpleDragTracker</code> and
 * the <code>SimpleHandleTracker</code>.
 * <p>
 * A Figure can be selected by clicking at it. Holding the alt key or the ctrl
 * key down, selects the Figure behind it.
 * <p>
 * Holding down the shift key on mouse pressed, enforces the area selection
 * function.
 *
 * @author Werner Randelshofer
 */
public class SelectionTool extends AbstractTool {

    public static final String ID = "tool.selectFigure";
    // ---
    // Property names
    // ---
    public static final String SELECT_BEHIND_ENABLED = "selectBehindEnabled";
    // ---
    // Fields
    // ---

    /**
     * The tracker encapsulates the current state of the SelectionTool.
     */
    private @Nullable Tracker tracker;
    /**
     * The tracker encapsulates the current state of the SelectionTool.
     */
    private HandleTracker handleTracker;
    /**
     * The tracker encapsulates the current state of the SelectionTool.
     */
    private SelectAreaTracker selectAreaTracker;
    /**
     * The tracker encapsulates the current state of the SelectionTool.
     */
    private DragTracker dragTracker;

    /**
     * Whether to update the cursor on mouse movements.
     */
    private final boolean updateCursor = true;

    private final BooleanProperty selectBehindEnabled = new SimpleBooleanProperty(this, SELECT_BEHIND_ENABLED, true);
    private boolean mouseDragged;
    private @Nullable Figure pressedFigure;
    private final HandleType handleType;
    private HandleType leadHandleType;
    private HandleType anchorHandleType;

    // ---
    // Constructors
    // ---
    public SelectionTool() {
        this("tool.selectFigure", HandleType.RESIZE, ApplicationLabels.getResources());
    }

    public SelectionTool(String name, Resources rsrc) {
        this(name, HandleType.RESIZE, rsrc);
    }

    public SelectionTool(String name, HandleType handleType, Resources rsrc) {
        super(name, rsrc);
        this.handleType = handleType;
    }

    public SelectionTool(String name, HandleType handleType, HandleType anchorHandleType, HandleType leadHandleType, Resources rsrc) {
        super(name, rsrc);
        this.handleType = handleType;
        this.anchorHandleType = anchorHandleType;
        this.leadHandleType = leadHandleType;
    }

    // ---
    // Properties
    // ---
    public @NonNull BooleanProperty selectBehindEnabledProperty() {
        return selectBehindEnabled;
    }

    // ---
    // Behaviors
    // ---
    @Override
    protected void stopEditing() {
        setTracker(null);
    }

    @Override
    protected void onMousePressed(@NonNull MouseEvent event, @NonNull DrawingView view) {
        requestFocus();
        mouseDragged = false;

        double vx = event.getX();
        double vy = event.getY();

        // HandleTracker may capture mouse event!
        Handle h = view.findHandle(vx, vy);
        if (h != null) {
            if (updateCursor) {
                node.setCursor(h.getCursor());
            }
            setTracker(getHandleTracker(h));
        } else {
            if (updateCursor) {
                node.setCursor(Cursor.DEFAULT);
            }
            tracker = null;
        }

        if (tracker == null) {
            // Mouse event not captured by handle tracker => Process mouse event on our own.
            if (event.isControlDown()) {
                SelectAreaTracker t = getSelectAreaTracker();
                setTracker(t);
            } else {

                // "alt" modifier finds a figure behind the current selection.
                if (isSelectBehindEnabled() && event.isAltDown()) {
                    // Select a figure behind the current selection
                    pressedFigure = null;
                    Figure firstFigure = null;
                    boolean selectionFound = false;
                    for (Map.Entry<Figure, Double> e : view.findFigures(vx, vy, false)) {
                        Figure f = e.getKey();
                        if (f.isShowing()) {
                            if (firstFigure == null) {
                                firstFigure = f;
                            }
                        }
                        if (view.selectedFiguresProperty().contains(f)) {
                            selectionFound = true;
                            continue;
                        }
                        if (selectionFound) {
                            pressedFigure = f;
                            break;
                        }
                    }
                    // take first figure
                    if (pressedFigure == null) {
                        pressedFigure = firstFigure;
                    }
                } else {
                    // find in selection
                    pressedFigure = view.findFigure(vx, vy, view.getSelectedFigures());
                    // find in entire drawing
                    if (pressedFigure == null) {
                        pressedFigure = view.findFigure(vx, vy);
                    }
                }

                if (event.isShiftDown() && !event.isMetaDown()) {
                    if (pressedFigure != null) {
                        if (view.getSelectedFigures().contains(pressedFigure)) {
                            // "shift"+mouse down on selected figure => remove from selection
                            view.getSelectedFigures().remove(pressedFigure);
                        } else {
                            // "shift"+mouse down on unselected figure => add to selection
                            view.getSelectedFigures().add(pressedFigure);
                        }
                        return;
                    }
                } else if (!event.isShiftDown() && event.isMetaDown()) {
                    return;
                    // no-op
                } else if (event.isShiftDown() && event.isMetaDown()) {
                    // "meta"+"shift"+mouse down on selected figure => reduce selection to a single figure
                    if (pressedFigure != null) {
                        view.selectedFiguresProperty().clear();
                        view.selectedFiguresProperty().add(pressedFigure);
                    }
                } else if (!event.isShiftDown() && !event.isMetaDown()) {
                    // mouse down without modifier keys sets the selection to the pressed figure, unless it is already selected
                    if (pressedFigure != null && !view.selectedFiguresProperty().contains(pressedFigure)) {
                        view.selectedFiguresProperty().clear();
                        view.selectedFiguresProperty().add(pressedFigure);
                    }
                }

                // "control" modifier enforces the select area tracker
                if (view.selectedFiguresProperty().contains(pressedFigure)) {
                    DragTracker t = getDragTracker(pressedFigure, view);
                    setTracker(t);
                } else {
                    SelectAreaTracker t = getSelectAreaTracker();
                    setTracker(t);
                }
            }
        }
        if (tracker != null) {
            tracker.trackMousePressed(event, view);
        }
        fireToolStarted();
    }

    @Override
    protected void onMouseDragged(@NonNull MouseEvent event, @NonNull DrawingView dv) {
        mouseDragged = true;
        if (tracker != null) {
            tracker.trackMouseDragged(event, dv);
        }
    }

    @Override
    protected void onMouseReleased(@NonNull MouseEvent event, @NonNull DrawingView dv) {
        if (tracker != null) {
            tracker.trackMouseReleased(event, dv);
        }
        //        setTracker(null);
    }

    @Override
    protected void onMouseClicked(@NonNull MouseEvent event, @NonNull DrawingView dv) {
        if (tracker != null) {
            tracker.trackMouseClicked(event, dv);
        }
        //        setTracker(null);
    }

    @Override
    protected void onMouseMoved(@NonNull MouseEvent event, @NonNull DrawingView view) {
        double vx = event.getX();
        double vy = event.getY();
        Handle h = view.findHandle(vx, vy);
        if (h != null) {
            if (updateCursor) {
                node.setCursor(h.getCursor());
            }
        } else {
            if (updateCursor) {
                node.setCursor(Cursor.DEFAULT);
            }
        }
    }

    @Override
    protected void onKeyPressed(@NonNull KeyEvent event, @NonNull DrawingView view) {
        if (tracker != null) {
            tracker.trackKeyPressed(event, view);
        }
    }

    @Override
    protected void onKeyReleased(@NonNull KeyEvent event, @NonNull DrawingView view) {
        if (tracker != null) {
            tracker.trackKeyReleased(event, view);
        }
    }

    @Override
    protected void onKeyTyped(@NonNull KeyEvent event, @NonNull DrawingView view) {
        if (tracker != null) {
            tracker.trackKeyTyped(event, view);
        }
    }

    /**
     * Method to get a {@code HandleTracker} which handles user interaction for
     * the specified handle.
     *
     * @param handle a handle
     * @return a handle tracker
     */
    protected HandleTracker getHandleTracker(Handle handle) {
        if (handleTracker == null) {
            handleTracker = new SimpleHandleTracker();
        }
        handleTracker.setHandles(handle, getDrawingView().getFiguresWithCompatibleHandle(
                getDrawingView().getSelectedFigures(), handle));
        return handleTracker;
    }

    /**
     * Method to get a {@code DragTracker} which handles user interaction for
     * dragging the specified figure.
     *
     * @param f  a figure
     * @param dv a drawing view
     * @return a tracker
     */
    protected DragTracker getDragTracker(Figure f, DrawingView dv) {
        if (dragTracker == null) {
            dragTracker = new SimpleDragTracker();
        }
        dragTracker.setDraggedFigure(f, dv);
        return dragTracker;
    }

    /**
     * Method to get a {@code SelectAreaTracker} which handles user interaction
     * for selecting an area on the drawing.
     *
     * @return a tracker
     */
    protected SelectAreaTracker getSelectAreaTracker() {
        if (selectAreaTracker == null) {
            selectAreaTracker = new SimpleSelectAreaTracker();
        }
        return selectAreaTracker;
    }

    /**
     * Method to set a {@code HandleTracker}. If you specify null, the
     * {@code SelectionTool} uses the {@code DefaultHandleTracker}.
     *
     * @param newValue a tracker
     */
    public void setHandleTracker(HandleTracker newValue) {
        handleTracker = newValue;
    }

    /**
     * Method to set a {@code SelectAreaTracker}. If you specify null, the
     * {@code SelectionTool} uses the {@code DefaultSelectAreaTracker}.
     *
     * @param newValue a tracker
     */
    public void setSelectAreaTracker(SelectAreaTracker newValue) {
        selectAreaTracker = newValue;
    }

    /**
     * Method to set a {@code DragTracker}. If you specify null, the
     * {@code SelectionTool} uses the {@code DefaultDragTracker}.
     *
     * @param newValue a tracker
     */
    public void setDragTracker(DragTracker newValue) {
        dragTracker = newValue;
    }

    private void setTracker(Tracker t) {
        if (tracker != null) {
            drawPane.setCenter(null);
            node.cursorProperty().unbindBidirectional(tracker.getNode().cursorProperty());
        }
        tracker = t;
        if (tracker != null) {
            drawPane.setCenter(tracker.getNode());
            //drawPane.layout();
            node.cursorProperty().bindBidirectional(tracker.getNode().cursorProperty());
            node.setCursor(Cursor.DEFAULT);
        }
    }

    @Override
    public void activate(@NonNull DrawingEditor editor) {
        for (DrawingView view : editor.getDrawingViews()) {
            view.getEditor().setHandleType(handleType);
            view.getEditor().setAnchorHandleType(anchorHandleType);
            view.getEditor().setLeadHandleType(leadHandleType);
        }
        requestFocus();
        super.activate(editor);
    }

    // ---
    // Convenience Methods
    // ---
    public boolean isSelectBehindEnabled() {
        return selectBehindEnabled.get();
    }

    double zoomFactor = 1.0;

    @Override
    protected void onZoom(@NonNull ZoomEvent event, @NonNull DrawingView dv) {
        dv.setZoomFactor(zoomFactor * event.getTotalZoomFactor());
    }

    @Override
    protected void onZoomStarted(@NonNull ZoomEvent event, @NonNull DrawingView dv) {
        zoomFactor = dv.getZoomFactor();
    }

    @Override
    public @NonNull String getHelpText() {
        return """
               SelectionTool
                 Click on the drawing view. The tool will select the figure at that location.
               Or:
                 Alt+Click on the drawing view. The tool will select the figure behind the currently selected figure at that location.
               Or:
                 Shift+Click on the drawing view. The tool will toggle the figure at that location to/from the selection.
               Or:
                 Shift+Command+Click on the drawing view. The tool will deselect all figures except the figure that is on that location.
               Or:
                 Press and drag the mouse over the drawing view to draw the diagonal of a rectangle. The tool will select all figures that fit into the rectangle.""";
    }

}
