/*
 * @(#)IndexedVertexPathSearchAlgo.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.path.algo;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.graph.algo.AddToIntSet;
import org.jhotdraw8.graph.path.backlink.IndexedVertexBackLinkWithCost;

import java.util.Spliterator;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntPredicate;

/**
 * Interface for a vertex path search algorithm over an indexed directed
 * graph.
 *
 * @param <C> the cost number type
 */
public interface IndexedVertexPathSearchAlgo<C extends Number & Comparable<C>> {

    /**
     * Search engine method.
     *
     * @param startVertices        the set of start vertices
     * @param goalPredicate        the goal predicate
     * @param nextVerticesFunction the next vertices function
     * @param maxDepth             the maximal depth (inclusive) of the search
     *                             Must be {@literal >= 0}.
     * @param zero                 the zero cost value
     * @param costLimit            the algorithm-specific cost limit.
     * @param costFunction         the cost function
     * @param sumFunction          the sum function for adding two cost values
     * @param visited              the visited function
     * @return on success: a back link, otherwise: null
     */
    @Nullable IndexedVertexBackLinkWithCost<C> search(
            @NonNull Iterable<Integer> startVertices,
            @NonNull IntPredicate goalPredicate,
            @NonNull Function<Integer, Spliterator.OfInt> nextVerticesFunction,
            int maxDepth,
            @NonNull C zero,
            @NonNull C costLimit,
            @NonNull BiFunction<Integer, Integer, C> costFunction,
            @NonNull BiFunction<C, C, C> sumFunction, @NonNull AddToIntSet visited);
}
