/*
 * @(#)DistributeVerticallyAction.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.action;

import javafx.event.ActionEvent;
import javafx.geometry.Bounds;
import javafx.scene.transform.Translate;
import org.jhotdraw8.application.resources.Resources;
import org.jhotdraw8.draw.DrawLabels;
import org.jhotdraw8.draw.DrawingEditor;
import org.jhotdraw8.draw.DrawingView;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.draw.model.DrawingModel;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DistributeVerticallyAction extends AbstractDrawingViewAction {

    public static final String ID = "edit.distributeVertically";

    /**
     * Creates a new instance.
     *
     * @param editor the drawing editor
     */
    @SuppressWarnings("this-escape")
    public DistributeVerticallyAction(DrawingEditor editor) {
        super(editor);
        Resources labels
                = DrawLabels.getResources();
        labels.configureAction(this, ID);
    }

    @Override
    protected void onActionPerformed(ActionEvent e, DrawingView drawingView) {
        final Set<Figure> figures = drawingView.getSelectedFigures();
        distributeVertically(drawingView, figures);
    }

    /**
     * Distributes the given figure vertically by their centers on the y-axis.
     *
     * @param view    the drawing view
     * @param figures the figures to be distributed horizontally
     */
    private void distributeVertically(DrawingView view, Set<Figure> figures) {
        if (figures.size() < 3) {
            return;
        }
        DrawingModel model = view.getModel();

        // Find min and and max center
        double maxX = Double.NEGATIVE_INFINITY;
        double minX = Double.POSITIVE_INFINITY;
        List<Map.Entry<Double, Figure>> list = new ArrayList<>();
        Outer:
        for (Figure f : figures) {
            for (Figure subject : f.getLayoutSubjects()) {
                if (figures.contains(subject)) {
                    // Filter out figures that base their layout on figures in the set.
                    continue Outer;
                }
            }
            Bounds b = f.getLayoutBoundsInWorld();
            double cy = b.getMinY() + b.getHeight() * 0.5;
            list.add(new AbstractMap.SimpleEntry<>(cy, f));
            maxX = Math.max(maxX, cy);
            minX = Math.min(minX, cy);
        }

        // Sort figures by their centers pn the x-axis
        // (Without sorting, we would distribute the list by the sequence
        // they were selected).
        list.sort(Comparator.comparingDouble(Map.Entry::getKey));

        // Distribute the figures by their centers
        double extent = maxX - minX;
        double count = figures.size();
        double index = 0;
        for (Map.Entry<Double, Figure> e : list) {
            Figure f = e.getValue();
            Bounds b = f.getLayoutBoundsInWorld();
            double oldcy = b.getMinY() + b.getHeight() * 0.5;
            double newcy = minX + extent * index / (count - 1);
            double dy = newcy - oldcy;
            if (dy != 0) {
                Translate tx = new Translate(0, dy);
                model.transformInParent(f, tx);
                model.fireLayoutInvalidated(f);
            }

            index++;
        }
    }
}
