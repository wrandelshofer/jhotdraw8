/*
 * @(#)GroupFigure.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.figure;

import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.transform.Transform;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.css.value.CssTransforms;
import org.jhotdraw8.draw.render.RenderContext;

import java.util.ArrayList;
import java.util.List;

/**
 * A figure which groups child figures, so that they can be edited by the user
 * as a unit.
 */
public class GroupFigure extends AbstractCompositeFigure
        implements Grouping, ResizableFigure, TransformableFigure, HideableFigure, StyleableFigure, LockableFigure, CompositableFigure {

    /**
     * The CSS type selector for a label object is {@value #TYPE_SELECTOR}.
     */
    public static final String TYPE_SELECTOR = "Group";

    public GroupFigure() {
    }

    @Override
    public Node createNode(RenderContext drawingView) {
        Group n = new Group();
        n.setAutoSizeChildren(false);
        n.setManaged(false);
        return n;
    }

    @Override
    public String getTypeSelector() {
        return TYPE_SELECTOR;
    }

    @Override
    public void reshapeInLocal(Transform transform) {
        flattenTransforms();
        for (Figure child : getChildren()) {
            child.reshapeInParent(transform);
        }
    }

    @Override
    public void reshapeInLocal(CssSize x, CssSize y, CssSize width, CssSize height) {
        flattenTransforms();
        Transform localTransform = CssTransforms.createReshapeTransform(getCssLayoutBounds(), x, y, width, height);
        for (Figure child : getChildren()) {
            child.reshapeInParent(localTransform);
        }
    }

    @Override
    public void updateNode(RenderContext ctx, Node n) {
        applyHideableFigureProperties(ctx, n);
        applyTransformableFigureProperties(ctx, n);
        applyStyleableFigureProperties(ctx, n);
        applyCompositableFigureProperties(ctx, n);

        List<Node> nodes = new ArrayList<>(getChildren().size());
        for (Figure child : getChildren()) {
            nodes.add(ctx.getNode(child));
        }
        ObservableList<Node> group = ((Group) n).getChildren();
        if (!group.equals(nodes)) {
            group.setAll(nodes);
        }
    }

    @Override
    public boolean isSuitableParent(Figure newParent) {
        return true;
    }


    @Override
    public boolean isSuitableChild(Figure newChild) {
        return true;
    }
}
