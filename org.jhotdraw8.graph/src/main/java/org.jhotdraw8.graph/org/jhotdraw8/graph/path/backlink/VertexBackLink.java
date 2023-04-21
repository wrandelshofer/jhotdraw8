/*
 * @(#)VertexBackLink.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.path.backlink;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.BiFunction;

/**
 * Represents a vertex back link with depth.
 *
 * @param <V> the vertex data type
 */
public class VertexBackLink<V> extends AbstractBackLink<VertexBackLink<V>> {
    private final @NonNull V vertex;

    /**
     * Creates a new instance.
     *
     * @param vertex the vertex data
     * @param parent the parent back link
     */
    public VertexBackLink(@NonNull V vertex, @Nullable VertexBackLink<V> parent) {
        super(parent);
        this.vertex = vertex;
    }


    public @NonNull V getVertex() {
        return vertex;
    }

    /**
     * Converts an {@link VertexBackLink} to {@link VertexBackLinkWithCost}.
     *
     * @param node         the {@link VertexBackLink}
     * @param zero         the zero cost value
     * @param costFunction the cost function
     * @param sumFunction  the sum function for cost values
     * @param <VV>         the vertex data type
     * @param <CC>         the cost number type
     * @return the converted {@link VertexBackLinkWithCost}
     */
    public static <VV, CC extends Number & Comparable<CC>> @Nullable VertexBackLinkWithCost<VV, CC> toVertexBackLinkWithCost(
            @Nullable VertexBackLink<VV> node,
            @NonNull CC zero,
            @NonNull BiFunction<VV, VV, CC> costFunction,
            @NonNull BiFunction<CC, CC, CC> sumFunction) {
        if (node == null) {
            return null;
        }


        Deque<VertexBackLink<VV>> deque = new ArrayDeque<>();
        for (VertexBackLink<VV> n = node; n != null; n = n.getParent()) {
            deque.addFirst(n);
        }


        VertexBackLinkWithCost<VV, CC> newNode = null;
        for (VertexBackLink<VV> n : deque) {
            newNode = new VertexBackLinkWithCost<>(n.getVertex(), newNode,
                    newNode == null
                            ? zero
                            : sumFunction.apply(newNode.getCost(),
                            costFunction.apply(newNode.getVertex(), n.getVertex())));
        }
        return newNode;
    }
}
