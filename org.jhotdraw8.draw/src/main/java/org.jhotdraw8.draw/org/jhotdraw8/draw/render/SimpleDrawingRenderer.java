/*
 * @(#)SimpleDrawingRenderer.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.render;

import javafx.scene.Group;
import javafx.scene.Node;
import org.jspecify.annotations.Nullable;
import org.jhotdraw8.draw.figure.Drawing;
import org.jhotdraw8.draw.figure.Figure;
import org.jhotdraw8.fxbase.beans.AbstractPropertyBean;
import org.jhotdraw8.fxcollection.typesafekey.Key;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * SimpleDrawingRenderer.
 *
 * @author Werner Randelshofer
 */
public class SimpleDrawingRenderer extends AbstractPropertyBean implements RenderContext {

    // ---
    // Field declarations
    // ---
    protected final Map<Figure, Node> figureToNodeMap = new HashMap<>();

    public SimpleDrawingRenderer() {
    }


    // ---
    // Behavior
    // ---
    @Override
    public Node getNode(Figure f) {
        Node n = figureToNodeMap.get(f);
        if (n == null) {
            n = f.createNode(this);
            figureToNodeMap.put(f, n);
        }
        return n;
    }

    /**
     * Renders the provided figure into a JavaFX Node.
     *
     * @param figure The figure
     * @return the rendered node
     */
    public Node render(Figure figure) {
        figureToNodeMap.clear();
        renderRecursive(figure);
        return getNode(figure);
    }

    /**
     * Recursive part of the render method.
     *
     * @param figure The figure
     */
    private void renderRecursive(Figure figure) {
        figure.updateNode(this, getNode(figure));
        for (Figure child : figure.getChildren()) {
            renderRecursive(child);
        }
    }

    public static Node toNode(Drawing external, Collection<Figure> selection, @Nullable Map<Key<?>, Object> renderingHints) {
        SimpleDrawingRenderer r = new SimpleDrawingRenderer();
        if (renderingHints != null) {
            r.getProperties().putAll(renderingHints);
        }
        LinkedList<Node> nodes = new LinkedList<>();
        for (Figure f : external.preorderIterable()) {
            if (selection.contains(f)) {
                nodes.add(r.render(f));
            }
        }
        Node drawingNode;
        if (nodes.size() == 1) {
            drawingNode = nodes.getFirst();
        } else {
            drawingNode = new Group(nodes);
        }
        return drawingNode;
    }

}
