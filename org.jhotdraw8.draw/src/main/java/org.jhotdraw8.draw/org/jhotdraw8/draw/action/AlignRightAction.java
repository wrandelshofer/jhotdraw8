/*
 * @(#)AlignRightAction.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.action;

import javafx.event.ActionEvent;
import javafx.geometry.Point2D;
import javafx.scene.transform.Translate;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.application.resources.Resources;
import org.jhotdraw8.draw.DrawLabels;
import org.jhotdraw8.draw.DrawingEditor;
import org.jhotdraw8.draw.DrawingView;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.draw.model.DrawingModel;
import org.jhotdraw8.geom.FXTransforms;

import java.util.Set;

public class AlignRightAction extends AbstractDrawingViewAction {

    public static final String ID = "edit.alignRight";

    /**
     * Creates a new instance.
     *
     * @param editor the drawing editor
     */
    public AlignRightAction(@NonNull DrawingEditor editor) {
        super(editor);
        Resources labels
                = DrawLabels.getResources();
        labels.configureAction(this, ID);
    }

    @Override
    protected void onActionPerformed(@NonNull ActionEvent e, @NonNull DrawingView drawingView) {
        final Set<Figure> figures = drawingView.getSelectedFigures();
        if (figures.size() < 2) {
            return;
        }
        Figure lead = drawingView.getSelectionLead();
        alignRight(drawingView, figures, lead);
    }

    private void alignRight(@NonNull DrawingView view, @NonNull Set<Figure> figures, @Nullable Figure lead) {
        if (figures.size() < 2 || lead == null) {
            return;
        }
        DrawingModel model = view.getModel();
        double xInWorld = lead.getLayoutBoundsInWorld().getMaxX();
        Point2D xPointInWorld = new Point2D(xInWorld, 0);
        for (Figure f : figures) {
            if (f != lead && f.isEditable()) {
                double desiredX = FXTransforms.transform(f.getWorldToParent(), xPointInWorld).getX();
                double actualX = f.getLayoutBoundsInParent().getMaxX();
                double dx = desiredX - actualX;
                Translate tx = new Translate(dx, 0);
                model.transformInParent(f, tx);
                model.fireLayoutInvalidated(f);
            }
        }
    }
}
