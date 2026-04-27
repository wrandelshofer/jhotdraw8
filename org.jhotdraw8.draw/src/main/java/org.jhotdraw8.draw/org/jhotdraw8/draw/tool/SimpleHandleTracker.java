/*
 * @(#)SimpleHandleTracker.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.tool;

import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import org.jhotdraw8.draw.DrawingView;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.draw.handle.Handle;

import java.util.Collection;

/// `SimpleHandleTracker` implements interactions with the handles of a
/// Figure.
///
/// The `SimpleHandleTracker` handles one of the three states of the
/// `SelectionTool`. Iz comes into action, when the user presses the mouse
/// button over a `Figure`.
///
/// Design pattern:
/// Name: Chain of Responsibility.
/// Role: Handler.
/// Partners: [SelectionTool] as Handler, [SelectAreaTracker] as
/// Handler, [DragTracker] as Handler, [HandleTracker] as Handler.
///
/// Design pattern:
/// Name: State.
/// Role: State.
/// Partners: [SelectAreaTracker] as State, [DragTracker] as State,
/// [SelectionTool] as Context.
///
/// @see SelectionTool
public class SimpleHandleTracker extends AbstractTracker implements HandleTracker {

    private Handle handle;
    private Collection<Figure> compatibleFigures;

    public SimpleHandleTracker() {
    }

    @Override
    public void setHandles(Handle handle, Collection<Figure> compatibleFigures) {
        this.handle = handle;
        this.compatibleFigures = compatibleFigures;
    }


    @Override
    public void trackMousePressed(MouseEvent event, DrawingView dv) {
        stopCompositeEdit(dv);
        handle.onMousePressed(event, dv);
        node.setCursor(handle.getCursor());
        node.setAccessibleHelp(handle.getNode(dv).getAccessibleHelp());
        node.setAccessibleText(handle.getNode(dv).getAccessibleText());
    }

    @Override
    public void trackMouseClicked(MouseEvent event, DrawingView dv) {
        stopCompositeEdit(dv);
        handle.onMouseClicked(event, dv);
    }

    @Override
    public void trackMouseReleased(MouseEvent event, DrawingView dv) {
        handle.onMouseReleased(event, dv);
        node.setCursor(handle.getCursor());
        stopCompositeEdit(dv);
    }

    @Override
    public void trackMouseDragged(MouseEvent event, DrawingView dv) {
        startCompositeEdit(dv);
        handle.onMouseDragged(event, dv);
        node.setCursor(handle.getCursor());
    }

    @Override
    public void trackKeyPressed(KeyEvent event, DrawingView view) {
    }

    @Override
    public void trackKeyReleased(KeyEvent event, DrawingView view) {
    }

    @Override
    public void trackKeyTyped(KeyEvent event, DrawingView view) {
    }
}
