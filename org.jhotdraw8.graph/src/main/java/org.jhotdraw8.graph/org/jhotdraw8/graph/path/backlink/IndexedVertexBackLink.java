/*
 * @(#)IndexedVertexBackLink.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.path.backlink;

import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.ToIntBiFunction;

/// Represents an indexed vertex back link with depth.
public class IndexedVertexBackLink extends AbstractBackLink<IndexedVertexBackLink> {

    final int vertex;

    /// Creates a new instance.
    ///
    /// @param vertex the vertex index
    /// @param parent the parent back link
    public IndexedVertexBackLink(int vertex, @Nullable IndexedVertexBackLink parent) {
        super(parent);
        this.vertex = vertex;
    }


    public int getVertex() {
        return vertex;
    }

    /// Converts an [IndexedVertexBackLink] to [IndexedVertexBackLinkWithCost].
    ///
    /// @param node         the [IndexedVertexBackLink]
    /// @param costFunction the cost function
    /// @return the converted [IndexedVertexBackLinkWithCost]
    public static @Nullable IndexedVertexBackLinkWithCost
    toIndexedVertexBackLinkWithCost(@Nullable IndexedVertexBackLink node,
                                    ToIntBiFunction<Integer, Integer> costFunction) {
        if (node == null) {
            return null;
        }


        Deque<IndexedVertexBackLink> deque = new ArrayDeque<>();
        for (IndexedVertexBackLink n = node; n != null; n = n.getParent()) {
            deque.addFirst(n);
        }


        IndexedVertexBackLinkWithCost newNode = null;
        for (IndexedVertexBackLink n : deque) {
            newNode = new IndexedVertexBackLinkWithCost(n.getVertex(), newNode,
                    newNode == null
                            ? 0
                            : (newNode.getCost() +
                            costFunction.applyAsInt(newNode.getVertex(), n.getVertex())));
        }
        return newNode;
    }

}
