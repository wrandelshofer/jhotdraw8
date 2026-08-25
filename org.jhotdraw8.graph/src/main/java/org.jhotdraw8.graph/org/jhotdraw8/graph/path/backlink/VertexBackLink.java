/*
 * @(#)VertexBackLink.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.path.backlink;

import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.ToIntBiFunction;

/// Represents a vertex back link with depth.
///
/// @param <V> the vertex data type
public class VertexBackLink<V> extends AbstractBackLink<VertexBackLink<V>> {
    private final V vertex;

    /// Creates a new instance.
    ///
    /// @param vertex the vertex data
    /// @param parent the parent back link
    public VertexBackLink(V vertex, @Nullable VertexBackLink<V> parent) {
        super(parent);
        this.vertex = vertex;
    }


    public V getVertex() {
        return vertex;
    }

    /// Converts an [VertexBackLink] to [VertexBackLinkWithCost].
    ///
    /// @param <VV>         the vertex data type
    /// @param <CC>         the cost number type
    /// @param node         the [VertexBackLink]
    /// @param costFunction the cost function
    /// @return the converted [VertexBackLinkWithCost]
    public static <VV> @Nullable VertexBackLinkWithCost<VV> toVertexBackLinkWithCost(
            @Nullable VertexBackLink<VV> node,
            ToIntBiFunction<VV, VV> costFunction) {
        if (node == null) {
            return null;
        }


        Deque<VertexBackLink<VV>> deque = new ArrayDeque<>();
        for (VertexBackLink<VV> n = node; n != null; n = n.getParent()) {
            deque.addFirst(n);
        }


        VertexBackLinkWithCost<VV> newNode = null;
        for (VertexBackLink<VV> n : deque) {
            newNode = new VertexBackLinkWithCost<>(n.getVertex(), newNode,
                    newNode == null
                            ? 0
                            : (newNode.getCost() +
                            costFunction.applyAsInt(newNode.getVertex(), n.getVertex())));
        }
        return newNode;
    }
}
