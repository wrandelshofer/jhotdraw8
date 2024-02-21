/*
 * @(#)SvgGFigure.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.svg.figure;

import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.transform.Transform;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.draw.css.value.CssTransforms;
import org.jhotdraw8.draw.figure.AbstractCompositeFigure;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.draw.figure.Grouping;
import org.jhotdraw8.draw.figure.HideableFigure;
import org.jhotdraw8.draw.figure.LockableFigure;
import org.jhotdraw8.draw.figure.ResizableFigure;
import org.jhotdraw8.draw.figure.StyleableFigure;
import org.jhotdraw8.draw.render.RenderContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an SVG 'g' element.
 *
 * @author Werner Randelshofer
 */
public class SvgGFigure extends AbstractCompositeFigure
        implements Grouping, ResizableFigure, SvgTransformableFigure, HideableFigure, StyleableFigure, LockableFigure,
        SvgDefaultableFigure,
        SvgElementFigure {

    /**
     * The CSS type selector for a label object is {@value #TYPE_SELECTOR}.
     */
    public static final String TYPE_SELECTOR = "g";

    @Override
    public @NonNull Node createNode(@NonNull RenderContext drawingView) {
        Group g = new Group();
        g.setAutoSizeChildren(false);
        g.setManaged(false);
        return g;
    }

    @Override
    public @NonNull String getTypeSelector() {
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
    public void reshapeInLocal(@NonNull CssSize x, @NonNull CssSize y, @NonNull CssSize width, @NonNull CssSize height) {
        flattenTransforms();
        Transform localTransform = CssTransforms.createReshapeTransform(getCssLayoutBounds(), x, y, width, height);
        for (Figure child : getChildren()) {
            child.reshapeInParent(localTransform);
        }
    }

    @Override
    public void updateNode(@NonNull RenderContext ctx, @NonNull Node n) {
        applyHideableFigureProperties(ctx, n);
        applyTransformableFigureProperties(ctx, n);
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
    public boolean isSuitableParent(@NonNull Figure newParent) {
        return true;
    }


    @Override
    public boolean isSuitableChild(@NonNull Figure newChild) {
        return true;
    }
}
