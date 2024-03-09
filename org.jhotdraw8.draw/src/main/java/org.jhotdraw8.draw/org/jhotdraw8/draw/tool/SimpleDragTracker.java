/*
 * @(#)SimpleDragTracker.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.tool;

import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.util.MathUtil;
import org.jhotdraw8.draw.DrawingView;
import org.jhotdraw8.draw.css.value.CssPoint2D;
import org.jhotdraw8.draw.figure.AnchorableFigure;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.draw.model.DrawingModel;
import org.jhotdraw8.fxbase.undo.CompositeEdit;
import org.jhotdraw8.graph.iterator.BfsDfsVertexSpliterator;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.StreamSupport;

import static org.jhotdraw8.draw.handle.MoveHandle.translateFigure;

/**
 * |@code SimpleDragTracker} implements interactions with the content area of a
 * {@code Figure}.
 * <p>
 * The {@code DefaultDragTracker} handles one of the three states of the
 * {@code SelectionTool}. It comes into action, when the user presses the mouse
 * button over the content area of a {@code Figure}.
 * <p>
 * Design pattern:<br>
 * Name: Chain of Responsibility.<br>
 * Role: Handler.<br>
 * Partners: {@link SelectionTool} as Handler, {@link SelectAreaTracker} as
 * Handler, {@link HandleTracker} as Handler.
 * <p>
 * Design pattern:<br>
 * Name: State.<br>
 * Role: State.<br>
 * Partners: {@link SelectAreaTracker} as State, {@link SelectionTool} as
 * Context, {@link HandleTracker} as State.
 *
 * @author Werner Randelshofer
 * @see SelectionTool
 */
public class SimpleDragTracker extends AbstractTracker implements DragTracker {


    private @Nullable Set<Figure> groupReshapeableFigures;
    private @Nullable Figure anchorFigure;
    private @Nullable CssPoint2D oldPoint;
    private @Nullable CssPoint2D anchor;

    private @Nullable CompositeEdit undoableEdit;

    public SimpleDragTracker() {
    }

    // ---
    // Behaviors
    // ---
    @Override
    public void setDraggedFigure(Figure anchor, @NonNull DrawingView view) {
        this.anchorFigure = anchor;

        // Determine which figures can be reshaped together as a group.
        Set<Figure> selectedFigures = view.getSelectedFigures();
        groupReshapeableFigures = new HashSet<>();
        for (Figure f : selectedFigures) {
            if (f.isGroupReshapeableWith(selectedFigures)) {
                    groupReshapeableFigures.add(f);
            }
        }
    }

    private boolean dependsOn(final @NonNull Figure f, final @NonNull Set<Figure> others) {
        return StreamSupport.stream(new BfsDfsVertexSpliterator<>(Figure::getLayoutSubjects, f, false), false)
                .anyMatch(fg -> (fg != f) && others.contains(fg) ||
                        (fg.getParent() != null && containsAny(others, fg.getParent().getPath())));
    }

    private <E> boolean containsAny(@NonNull Collection<E> subject, @NonNull Collection<E> c) {
        for (E e : c) {
            if (subject.contains(e)) {
                return true;
            }
        }
        return false;
    }


    @Override
    public void trackMousePressed(@NonNull MouseEvent event, @NonNull DrawingView view) {
        stopCompositeEdit(view);
        oldPoint = anchor = view.getConstrainer().constrainPoint(anchorFigure,
                new CssPoint2D(view.viewToWorld(new Point2D(event.getX(), event.getY()))));
    }

    @Override
    public void trackMouseReleased(MouseEvent event, @NonNull DrawingView dv) {
        dv.recreateHandles();
        stopCompositeEdit(dv);
    }

    @Override
    public void trackMouseClicked(MouseEvent event, DrawingView dv) {
        stopCompositeEdit(dv);
    }

    @Override
    public void trackMouseDragged(@NonNull MouseEvent event, @NonNull DrawingView view) {
        startCompositeEdit(view);
        CssPoint2D newPoint = new CssPoint2D(view.viewToWorld(new Point2D(event.getX(), event.getY())));

        if (!event.isAltDown() && !event.isControlDown()) {
            // alt or control turns the constrainer off
            newPoint = view.getConstrainer().constrainPoint(anchorFigure, newPoint);
        }

        if (event.isMetaDown()) {
            // meta snaps the top left corner of the anchor figure to the grid
            // or whatever corner is specified in the anchor
            Bounds bounds = anchorFigure.getLayoutBounds();

            double anchorX = MathUtil.clamp(anchorFigure.getNonNull(AnchorableFigure.ANCHOR_X), 0, 1);
            double anchorY = MathUtil.clamp(anchorFigure.getNonNull(AnchorableFigure.ANCHOR_Y), 0, 1);

            Point2D loc = new Point2D(bounds.getMinX() + anchorX * bounds.getWidth(),
                    bounds.getMinY() + anchorY * bounds.getHeight());
            oldPoint = new CssPoint2D(anchorFigure.localToWorld(loc));
        }

        if (newPoint.equals(oldPoint)) {
            return;
        }

        DrawingModel model = view.getModel();
        if (event.isShiftDown()) {
            // shift transforms only the anchor figure
            Figure f = anchorFigure;
            translateFigure(f, oldPoint, newPoint, model);
        } else {
            for (Figure f : groupReshapeableFigures) {
                translateFigure(f, oldPoint, newPoint, model);
            }
        }

        oldPoint = newPoint;
    }

    @Override
    public void trackKeyPressed(KeyEvent event, DrawingView view) {
        CssPoint2D translate = null;
        if (event.getEventType() == KeyEvent.KEY_PRESSED) {
            translate = switch (event.getCode()) {
                case UP -> new CssPoint2D(0, -1);
                case DOWN -> new CssPoint2D(0, 1);
                case LEFT -> new CssPoint2D(-1, 0);
                case RIGHT -> new CssPoint2D(1, 0);
                default -> null;
            };
        }
        if (translate != null) {
            event.consume();
            DrawingModel model = view.getModel();
            for (Figure f : view.getSelectedFigures()) {
                model.translateInParent(f, translate);
            }

        }

    }

    @Override
    public void trackKeyReleased(KeyEvent event, DrawingView view) {
    }

    @Override
    public void trackKeyTyped(KeyEvent event, DrawingView view) {
    }
}
