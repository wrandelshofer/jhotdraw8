package org.jhotdraw8.graph.path.algo;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.graph.Arc;
import org.jhotdraw8.util.TriFunction;

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
     * @param zero             the zero cost value
     * @param positiveInfinity the positive infinity value
     * @param searchLimit      the algorithm-specific search limit.
     *                         Set this value as small as you can, to prevent
     *                         long search times if the goal can not be reached.
     * @param costFunction     the cost function
     * @param sumFunction      the sum function for adding two cost values
     * @return true on success
     */
    boolean tryToReach(
            @NonNull Iterable<V> startVertices,
            @NonNull Predicate<V> goalPredicate,
            @NonNull Function<V, Iterable<Arc<V, A>>> nextArcsFunction,
            @NonNull C zero,
            @NonNull C positiveInfinity,
            @NonNull C searchLimit,
            @NonNull TriFunction<V, V, A, C> costFunction,
            @NonNull BiFunction<C, C, C> sumFunction);
}
