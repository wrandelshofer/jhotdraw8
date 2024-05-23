/*
 * @(#)VertexPathSearchAlgo.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.path.algo;

import org.jhotdraw8.graph.algo.AddToSet;
import org.jhotdraw8.graph.path.backlink.VertexBackLinkWithCost;
import org.jspecify.annotations.Nullable;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Interface for a vertex path search algorithm.
 *
 * @param <V> the vertex data type
 * @param <C> the cost number type
 */
public interface VertexPathSearchAlgo<V, C extends Number & Comparable<C>> {

    /**
     * Search engine method.
     *
     * @param startVertices        the set of start vertices
     * @param goalPredicate        the goal predicate
     * @param nextVerticesFunction the next vertices function
     * @param maxDepth             the maximal depth (inclusive) of the search
     *                             Must be {@literal >= 0}.
     * @param zero                 the zero cost value
     * @param costLimit            the algorithm-specific cost limit
     * @param sumFunction          the sum function for adding two cost values
     * @param visited              the visited function
     * @return on success: a back link, otherwise: null
     */
    @Nullable VertexBackLinkWithCost<V, C> search(
            Iterable<V> startVertices,
            Predicate<V> goalPredicate,
            Function<V, Iterable<V>> nextVerticesFunction,
            int maxDepth,
            C zero,
            C costLimit,
            BiFunction<V, V, C> costFunction,
            BiFunction<C, C, C> sumFunction, AddToSet<V> visited);
}
