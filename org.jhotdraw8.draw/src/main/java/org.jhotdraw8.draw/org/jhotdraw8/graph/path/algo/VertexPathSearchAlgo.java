package org.jhotdraw8.graph.path.algo;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.graph.path.backlink.VertexBackLink;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Defines an API for a vertex path search algorithm.
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
     * @param zero                 the zero cost value
     * @param positiveInfinity     the positive infinity value
     * @param searchLimit          the algorithm-specific search limit
     *                             Set this value as small as you can, to prevent
     *                             long search times if the goal can not be reached.
     * @param sumFunction          the sum function for adding two cost values
     * @return on success: a back link, otherwise: null
     */
    @Nullable VertexBackLink<V, C> search(
            @NonNull Iterable<V> startVertices,
            @NonNull Predicate<V> goalPredicate,
            @NonNull Function<V, Iterable<V>> nextVerticesFunction,
            @NonNull C zero,
            @NonNull C positiveInfinity,
            @NonNull C searchLimit,
            @NonNull BiFunction<V, V, C> costFunction,
            @NonNull BiFunction<C, C, C> sumFunction);
}
