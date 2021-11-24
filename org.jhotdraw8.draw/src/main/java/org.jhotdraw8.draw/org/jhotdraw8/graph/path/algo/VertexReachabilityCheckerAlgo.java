package org.jhotdraw8.graph.path.algo;

import org.jhotdraw8.annotation.NonNull;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Defines an API for a reachability test algorithm.
 *
 * @param <V> the vertex data type
 * @param <C> the cost number type
 */
public interface VertexReachabilityCheckerAlgo<V, C extends Number & Comparable<C>> {

    /**
     * Search engine method.
     *
     * @param startVertices        the set of start vertices
     * @param goalPredicate        the goal predicate
     * @param zero                 the zero cost value
     * @param positiveInfinity     the positive infinity value
     * @param maxCost              the maximal cost (inclusive) that a sequence may have
     * @param nextVerticesFunction the next nodes function
     * @param costFunction         the cost function
     * @param sumFunction          the sum function for adding two cost values
     * @return true on success
     */
    boolean tryToReach(
            @NonNull Iterable<V> startVertices,
            @NonNull Predicate<V> goalPredicate,
            @NonNull C zero,
            @NonNull C positiveInfinity,
            @NonNull C maxCost,
            @NonNull Function<V, Iterable<V>> nextVerticesFunction,
            @NonNull BiFunction<V, V, C> costFunction,
            @NonNull BiFunction<C, C, C> sumFunction);
}
