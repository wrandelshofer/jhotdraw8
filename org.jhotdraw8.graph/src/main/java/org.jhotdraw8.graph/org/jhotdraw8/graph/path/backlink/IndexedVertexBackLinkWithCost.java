/*
 * @(#)IndexedVertexBackLinkWithCost.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.path.backlink;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.VectorList;
import org.jhotdraw8.collection.immutable.ImmutableList;
import org.jhotdraw8.collection.pair.SimpleOrderedPair;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Function;

/**
 * Represents an indexed vertex back link with cost and depth.
 *
 * @param <C> the cost number type
 */
public class IndexedVertexBackLinkWithCost<C extends Number & Comparable<C>> extends AbstractBackLinkWithCost<IndexedVertexBackLinkWithCost<C>, C> {

    final int vertex;

    /**
     * Creates a new instance.
     *
     * @param vertex the vertex index
     * @param parent the parent back link
     * @param cost   the cumulated cost of this back link. Must be zero if parent is null.
     */
    public IndexedVertexBackLinkWithCost(int vertex, @Nullable IndexedVertexBackLinkWithCost<C> parent, @NonNull C cost) {
        super(parent, cost);
        this.vertex = vertex;
    }


    public int getVertex() {
        return vertex;
    }

    /**
     * Converts an {@link ArcBackLinkWithCost} into a vertex sequence.
     *
     * @param node            the {@link ArcBackLinkWithCost}
     * @param mappingFunction the mapping function
     * @param <CC>            the cost number type
     * @param <XX>            the vertex sequence element type
     * @return the vertex sequence
     */
    public static <XX, CC extends Number & Comparable<CC>> @Nullable SimpleOrderedPair<ImmutableList<XX>, CC> toVertexSequence(@Nullable IndexedVertexBackLinkWithCost<CC> node,
                                                                                                                               @NonNull Function<IndexedVertexBackLinkWithCost<CC>, XX> mappingFunction) {
        if (node == null) {
            return null;
        }

        Deque<XX> deque = new ArrayDeque<>();
        for (IndexedVertexBackLinkWithCost<CC> parent = node; parent != null; parent = parent.getParent()) {
            deque.addFirst(mappingFunction.apply(parent));
        }
        return new SimpleOrderedPair<>(VectorList.copyOf(deque), node.getCost());
    }

}
