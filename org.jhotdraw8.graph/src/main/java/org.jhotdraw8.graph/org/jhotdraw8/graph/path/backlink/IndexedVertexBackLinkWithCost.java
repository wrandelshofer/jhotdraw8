/*
 * @(#)IndexedVertexBackLinkWithCost.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.path.backlink;

import org.jhotdraw8.collection.pair.SimpleOrderedPair;
import org.jhotdraw8.icollection.PersistentVectorList;
import org.jhotdraw8.icollection.persistent.PersistentList;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Function;

/// Represents an indexed vertex back link with cost and depth.
///
/// @param <C> the cost number type
public class IndexedVertexBackLinkWithCost extends AbstractBackLinkWithCost<IndexedVertexBackLinkWithCost> {

    final int vertex;

    /// Creates a new instance.
    ///
    /// @param vertex the vertex index
    /// @param parent the parent back link
    /// @param cost   the cumulated cost of this back link. Must be zero if parent is null.
    public IndexedVertexBackLinkWithCost(int vertex, @Nullable IndexedVertexBackLinkWithCost parent, int cost) {
        super(parent, cost);
        this.vertex = vertex;
    }


    public int getVertex() {
        return vertex;
    }

    /// Converts an [ArcBackLinkWithCost] into a vertex sequence.
    ///
    /// @param node            the [ArcBackLinkWithCost]
    /// @param mappingFunction the mapping function
    /// @param <XX>            the vertex sequence element type
    /// @return the vertex sequence
    public static <XX> @Nullable SimpleOrderedPair<PersistentList<XX>, Integer> toVertexSequence(@Nullable IndexedVertexBackLinkWithCost node,
                                                                                                 Function<IndexedVertexBackLinkWithCost, XX> mappingFunction) {
        if (node == null) {
            return null;
        }

        Deque<XX> deque = new ArrayDeque<>();
        for (IndexedVertexBackLinkWithCost parent = node; parent != null; parent = parent.getParent()) {
            deque.addFirst(mappingFunction.apply(parent));
        }
        return new SimpleOrderedPair<PersistentList<XX>, Integer>(PersistentVectorList.copyOf(deque), node.getCost());
    }

}
