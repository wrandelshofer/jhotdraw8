/*
 * @(#)BringForwardAction.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.action;

import javafx.event.ActionEvent;
import org.jhotdraw8.application.resources.Resources;
import org.jhotdraw8.draw.DrawLabels;
import org.jhotdraw8.draw.DrawingEditor;
import org.jhotdraw8.draw.DrawingView;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.draw.model.DrawingModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * BringForwardAction.
 *
 */
public class BringForwardAction extends AbstractDrawingViewAction {

    public static final String ID = "edit.bringForward";

    /**
     * Creates a new instance.
     *
     * @param editor the drawing editor
     */
    public BringForwardAction(DrawingEditor editor) {
        super(editor);
        Resources labels
                = DrawLabels.getResources();
        labels.configureAction(this, ID);
    }

    @Override
    protected void onActionPerformed(ActionEvent e, DrawingView drawingView) {
        final List<Figure> figures = new ArrayList<>(drawingView.getSelectedFigures());
        moveUp(drawingView, figures);

    }

    public void moveUp(DrawingView view, Collection<Figure> figures) {
        DrawingModel model = view.getModel();
        for (Figure child : figures) {
            Figure parent = child.getParent();
            if (parent != null && parent.isEditable() && parent.isDecomposable()) {
                int size = parent.getChildren().size();
                int index = parent.getChildren().indexOf(child);
                if (index < size - 1) {
                    model.insertChildAt(child, parent, index + 1);
                }
            }
        }
    }
}
