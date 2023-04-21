/*
 * @(#)SendToBackAction.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.action;

import javafx.event.ActionEvent;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.application.resources.Resources;
import org.jhotdraw8.draw.DrawLabels;
import org.jhotdraw8.draw.DrawingEditor;
import org.jhotdraw8.draw.DrawingView;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.draw.model.DrawingModel;

import java.util.Collection;
import java.util.LinkedList;

/**
 * SendToBackAction.
 *
 * @author Werner Randelshofer
 */
public class SendToBackAction extends AbstractDrawingViewAction {

    public static final String ID = "edit.sendToBack";

    /**
     * Creates a new instance.
     *
     * @param editor the drawing editor
     */
    public SendToBackAction(@NonNull DrawingEditor editor) {
        super(editor);
        Resources labels
                = DrawLabels.getResources();
        labels.configureAction(this, ID);
    }

    @Override
    protected void onActionPerformed(@NonNull ActionEvent e, @NonNull DrawingView dview) {
        final LinkedList<Figure> figures = new LinkedList<>(dview.getSelectedFigures());
        sendToBack(dview, figures);
    }

    public void sendToBack(@NonNull DrawingView view, @NonNull Collection<Figure> figures) {
        DrawingModel model = view.getModel();
        for (Figure child : figures) {
            Figure parent = child.getParent();
            if (parent != null && parent.isEditable() && parent.isDecomposable()) {
                assert parent.isAllowsChildren();
                model.insertChildAt(child, parent, 0);
            }
        }
    }
}
