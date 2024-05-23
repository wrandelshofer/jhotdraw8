/*
 * @(#)VertexBackLinkWithAncestorSet.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.path.backlink;

import org.jhotdraw8.icollection.ChampAddOnlySet;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.BiFunction;

/**
 * Represents a vertex back link with depth and a set of ancestors.
 *
 * @param <V> the vertex data type
 */
public class VertexBackLinkWithAncestorSet<V> extends AbstractBackLink<VertexBackLinkWithAncestorSet<V>> {
    private final V vertex;

    /**
     * This set contains the vertex of this back link and the vertices of all
     * parent backlinks.
     * <p>
     * This set is only needed for backlinks that are in the search frontier.
     * Once they leave the search frontier, the set is removed.
     */
    private @Nullable ChampAddOnlySet<V> ancestors;

    /**
     * Creates a new instance.
     *
     * @param vertex the vertex data
     * @param parent the parent back link
     */
    public VertexBackLinkWithAncestorSet(
            V vertex,
            @Nullable VertexBackLinkWithAncestorSet<V> parent,
            ChampAddOnlySet<V> ancestors) {
        super(parent);
        this.vertex = vertex;
        this.ancestors = ancestors;
    }

    /**
     * Converts an {@link VertexBackLinkWithAncestorSet} to {@link VertexBackLinkWithCost}.
     *
     * @param node         the {@link VertexBackLinkWithAncestorSet}
     * @param zero         the zero cost value
     * @param costFunction the cost function
     * @param sumFunction  the sum function for cost values
     * @param <VV>         the vertex data type
     * @param <CC>         the cost number type
     * @return the converted {@link VertexBackLinkWithCost}
     */
    public static <VV, CC extends Number & Comparable<CC>> @Nullable VertexBackLinkWithCost<VV, CC> toVertexBackLinkWithCost(
            @Nullable VertexBackLinkWithAncestorSet<VV> node,
            CC zero,
            BiFunction<VV, VV, CC> costFunction,
            BiFunction<CC, CC, CC> sumFunction) {
        if (node == null) {
            return null;
        }


        Deque<VertexBackLinkWithAncestorSet<VV>> deque = new ArrayDeque<>();
        for (VertexBackLinkWithAncestorSet<VV> n = node; n != null; n = n.getParent()) {
            deque.addFirst(n);
        }


        VertexBackLinkWithCost<VV, CC> newNode = null;
        for (VertexBackLinkWithAncestorSet<VV> n : deque) {
            newNode = new VertexBackLinkWithCost<>(n.getVertex(), newNode,
                    newNode == null
                            ? zero
                            : sumFunction.apply(newNode.getCost(),
                            costFunction.apply(newNode.getVertex(), n.getVertex())));
        }
        return newNode;
    }

    public ChampAddOnlySet<V> removeAncestors() {
        if (ancestors == null) {
            throw new IllegalStateException("ancestors already removed");
        }
        ChampAddOnlySet<V> ancestors = this.ancestors;
        this.ancestors = null;
        return ancestors;
    }

    public V getVertex() {
        return vertex;
    }
}
