package org.jhotdraw8.graph.path;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.graph.Arc;
import org.jhotdraw8.util.TriFunction;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Defines an API for path search algorithm.
 *
 * @param <V> the vertex data type
 * @param <A> the arrow data type
 * @param <C> the cost number type
 */
public interface PathSearchAlgo<V, A, C extends Number & Comparable<C>> {

    /**
     * Search engine method.
     *
     * @param startVertices     the set of start vertices
     * @param goalPredicate     the goal predicate
     * @param zero              the zero cost value
     * @param positiveInfinity  the positive infinity value
     * @param maxCost           the maximal cost (inclusive) that a sequence may have
     * @param nextNodesFunction the next nodes function
     * @param costFunction      the cost function
     * @param sumFunction       the sum function for adding two cost values
     * @return
     */
    @Nullable ArcBackLink<V, A, C> search(
            @NonNull Iterable<V> startVertices,
            @NonNull Predicate<V> goalPredicate,
            @NonNull C zero,
            @NonNull C positiveInfinity,
            @NonNull C maxCost,
            @NonNull Function<V, Iterable<Arc<V, A>>> nextNodesFunction,
            @NonNull TriFunction<V, V, A, C> costFunction,
            @NonNull BiFunction<C, C, C> sumFunction);
}
