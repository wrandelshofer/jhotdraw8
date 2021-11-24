package org.jhotdraw8.graph.path;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * This {@link VertexSequenceFinder} uses Diijkstra's 'shortest path' algorithm.
 * If more than one shortest path is possible, the builder returns an arbitrary
 * one.
 * <p>
 * The provided cost function must always return a value &gt;= 0.
 * <p>
 * Invariants of a sequences returned by the {@code findSequence} methods
 * (excluding the {@code findSequenceOverWaypoints} methods):
 * <ul>
 *     <li>A sequence is a path.</li>
 *     <li>A sequence is directed. That is, the sequence respects
 *     the direction of the arrows in the graph.</li>
 *     <li>A sequence does not include zero-weight arrows that form cycles.</li>
 *     <li>A sequences has minimal cost and has minimal number of arrows/vertices.</li>
 * </ul>
 */
public class ArbitraryShortestVertexPathFinder<V, C extends Number & Comparable<C>> extends AbstractVertexSequenceFinder<V, C> {

    /**
     * Creates a new instance.
     *
     * @param zero                 the zero value, e.g. {@code 0}, {@code 0.0}.
     * @param positiveInfinity     the positive infinity value or max value,
     *                             e.g. {@link Integer#MAX_VALUE},
     *                             {@link Double#POSITIVE_INFINITY}.
     * @param maxCost              the maximal cost of a sequence,
     *                             e.g. {@link Integer#MAX_VALUE}, {@link Double#MAX_VALUE}.
     * @param nextVerticesFunction a function that given a vertex,
     *                             returns an {@link Iterable} for the next vertices
     *                             of that vertex.
     * @param costFunction         the cost function, all costs must be &gt;= 0.
     * @param sumFunction          the sum function, which adds two numbers,
     *                             e.g. {@link Integer#sum}, {@link Double#sum}.
     */
    public ArbitraryShortestVertexPathFinder(@NonNull C zero, @NonNull C positiveInfinity, @NonNull C maxCost, @NonNull Function<V, Iterable<V>> nextVerticesFunction, @NonNull BiFunction<V, V, C> costFunction, @NonNull BiFunction<C, C, C> sumFunction) {
        super(zero, positiveInfinity, maxCost, nextVerticesFunction, costFunction, sumFunction);
    }


    @Override
    protected @Nullable VertexBackLink<V, C> search(
            @NonNull Iterable<V> starts,
            @NonNull Predicate<V> goalPredicate,
            @NonNull C zero,
            @NonNull C positiveInfinity,
            @NonNull C maxCost,
            @NonNull Function<V, Iterable<V>> nextf,
            @NonNull BiFunction<V, V, C> costf,
            @NonNull BiFunction<C, C, C> sumf) {
        return new ArbitraryShortestVertexPathSearchAlgo<V, C>().search(
                starts, goalPredicate, zero, positiveInfinity, maxCost, nextf, costf, sumf
        );
    }
}
