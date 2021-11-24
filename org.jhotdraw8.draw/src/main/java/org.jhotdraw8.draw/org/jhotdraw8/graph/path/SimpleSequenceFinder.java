package org.jhotdraw8.graph.path;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.graph.Arc;
import org.jhotdraw8.graph.path.algo.PathSearchAlgo;
import org.jhotdraw8.graph.path.backlink.ArcBackLink;
import org.jhotdraw8.util.TriFunction;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Implements the {@link SequenceFinder} interface.
 *
 * @param <V> the vertex data type
 * @param <A> the arrow data type
 * @param <C> the cost number type
 */
public class SimpleSequenceFinder<V, A, C extends Number & Comparable<C>> extends AbstractSequenceFinder<V, A, C> {
    private final @NonNull PathSearchAlgo<V, A, C> algo;

    /**
     * Creates a new instance.
     *
     * @param zero             the zero value, e.g. {@code 0}, {@code 0.0}.
     * @param positiveInfinity the positive infinity value or max value,
     *                         e.g. {@link Integer#MAX_VALUE},
     *                         {@link Double#POSITIVE_INFINITY}.
     * @param nextArcsFunction a function that given a vertex,
     *                         returns an {@link Iterable} for the next arcs
     *                         of that vertex.
     * @param costFunction     the cost function
     * @param sumFunction      the sum function, which adds two numbers,
     *                         e.g. {@link Integer#sum}, {@link Double#sum}.
     * @param algo             the search algorithm
     */
    public SimpleSequenceFinder(
            @NonNull C zero,
            @NonNull C positiveInfinity,
            @NonNull Function<V, Iterable<Arc<V, A>>> nextArcsFunction,
            @NonNull TriFunction<V, V, A, C> costFunction,
            @NonNull BiFunction<C, C, C> sumFunction,
            @NonNull PathSearchAlgo<V, A, C> algo) {
        super(zero, positiveInfinity, nextArcsFunction, costFunction, sumFunction);
        this.algo = algo;
    }


    /**
     * Creates a new instance which has a cost function that returns integer
     * numbers.
     *
     * @param nextArcsFunction a function that given a vertex,
     *                         returns an {@link Iterable} for the next arcs
     *                         of that vertex.
     * @param costFunction     the cost function
     * @param algo             the search algorithm
     * @param <VV>             the vertex data type
     * @param <AA>             the arrow data type
     * @return the new {@link SimpleSequenceFinder} instance.
     */
    public static <VV, AA> SimpleSequenceFinder<VV, AA, Integer> newIntCostInstance(
            @NonNull Function<VV, Iterable<Arc<VV, AA>>> nextArcsFunction,
            @NonNull TriFunction<VV, VV, AA, Integer> costFunction,
            @NonNull PathSearchAlgo<VV, AA, Integer> algo) {
        return new SimpleSequenceFinder<>(0, Integer.MAX_VALUE, nextArcsFunction, costFunction, Integer::sum, algo);
    }

    /**
     * Creates a new instance which has a cost function that returns integer
     * numbers.
     *
     * @param nextArcsFunction a function that given a vertex,
     *                         returns an {@link Iterable} for the next arcs
     *                         of that vertex.
     * @param costFunction     the cost function
     * @param algo             the search algorithm
     * @param <VV>             the vertex data type
     * @param <AA>             the arrow data type
     * @return the new {@link SimpleSequenceFinder} instance.
     */
    public static <VV, AA> SimpleSequenceFinder<VV, AA, Integer> newIntCostInstance(
            @NonNull Function<VV, Iterable<Arc<VV, AA>>> nextArcsFunction,
            @NonNull BiFunction<VV, VV, Integer> costFunction,
            @NonNull PathSearchAlgo<VV, AA, Integer> algo) {
        return new SimpleSequenceFinder<>(0, Integer.MAX_VALUE, nextArcsFunction, (u, v, a) -> costFunction.apply(u, v), Integer::sum, algo);
    }

    /**
     * Creates a new instance which has a cost function that counts the number
     * of arrows.
     *
     * @param nextArcsFunction a function that given a vertex,
     *                         returns an {@link Iterable} for the next arcs
     *                         of that vertex.
     * @param algo             the search algorithm
     * @param <VV>             the vertex data type
     * @param <AA>             the arrow data type
     * @return the new {@link SimpleSequenceFinder} instance.
     */
    public static <VV, AA> SimpleSequenceFinder<VV, AA, Integer> newIntCostInstance(
            @NonNull Function<VV, Iterable<Arc<VV, AA>>> nextArcsFunction,
            @NonNull PathSearchAlgo<VV, AA, Integer> algo) {
        return new SimpleSequenceFinder<>(0, Integer.MAX_VALUE, nextArcsFunction, (u, v, a) -> 1, Integer::sum, algo);
    }

    /**
     * Creates a new instance which has a cost function that returns double
     * numbers.
     *
     * @param nextArcsFunction a function that given a vertex,
     *                         returns an {@link Iterable} for the next arcs
     *                         of that vertex.
     * @param costFunction     the cost function
     * @param algo             the search algorithm
     * @param <VV>             the vertex data type
     * @param <AA>             the arrow data type
     * @return the new {@link SimpleSequenceFinder} instance.
     */
    public static <VV, AA> SimpleSequenceFinder<VV, AA, Double> newDoubleCostInstance(
            @NonNull Function<VV, Iterable<Arc<VV, AA>>> nextArcsFunction,
            @NonNull TriFunction<VV, VV, AA, Double> costFunction,
            @NonNull PathSearchAlgo<VV, AA, Double> algo) {
        return new SimpleSequenceFinder<>(0.0, Double.POSITIVE_INFINITY, nextArcsFunction, costFunction, Double::sum, algo);
    }


    /**
     * Creates a new instance which has a cost function that returns long
     * numbers.
     *
     * @param nextArcsFunction a function that given a vertex,
     *                         returns an {@link Iterable} for the next arcs
     *                         of that vertex.
     * @param costFunction     the cost function
     * @param algo             the search algorithm
     * @param <VV>             the vertex data type
     * @param <AA>             the arrow data type
     * @return the new {@link SimpleSequenceFinder} instance.
     */
    public static <VV, AA> SimpleSequenceFinder<VV, AA, Long> newLongCostInstance(
            @NonNull Function<VV, Iterable<Arc<VV, AA>>> nextArcsFunction,
            @NonNull TriFunction<VV, VV, AA, Long> costFunction,
            @NonNull PathSearchAlgo<VV, AA, Long> algo) {
        return new SimpleSequenceFinder<>(0L, Long.MAX_VALUE, nextArcsFunction, costFunction, Long::sum, algo);
    }

    @Override
    protected @Nullable ArcBackLink<V, A, C> search(
            @NonNull Iterable<V> starts,
            @NonNull Predicate<V> goalPredicate,
            @NonNull Function<V, Iterable<Arc<V, A>>> nextArcsFunction,
            @NonNull C zero,
            @NonNull C positiveInfinity,
            @NonNull C maxCost,
            @NonNull TriFunction<V, V, A, C> costFunction,
            @NonNull BiFunction<C, C, C> sumFunction) {
        return algo.search(
                starts, goalPredicate, nextArcsFunction, zero, positiveInfinity, maxCost, costFunction, sumFunction
        );
    }
}
