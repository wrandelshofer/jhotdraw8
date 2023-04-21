/*
 * @(#)IndexedVertexBackLink.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.path.backlink;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.BiFunction;

/**
 * Represents an indexed vertex back link with depth.
 */
public class IndexedVertexBackLink extends AbstractBackLink<IndexedVertexBackLink> {

    final int vertex;

    /**
     * Creates a new instance.
     *
     * @param vertex the vertex index
     * @param parent the parent back link
     */
    public IndexedVertexBackLink(int vertex, @Nullable IndexedVertexBackLink parent) {
        super(parent);
        this.vertex = vertex;
    }


    public int getVertex() {
        return vertex;
    }

    /**
     * Converts an {@link IndexedVertexBackLink} to {@link IndexedVertexBackLinkWithCost}.
     *
     * @param node         the {@link IndexedVertexBackLink}
     * @param zero         the zero cost value
     * @param costFunction the cost function
     * @param sumFunction  the sum function for cost values
     * @param <CC>         the cost number type
     * @return the converted {@link IndexedVertexBackLinkWithCost}
     */
    public static <CC extends Number & Comparable<CC>> @Nullable IndexedVertexBackLinkWithCost<CC>
    toIndexedVertexBackLinkWithCost(@Nullable IndexedVertexBackLink node,
                                    @NonNull CC zero,
                                    @NonNull BiFunction<Integer, Integer, CC> costFunction,
                                    @NonNull BiFunction<CC, CC, CC> sumFunction) {
        if (node == null) {
            return null;
        }


        Deque<IndexedVertexBackLink> deque = new ArrayDeque<>();
        for (IndexedVertexBackLink n = node; n != null; n = n.getParent()) {
            deque.addFirst(n);
        }


        IndexedVertexBackLinkWithCost<CC> newNode = null;
        for (IndexedVertexBackLink n : deque) {
            newNode = new IndexedVertexBackLinkWithCost<>(n.getVertex(), newNode,
                    newNode == null
                            ? zero
                            : sumFunction.apply(newNode.getCost(),
                            costFunction.apply(newNode.getVertex(), n.getVertex())));
        }
        return newNode;
    }

}
