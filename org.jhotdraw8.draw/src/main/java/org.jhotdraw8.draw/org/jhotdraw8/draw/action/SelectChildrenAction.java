/*
 * @(#)SelectChildrenAction.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.action;

import javafx.event.ActionEvent;
import org.jhotdraw8.application.action.Action;
import org.jhotdraw8.application.resources.Resources;
import org.jhotdraw8.draw.DrawLabels;
import org.jhotdraw8.draw.DrawingEditor;
import org.jhotdraw8.draw.DrawingView;
import org.jhotdraw8.draw.figure.Figure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * SelectChildrenAction.
 *
 */
public class SelectChildrenAction extends AbstractDrawingViewAction {

    public static final String ID = "edit.selectChildren";

    /**
     * Creates a new instance.
     *
     * @param editor the drawing editor
     */
    public SelectChildrenAction(DrawingEditor editor) {
        super(editor);
        Resources labels = DrawLabels.getResources();
        set(Action.ID_KEY, ID);
        labels.configureAction(this, ID);
    }

    @Override
    protected void onActionPerformed(ActionEvent e, DrawingView dview) {
        final List<Figure> figures = new ArrayList<>(dview.getSelectedFigures());
        selectChildren(dview, figures);

    }

    public static void selectChildren(DrawingView view, Collection<Figure> figures) {
        List<Figure> selectedChildren = new ArrayList<>();
        for (Figure f : figures) {
            selectedChildren.addAll(f.getChildren());
        }
        view.getSelectedFigures().clear();
        view.getSelectedFigures().addAll(selectedChildren);
    }
}
