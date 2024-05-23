/*
 * @(#)SimpleLayeredDrawing.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.figure;

import javafx.scene.Node;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.draw.render.RenderContext;

public class SimpleLayeredDrawing extends AbstractViewBoxDrawing
        implements LayeredDrawing, StyleableFigure, LockableFigure, NonTransformableFigure {
    public SimpleLayeredDrawing() {
    }

    public SimpleLayeredDrawing(double width, double height) {
        super(width, height);
    }

    public SimpleLayeredDrawing(CssSize width, CssSize height) {
        super(width, height);
    }

    @Override
    public void updateNode(RenderContext ctx, Node n) {
        super.updateNode(ctx, n);
        applyStyleableFigureProperties(ctx, n);
    }
}
