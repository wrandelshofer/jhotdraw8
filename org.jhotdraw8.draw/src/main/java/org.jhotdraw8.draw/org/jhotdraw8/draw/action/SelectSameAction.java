/*
 * @(#)SelectSameAction.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.action;

import javafx.collections.ObservableSet;
import javafx.event.ActionEvent;
import org.jhotdraw8.application.action.Action;
import org.jhotdraw8.application.resources.Resources;
import org.jhotdraw8.draw.DrawLabels;
import org.jhotdraw8.draw.DrawingEditor;
import org.jhotdraw8.draw.DrawingView;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.icollection.readable.ReadableSet;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * SelectSameAction.
 *
 */
public class SelectSameAction extends AbstractDrawingViewAction {

    public static final String ID = "edit.selectSame";

    /**
     * Creates a new instance.
     *
     * @param editor the drawing editor
     */
    public SelectSameAction(DrawingEditor editor) {
        super(editor);
        Resources labels = DrawLabels.getResources();
        set(Action.ID_KEY, ID);
        labels.configureAction(this, ID);
    }

    @Override
    protected void onActionPerformed(ActionEvent e, DrawingView dview) {
        ObservableSet<Figure> selection = dview.getSelectedFigures();
        selectSame(dview, selection.isEmpty() ? null : selection.iterator().next());

    }

    public static void selectSame(DrawingView view, @Nullable Figure prototype) {
        if (prototype == null) {
            return;
        }

        String stype = prototype.getTypeSelector();
        ReadableSet<String> sclass = prototype.getStyleClasses();

        List<Figure> selectedSame = new ArrayList<>();
        for (Figure f : view.getDrawing().preorderIterable()) {
            if (f.isSelectable() && f.isShowing()) {
                if (Objects.equals(f.getTypeSelector(), stype)
                        && Objects.equals(f.getStyleClasses(), sclass) && f != prototype) {
                    selectedSame.add(f);
                }
            }
        }
        view.getSelectedFigures().clear();
        view.getSelectedFigures().add(prototype);
        view.getSelectedFigures().addAll(selectedSame);
    }
}
