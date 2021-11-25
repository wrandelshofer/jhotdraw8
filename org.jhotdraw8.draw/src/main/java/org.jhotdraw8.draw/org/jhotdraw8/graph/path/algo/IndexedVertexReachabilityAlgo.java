package org.jhotdraw8.graph.path.algo;

import org.jhotdraw8.annotation.NonNull;

import java.util.Spliterator;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntPredicate;

/**
 * Defines an API for a reachability checker algorithm over a directed graph.
 *
 * @param <C> the cost number type
 */
public interface IndexedVertexReachabilityAlgo<C extends Number & Comparable<C>> {
    /**
     * Search engine method.
     *
     * @param startVertices        the set of start vertices
     * @param goalPredicate        the goal predicate
     * @param nextVerticesFunction the next vertices function
     * @param maxCost              the maximal cost (inclusive) that a sequence may have
     * @param zero                 the zero cost value
     * @param positiveInfinity     the positive infinity value
     * @param costFunction         the cost function
     * @param sumFunction          the sum function for adding two cost values
     * @return true on success
     */
    boolean tryToReach(@NonNull Iterable<Integer> startVertices,
                       @NonNull IntPredicate goalPredicate,
                       @NonNull Function<Integer, Spliterator.OfInt> nextVerticesFunction,
                       @NonNull C maxCost,
                       @NonNull C zero,
                       @NonNull C positiveInfinity,
                       @NonNull BiFunction<Integer, Integer, C> costFunction,
                       @NonNull BiFunction<C, C, C> sumFunction);
}
