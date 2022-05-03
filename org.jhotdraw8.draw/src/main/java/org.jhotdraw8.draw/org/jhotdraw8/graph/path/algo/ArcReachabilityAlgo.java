/*
 * @(#)ArcReachabilityAlgo.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.path.algo;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.graph.Arc;
import org.jhotdraw8.util.function.TriFunction;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Defines an API for a reachability checker algorithm over a directed graph.
 *
 * @param <V> the vertex data type
 * @param <A> the arrow data type
 * @param <C> the cost number type
 */
public interface ArcReachabilityAlgo<V, A, C extends Number & Comparable<C>> {

    /**
     * Search engine method.
     *
     * @param startVertices    the set of start vertices
     * @param goalPredicate    the goal predicate
     * @param nextArcsFunction the next arcs function
     * @param maxDepth         the maximal depth (inclusive) of the search
     *                         Must be {@literal >= 0}.
     * @param zero             the zero cost value
     * @param costLimit        the algorithm-specific cost limit.
     * @param costFunction     the cost function
     * @param sumFunction      the sum function for adding two cost values
     * @return true on success
     */
    boolean tryToReach(
            @NonNull Iterable<V> startVertices,
            @NonNull Predicate<V> goalPredicate,
            @NonNull Function<V, Iterable<Arc<V, A>>> nextArcsFunction,
            int maxDepth,
            @NonNull C zero,
            @NonNull C costLimit,
            @NonNull TriFunction<V, V, A, C> costFunction,
            @NonNull BiFunction<C, C, C> sumFunction);
}
