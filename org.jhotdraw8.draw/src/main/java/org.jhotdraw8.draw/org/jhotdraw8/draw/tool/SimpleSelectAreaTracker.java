/*
 * @(#)SimpleSelectAreaTracker.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.tool;

import javafx.geometry.Bounds;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.jhotdraw8.application.ApplicationLabels;
import org.jhotdraw8.application.resources.Resources;
import org.jhotdraw8.draw.DrawingView;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.draw.figure.Layer;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.lang.Math.abs;
import static java.lang.Math.min;
import static java.lang.Math.round;

/**
 * {@code SimpleSelectAreaTracker} implements interactions with the background
 * area of a {@code Drawing}.
 * <p>
 * The {@code DefaultSelectAreaTracker} handles one of the three states of the
 * {@code SelectionTool}. It comes into action, when the user presses the mouse
 * button over the background of a {@code Drawing}.
 * <p>
 * This tool draws a {@code Rectangle} with style class "tool-rubberband".
 * <p>
 * Design pattern:<br>
 * Name: Chain of Responsibility.<br>
 * Role: Handler.<br>
 * Partners: {@link SelectionTool} as Handler, {@link DragTracker} as Handler,
 * {@link HandleTracker} as Handler.
 * <p>
 * Design pattern:<br>
 * Name: State.<br>
 * Role: State.<br>
 * Partners: {@link SelectionTool} as Context, {@link DragTracker} as State,
 * {@link HandleTracker} as State.
 *
 * @author Werner Randelshofer
 * @see SelectionTool
 */
public class SimpleSelectAreaTracker extends AbstractTracker implements SelectAreaTracker {

    /**
     * This tool draws a JavaFX {@code Rectangle} with style class
     * "tool-rubberband".
     */
    public static final String STYLECLASS_TOOL_RUBBERBAND = "tool-rubberband";


    /**
     * The rubberband.
     */
    private final Rectangle rubberband = new Rectangle();

    double x;
    double y;

    public SimpleSelectAreaTracker() {
        this("tool.selectArea", ApplicationLabels.getResources());
    }

    public SimpleSelectAreaTracker(String name, Resources rsrc) {
        //super(name, rsrc);

        // Add the rubberband to the node with absolute positioning
        node.getChildren().add(rubberband);
        rubberband.setVisible(false);
        initNode(rubberband);
    }

    protected void initNode(Rectangle r) {
        r.setFill(null);
        r.setStroke(Color.BLACK);
        rubberband.getStyleClass().add(STYLECLASS_TOOL_RUBBERBAND);
    }

    @Override
    public void trackMousePressed(MouseEvent event, DrawingView dv) {
        Bounds b = getNode().getBoundsInParent();
        x = event.getX();
        y = event.getY();
        rubberband.setVisible(true);
        rubberband.setX(round(x) - 0.5);
        rubberband.setY(round(y) - 0.5);
        rubberband.setWidth(0);
        rubberband.setHeight(0);
    }

    @Override
    public void trackMouseReleased(MouseEvent event, DrawingView dv) {
        rubberband.setVisible(false);

        double w = x - event.getX();
        double h = y - event.getY();
        final List<Map.Entry<Figure, Double>> result =
                event.isAltDown()
                        ? dv.findFiguresIntersecting(min(x, event.getX()), min(y, event.getY()), abs(w), abs(h), false
                        , f -> !(f instanceof Layer) && f.isSelectable())
                        : dv.findFiguresInside(min(x, event.getX()), min(y, event.getY()), abs(w), abs(h), false);
        List<Figure> f = result
                .stream().map(Map.Entry::getKey).collect(Collectors.toList());
        if (event.isShiftDown()) {
            if (dv.getSelectedFigures().containsAll(f)) {
                if (event.isMetaDown()) {
                    dv.getSelectedFigures().retainAll(f);
                } else {
                    f.forEach(dv.getSelectedFigures()::remove);
                }
            } else {
                dv.selectedFiguresProperty().addAll(f);
            }
        } else {
            dv.selectedFiguresProperty().clear();
            dv.selectedFiguresProperty().addAll(f);
        }
    }

    @Override
    public void trackMouseDragged(MouseEvent event, DrawingView dv) {
        double w = x - event.getX();
        double h = y - event.getY();
        rubberband.setX(round(min(x, event.getX())) - 0.5);
        rubberband.setY(round(min(y, event.getY())) - 0.5);
        rubberband.setWidth(round(abs(w)));
        rubberband.setHeight(round(abs(h)));
    }

    @Override
    public void trackMouseClicked(MouseEvent event, DrawingView view) {
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
