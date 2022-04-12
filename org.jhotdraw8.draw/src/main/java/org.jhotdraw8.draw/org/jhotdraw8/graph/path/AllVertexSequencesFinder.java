/*
 * @(#)AllVertexSequencesFinder.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.path;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.ImmutableList;
import org.jhotdraw8.collection.OrderedPair;

import java.util.function.Predicate;

/**
 * Defines an API for finding all sequences between a set of source
 * vertices and goal vertices up to a maximal depth and a maximal cost
 * in a directed graph.
 *
 * @param <V> the vertex data type
 * @param <A> the arrow data type
 * @param <C> the cost number type
 */
public interface AllVertexSequencesFinder<V, A, C extends Number & Comparable<C>> {

    /**
     * Finds all vertex paths up to (including) the specified maximal cost.
     *
     * @param startVertices the set of start vertices
     * @param goalPredicate the goal predicate
     * @param maxDepth      the maximal depth (inclusive) of the search
     *                      Must be {@literal >= 0}.
     * @param costLimit     the algorithm-specific search limit
     * @return all paths
     */
    @NonNull Iterable<OrderedPair<ImmutableList<V>, C>> findAllVertexSequences(
            @NonNull Iterable<V> startVertices,
            @NonNull Predicate<V> goalPredicate,
            int maxDepth, @NonNull C costLimit);
}
