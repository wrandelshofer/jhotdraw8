/*
 * @(#)VertexReachabilityAlgo.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.path.algo;

import org.jhotdraw8.graph.algo.AddToSet;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Interface for a reachability test algorithm.
 *
 * @param <V> the vertex data type
 * @param <C> the cost number type
 */
public interface VertexReachabilityAlgo<V, C extends Number & Comparable<C>> {

    /**
     * Search engine method.
     *
     * @param startVertices        the set of start vertices
     * @param goalPredicate        the goal predicate
     * @param maxDepth             the maximal depth (inclusive) of the search
     *                             Must be {@literal >= 0}.
     * @param zero                 the zero cost value
     * @param costLimit            the algorithm-specific cost limit
     * @param nextVerticesFunction the next nodes function
     * @param costFunction         the cost function
     * @param sumFunction          the sum function for adding two cost values
     * @param visited              the visited function
     * @return true on success
     */
    boolean tryToReach(
            Iterable<V> startVertices,
            Predicate<V> goalPredicate,
            int maxDepth,
            C zero,
            C costLimit,
            Function<V, Iterable<V>> nextVerticesFunction,
            BiFunction<V, V, C> costFunction,
            BiFunction<C, C, C> sumFunction, AddToSet<V> visited);
}
