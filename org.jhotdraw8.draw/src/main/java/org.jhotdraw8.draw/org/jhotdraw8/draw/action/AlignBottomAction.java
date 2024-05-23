/*
 * @(#)AlignBottomAction.java
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

public class AlignBottomAction extends AbstractDrawingViewAction {

    public static final String ID = "edit.alignBottom";

    /**
     * Creates a new instance.
     *
     * @param editor the drawing editor
     */
    @SuppressWarnings("this-escape")
    public AlignBottomAction(DrawingEditor editor) {
        super(editor);
        Resources labels
                = DrawLabels.getResources();
        labels.configureAction(this, ID);
    }

    @Override
    protected void onActionPerformed(ActionEvent e, DrawingView drawingView) {
        final Set<Figure> figures = drawingView.getSelectedFigures();
        Figure lead = drawingView.getSelectionLead();
        alignBottom(drawingView, figures, lead);
    }

    private void alignBottom(DrawingView view, Set<Figure> figures, @Nullable Figure lead) {
        if (figures.size() < 2 || lead == null) {
            return;
        }
        DrawingModel model = view.getModel();
        double yInWorld = lead.getLayoutBoundsInWorld().getMaxY();
        Point2D yPointInWorld = new Point2D(0, yInWorld);
        for (Figure f : figures) {
            if (f != lead && f.isEditable()) {
                double desiredY = FXTransforms.transform(f.getWorldToParent(), yPointInWorld).getY();
                double actualY = f.getLayoutBoundsInParent().getMaxY();
                double dy = desiredY - actualY;
                Translate tx = new Translate(0, dy);
                model.transformInParent(f, tx);
                model.fireLayoutInvalidated(f);
            }
        }
    }
}
