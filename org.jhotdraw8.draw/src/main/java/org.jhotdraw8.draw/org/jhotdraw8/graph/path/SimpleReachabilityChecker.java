package org.jhotdraw8.graph.path;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.graph.Arc;
import org.jhotdraw8.graph.path.algo.ReachabilityAlgo;
import org.jhotdraw8.util.TriFunction;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Implements the {@link VertexSequenceFinder} interface.
 *
 * @param <V> the vertex data type
 * @param <C> the cost number type
 */
public class SimpleReachabilityChecker<V, A, C extends Number & Comparable<C>> extends AbstractReachabilityChecker<V, A, C> {
    private final @NonNull ReachabilityAlgo<V, A, C> algo;


    public SimpleReachabilityChecker(
            @NonNull C zero,
            @NonNull C positiveInfinity,
            @NonNull Function<V, Iterable<Arc<V, A>>> nextArcsFunction,
            @NonNull TriFunction<V, V, A, C> costFunction,
            @NonNull BiFunction<C, C, C> sumFunction,
            @NonNull ReachabilityAlgo<V, A, C> algo) {
        super(zero,
                positiveInfinity,
                nextArcsFunction, costFunction, sumFunction);
        this.algo = algo;
    }


    /**
     * Creates a new instance which has a cost function that returns integer
     * numbers.
     *
     * @param nextArcsFunction a function that given a vertex,
     *                         returns an {@link Iterable} for the arcs
     *                         of that vertex.
     * @param costFunction     the cost function
     * @param algo             the search algorithm
     * @param <VV>             the vertex data type
     * @return the new {@link SimpleReachabilityChecker} instance.
     */
    public static <VV, AA> SimpleReachabilityChecker<VV, AA, Integer> newIntCostInstance(
            @NonNull Function<VV, Iterable<Arc<VV, AA>>> nextArcsFunction,
            @NonNull TriFunction<VV, VV, AA, Integer> costFunction,
            @NonNull ReachabilityAlgo<VV, AA, Integer> algo) {
        return new SimpleReachabilityChecker<VV, AA, Integer>(0, Integer.MAX_VALUE, nextArcsFunction, costFunction, Integer::sum, algo);
    }

    /**
     * Creates a new instance which has a int cost function that counts the
     * number of arrows.
     *
     * @param nextArcsFunction a function that given a vertex,
     *                         returns an {@link Iterable} for the arcs
     *                         of that vertex.
     * @param algo             the search algorithm
     * @param <VV>             the vertex data type
     * @return the new {@link SimpleReachabilityChecker} instance.
     */
    public static <VV, AA> SimpleReachabilityChecker<VV, AA, Integer> newIntCostInstance(
            @NonNull Function<VV, Iterable<Arc<VV, AA>>> nextArcsFunction,
            @NonNull ReachabilityAlgo<VV, AA, Integer> algo) {
        return new SimpleReachabilityChecker<VV, AA, Integer>(0, Integer.MAX_VALUE, nextArcsFunction, (u, v, a) -> 1, Integer::sum, algo);
    }


    /**
     * Creates a new instance which has a cost function that returns long
     * numbers.
     *
     * @param nextArcsFunction a function that given a vertex,
     *                         returns an {@link Iterable} for the arcs
     *                         of that vertex.
     * @param costFunction     the cost function
     * @param algo             the search algorithm
     * @param <VV>             the vertex data type
     * @return the new {@link SimpleReachabilityChecker} instance.
     */
    public static <VV, AA> SimpleReachabilityChecker<VV, AA, Long> newLongCostInstance(
            @NonNull Function<VV, Iterable<Arc<VV, AA>>> nextArcsFunction,
            @NonNull TriFunction<VV, VV, AA, Long> costFunction,
            @NonNull ReachabilityAlgo<VV, AA, Long> algo) {
        return new SimpleReachabilityChecker<VV, AA, Long>(0L, Long.MAX_VALUE, nextArcsFunction, costFunction, Long::sum, algo);
    }

    @Override
    protected boolean tryToReach(
            @NonNull Iterable<V> starts,
            @NonNull Predicate<V> goalPredicate,
            @NonNull Function<V, Iterable<Arc<V, A>>> nextArcsFunction, @NonNull C zero,
            @NonNull C positiveInfinity,
            @NonNull C maxCost,
            @NonNull TriFunction<V, V, A, C> costf,
            @NonNull BiFunction<C, C, C> sumf) {
        return algo.tryToReach(
                starts, goalPredicate, nextArcsFunction, zero, positiveInfinity, maxCost, costf, sumf
        );
    }

}
