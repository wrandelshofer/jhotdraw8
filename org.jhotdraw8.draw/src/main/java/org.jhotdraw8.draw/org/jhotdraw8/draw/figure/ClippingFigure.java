/*
 * @(#)ClippingFigure.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.figure;

import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.transform.Transform;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.draw.handle.Handle;
import org.jhotdraw8.draw.handle.HandleType;
import org.jhotdraw8.draw.render.RenderContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * ClippingFigure.
 *
 * @author Werner Randelshofer
 */
public class ClippingFigure extends AbstractCompositeFigure
        implements Clipping, StyleableFigure, LockedFigure, NonTransformableFigure {

    public ClippingFigure() {
    }

    @SuppressWarnings("this-escape")
    public ClippingFigure(Collection<Figure> children) {
        getChildren().addAll(children);
    }

    @Override
    public void reshapeInLocal(Transform transform) {
        for (Figure child : getChildren()) {
            child.reshapeInLocal(transform);
        }
    }

    @Override
    public void reshapeInLocal(CssSize x, CssSize y, CssSize width, CssSize height) {
        // empty
    }

    @Override
    public void updateNode(RenderContext ctx, Node n) {
        applyStyleableFigureProperties(ctx, n);

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
    public Node createNode(RenderContext ctx) {
        Group n = new Group();
        n.setManaged(false);
        n.setAutoSizeChildren(false);
        return n;
    }

    /**
     * Layer figures always return false for isSelectable.
     *
     * @return false
     */
    @Override
    public boolean isSelectable() {
        return false;
    }

    /**
     * This method returns false for all new parents.
     *
     * @param newParent The new parent figure.
     * @return false
     */
    @Override
    public boolean isSuitableParent(Figure newParent) {
        return false;
    }

    /**
     * This method returns true for all children.
     *
     * @param newChild The new child figure.
     * @return true
     */
    @Override
    public boolean isSuitableChild(Figure newChild) {
        return true;
    }

    /**
     * Layers never create handles.
     */
    @Override
    public void createHandles(HandleType handleType, List<Handle> list) {
        // empty
    }

    @Override
    public Bounds getBoundsInLocal() {
        return getLayoutBounds();
    }
}
