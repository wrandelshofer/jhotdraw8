package org.jhotdraw8.graph.path;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.graph.Arc;
import org.jhotdraw8.util.TriFunction;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * This sequence builder uses Diijkstra's 'shortest path' algorithm.
 * If more than one shortest path is possible, the builder returns an arbitrary
 * one.
 * <p>
 * This algorithm needs a cost function that returns values {@literal >= 0}
 * for all arrows.
 * <p>
 * References:
 * <dl>
 * <dt>Esger W. Dijkstra (1959), A note on two problems in connexion with graphs,
 * Problem 2.
 * </dt>
 * <dd><a href="https://www-m3.ma.tum.de/twiki/pub/MN0506/WebHome/dijkstra.pdf">tum.de</a></dd>
 * </dl>
 */
public class ArbitraryShortestPathFinder<V, A, C extends Number & Comparable<C>> extends AbstractSequenceFinder<V, A, C> {

    /**
     * @param zero             the zero value, e.g. {@code 0}, {@code 0.0}.
     * @param positiveInfinity the positive infinity value or max value,
     *                         e.g. {@link Integer#MAX_VALUE},
     *                         {@link Double#POSITIVE_INFINITY}.
     * @param nextArcsFunction a function that given a vertex,
     *                         returns an {@link Iterable} for the next arcs
     *                         of that vertex.
     * @param costFunction     the cost function, must return values {@literal >= 0}.
     * @param sumFunction      the sum function, which adds two numbers,
     *                         e.g. {@link Integer#sum}, {@link Double#sum}.
     */
    public ArbitraryShortestPathFinder(@NonNull C zero,
                                       @NonNull C positiveInfinity,
                                       @NonNull Function<V, Iterable<Arc<V, A>>> nextArcsFunction,
                                       @NonNull TriFunction<V, V, A, C> costFunction,
                                       @NonNull BiFunction<C, C, C> sumFunction) {
        super(zero, positiveInfinity, nextArcsFunction, costFunction, sumFunction);
    }

    public static <V, A> ArbitraryShortestPathFinder<V, A, Long> createInstance(
            @NonNull Function<V, Iterable<Arc<V, A>>> nextArcsFunction,
            @NonNull TriFunction<V, V, A, Long> costFunction) {
        return new ArbitraryShortestPathFinder<>(0L, Long.MAX_VALUE, nextArcsFunction, costFunction, Long::sum);
    }

    @Override
    protected @Nullable ArcBackLink<V, A, C> search(
            @NonNull Iterable<V> starts,
            @NonNull Predicate<V> goalPredicate,
            @NonNull C zero,
            @NonNull C positiveInfinity,
            @NonNull C maxCost,
            @NonNull Function<V, Iterable<Arc<V, A>>> nextf,
            @NonNull TriFunction<V, V, A, C> costf,
            @NonNull BiFunction<C, C, C> sumf) {
        return new ArbitraryShortestPathFinderAlgo<V, A, C>().search(
                starts, goalPredicate, zero, positiveInfinity, maxCost, nextf, costf, sumf
        );
    }
}
