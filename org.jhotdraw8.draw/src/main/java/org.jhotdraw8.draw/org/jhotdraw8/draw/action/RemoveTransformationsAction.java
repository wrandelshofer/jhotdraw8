/*
 * @(#)RemoveTransformationsAction.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.action;

import javafx.event.ActionEvent;
import org.jhotdraw8.application.resources.Resources;
import org.jhotdraw8.draw.DrawLabels;
import org.jhotdraw8.draw.DrawingEditor;
import org.jhotdraw8.draw.DrawingView;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.draw.figure.TransformableFigure;
import org.jhotdraw8.draw.model.DrawingModel;
import org.jhotdraw8.fxcollection.typesafekey.Key;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

/**
 * RemoveTransformationsAction.
 *
 */
public class RemoveTransformationsAction extends AbstractDrawingViewAction {

    public static final String ID = "edit.removeTransformations";

    /**
     * Creates a new instance.
     *
     * @param editor the drawing editor
     */
    public RemoveTransformationsAction(DrawingEditor editor) {
        super(editor);
        Resources labels
                = DrawLabels.getResources();
        labels.configureAction(this, ID);
    }

    @Override
    protected void onActionPerformed(ActionEvent e, DrawingView drawingView) {
        final LinkedList<Figure> figures = new LinkedList<>(drawingView.getSelectedFigures());
        removeTransformations(drawingView, figures);

    }

    public static void removeTransformations(DrawingView view, Collection<Figure> figures) {
        Set<Key<?>> keys = TransformableFigure.getDeclaredKeys();

        DrawingModel model = view.getModel();
        for (Figure child : figures) {
            if (child instanceof TransformableFigure) {
                for (Key<?> k : keys) {
                    model.remove(child, k);
                }
            }
        }
    }
}
