/*
 * @(#)AlignLeftAction.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.action;

import javafx.event.ActionEvent;
import javafx.geometry.Point2D;
import javafx.scene.transform.Translate;
import org.jhotdraw8.application.resources.Resources;
import org.jhotdraw8.draw.DrawLabels;
import org.jhotdraw8.draw.DrawingEditor;
import org.jhotdraw8.draw.DrawingView;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.draw.model.DrawingModel;
import org.jhotdraw8.geom.FXTransforms;
import org.jspecify.annotations.Nullable;

import java.util.Set;

public class AlignLeftAction extends AbstractDrawingViewAction {

    public static final String ID = "edit.alignLeft";

    /**
     * Creates a new instance.
     *
     * @param editor the drawing editor
     */
    public AlignLeftAction(DrawingEditor editor) {
        super(editor);
        Resources labels
                = DrawLabels.getResources();
        labels.configureAction(this, ID);
    }

    @Override
    protected void onActionPerformed(ActionEvent e, DrawingView drawingView) {
        final Set<Figure> figures = drawingView.getSelectedFigures();
        Figure lead = drawingView.getSelectionLead();
        alignLeft(drawingView, figures, lead);
    }

    private void alignLeft(DrawingView view, Set<Figure> figures, @Nullable Figure lead) {
        if (figures.size() < 2 || lead == null) {
            return;
        }
        DrawingModel model = view.getModel();
        double xInWorld = lead.getLayoutBoundsInWorld().getMinX();
        Point2D xPointInWorld = new Point2D(xInWorld, 0);
        for (Figure f : figures) {
            if (f != lead && f.isEditable()) {
                double desiredX = FXTransforms.transform(f.getWorldToParent(), xPointInWorld).getX();
                double actualX = f.getLayoutBoundsInParent().getMinX();
                double dx = desiredX - actualX;
                Translate tx = new Translate(dx, 0);
                model.transformInParent(f, tx);
                model.fireLayoutInvalidated(f);
            }
        }
    }
}
