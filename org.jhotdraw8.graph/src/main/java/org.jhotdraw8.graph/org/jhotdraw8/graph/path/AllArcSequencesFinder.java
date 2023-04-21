/*
 * @(#)AllArcSequencesFinder.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.path;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.OrderedPair;
import org.jhotdraw8.collection.immutable.ImmutableList;
import org.jhotdraw8.graph.Arc;

import java.util.function.Predicate;

/**
 * Interface for finding all sequences between a set of source
 * vertices and goal vertices up to a maximal depth in a directed graph.
 *
 * @param <V> the vertex data type
 * @param <A> the arrow data type
 * @param <C> the cost number type
 */
public interface AllArcSequencesFinder<V, A, C extends Number & Comparable<C>> {
    /**
     * Finds all arc paths up to (including) the specified maximal cost.
     *
     * @param startVertices the set of start vertices
     * @param goalPredicate the goal predicate
     * @param maxDepth      the maximal depth (inclusive) of the search
     *                      Must be {@literal >= 0}.
     * @param costLimit     the algorithm-specific cost limit
     * @return all paths
     */
    @NonNull Iterable<OrderedPair<ImmutableList<Arc<V, A>>, C>> findAllArcSequences(
            @NonNull Iterable<V> startVertices,
            @NonNull Predicate<V> goalPredicate,
            int maxDepth, @NonNull C costLimit);

}
