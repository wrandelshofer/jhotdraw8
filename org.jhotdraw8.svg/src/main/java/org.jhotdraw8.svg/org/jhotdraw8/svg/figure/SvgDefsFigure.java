/*
 * @(#)SvgDefsFigure.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.svg.figure;

import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.draw.figure.AbstractCompositeFigure;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.draw.figure.Grouping;
import org.jhotdraw8.draw.figure.HideableFigure;
import org.jhotdraw8.draw.figure.LockableFigure;
import org.jhotdraw8.draw.figure.NonTransformableFigure;
import org.jhotdraw8.draw.figure.ResizableFigure;
import org.jhotdraw8.draw.figure.StyleableFigure;
import org.jhotdraw8.draw.render.RenderContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an SVG 'defs' element.
 *
 * @author Werner Randelshofer
 */
public class SvgDefsFigure extends AbstractCompositeFigure
        implements Grouping, ResizableFigure, NonTransformableFigure, HideableFigure, StyleableFigure, LockableFigure,
        SvgDefaultableFigure,
        SvgElementFigure {

    /**
     * The CSS type selector for a label object is {@value #TYPE_SELECTOR}.
     */
    public static final String TYPE_SELECTOR = "defs";

    public SvgDefsFigure() {
        set(VISIBLE, false);
    }

    @Override
    public Node createNode(RenderContext drawingView) {
        Group g = new Group();
        g.setAutoSizeChildren(false);
        g.setManaged(false);
        return g;
    }

    @Override
    public String getTypeSelector() {
        return TYPE_SELECTOR;
    }


    @Override
    public void updateNode(RenderContext ctx, Node n) {
        applyHideableFigureProperties(ctx, n);
        applyStyleableFigureProperties(ctx, n);
        applySvgDefaultableCompositingProperties(ctx, n);

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

    @Override
    public void reshapeInLocal(CssSize x, CssSize y, CssSize width, CssSize height) {
        // does nothing
    }
}
