/*
 * @(#)SimpleDrawing.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.figure;

import javafx.scene.Node;
import javafx.scene.transform.Transform;
import org.jhotdraw8.draw.render.RenderContext;

/**
 * A simple implementation of {@link Drawing}.
 */
public class SimpleDrawing extends AbstractViewBoxDrawing
        implements StyleableFigure, LockableFigure {
    public SimpleDrawing(double width, double height) {
        super(width, height);
    }

    public SimpleDrawing() {
    }

    @Override
    public boolean isSuitableChild(Figure newChild) {
        return true;
    }

    @Override
    public void reshapeInParent(Transform transform) {
        // cannot be reshaped
    }

    @Override
    public void transformInLocal(Transform transform) {
        // cannot be transformed
    }

    @Override
    public void transformInParent(Transform transform) {
        // cannot be transformed
    }

    @Override
    public void updateNode(RenderContext ctx, Node n) {
        super.updateNode(ctx, n);
        applyStyleableFigureProperties(ctx, n);
    }
}
