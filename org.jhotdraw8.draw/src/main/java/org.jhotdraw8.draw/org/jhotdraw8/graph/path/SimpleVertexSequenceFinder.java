package org.jhotdraw8.graph.path;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.ImmutableList;
import org.jhotdraw8.collection.OrderedPair;
import org.jhotdraw8.graph.path.algo.VertexPathSearchAlgo;
import org.jhotdraw8.graph.path.backlink.VertexBackLink;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Implements the {@link VertexSequenceFinder} interface.
 *
 * @param <V> the vertex data type
 * @param <C> the cost number type
 */
public class SimpleVertexSequenceFinder<V, C extends Number & Comparable<C>> implements VertexSequenceFinder<V, C> {
    private final @NonNull C zero;
    private final @NonNull C positiveInfinity;
    private final @NonNull Function<V, Iterable<V>> nextVerticesFunction;
    private final @NonNull BiFunction<V, V, C> costFunction;
    private final @NonNull BiFunction<C, C, C> sumFunction;
    private final @NonNull VertexPathSearchAlgo<V, C> algo;

    /**
     * Creates a new instance.
     *
     * @param zero                 the zero value, e.g. {@code 0}, {@code 0.0}.
     * @param positiveInfinity     the positive infinity value or max value,
     *                             e.g. {@link Integer#MAX_VALUE},
     *                             {@link Double#POSITIVE_INFINITY}.
     * @param nextVerticesFunction a function that given a vertex,
     *                             returns an {@link Iterable} for the next vertices
     *                             of that vertex.
     * @param costFunction         the cost function
     * @param sumFunction          the sum function, which adds two numbers,
     *                             e.g. {@link Integer#sum}, {@link Double#sum}.
     * @param algo                 the search algorithm
     */
    public SimpleVertexSequenceFinder(
            @NonNull C zero,
            @NonNull C positiveInfinity,
            @NonNull Function<V, Iterable<V>> nextVerticesFunction,
            @NonNull BiFunction<V, V, C> costFunction,
            @NonNull BiFunction<C, C, C> sumFunction,
            @NonNull VertexPathSearchAlgo<V, C> algo) {
        this.zero = zero;
        this.positiveInfinity = positiveInfinity;
        this.nextVerticesFunction = nextVerticesFunction;
        this.costFunction = costFunction;
        this.sumFunction = sumFunction;
        this.algo = algo;
    }

    /**
     * Creates a new instance which has a cost function that returns integer
     * numbers.
     *
     * @param nextVerticesFunction a function that given a vertex,
     *                             returns an {@link Iterable} for the next vertices
     *                             of that vertex.
     * @param costFunction         the cost function
     * @param algo                 the search algorithm
     * @param <VV>                 the vertex data type
     * @return the new {@link SimpleVertexSequenceFinder} instance.
     */
    public static <VV> SimpleVertexSequenceFinder<VV, Integer> newIntCostInstance(
            @NonNull Function<VV, Iterable<VV>> nextVerticesFunction,
            @NonNull BiFunction<VV, VV, Integer> costFunction,
            @NonNull VertexPathSearchAlgo<VV, Integer> algo) {
        return new SimpleVertexSequenceFinder<>(0, Integer.MAX_VALUE, nextVerticesFunction, costFunction, Integer::sum, algo);
    }

    /**
     * Creates a new instance which has a int cost function that counts the
     * number of arrows.
     *
     * @param nextVerticesFunction a function that given a vertex,
     *                             returns an {@link Iterable} for the next vertices
     *                             of that vertex.
     * @param algo                 the search algorithm
     * @param <VV>                 the vertex data type
     * @return the new {@link SimpleVertexSequenceFinder} instance.
     */
    public static <VV> SimpleVertexSequenceFinder<VV, Integer> newIntCostInstance(
            @NonNull Function<VV, Iterable<VV>> nextVerticesFunction,
            @NonNull VertexPathSearchAlgo<VV, Integer> algo) {
        return new SimpleVertexSequenceFinder<>(0, Integer.MAX_VALUE, nextVerticesFunction, (u, v) -> 1, Integer::sum, algo);
    }

    /**
     * Creates a new instance which has a cost function that returns long
     * numbers.
     *
     * @param nextVerticesFunction a function that given a vertex,
     *                             returns an {@link Iterable} for the next vertices
     *                             of that vertex.
     * @param costFunction         the cost function
     * @param algo                 the search algorithm
     * @param <VV>                 the vertex data type
     * @return the new {@link SimpleVertexSequenceFinder} instance.
     */
    public static <VV> SimpleVertexSequenceFinder<VV, Long> newLongCostInstance(
            @NonNull Function<VV, Iterable<VV>> nextVerticesFunction,
            @NonNull BiFunction<VV, VV, Long> costFunction,
            @NonNull VertexPathSearchAlgo<VV, Long> algo) {
        return new SimpleVertexSequenceFinder<>(0L, Long.MAX_VALUE, nextVerticesFunction, costFunction, Long::sum, algo);
    }

    @Override
    public @Nullable OrderedPair<ImmutableList<V>, C> findVertexSequence(@NonNull Iterable<V> startVertices, @NonNull Predicate<V> goalPredicate, @NonNull C searchLimit) {
        return VertexBackLink.toVertexSequence(algo.search(
                startVertices, goalPredicate, nextVerticesFunction, zero, positiveInfinity, searchLimit, costFunction, sumFunction
        ), VertexBackLink::getVertex);
    }

    @Override
    public @Nullable OrderedPair<ImmutableList<V>, C> findVertexSequenceOverWaypoints(@NonNull Iterable<V> waypoints, @NonNull C searchLimit) {
        return VertexSequenceFinder.findVertexSequenceOverWaypoints(
                waypoints,
                (start, goal) -> this.findVertexSequence(start, goal, searchLimit),
                zero,
                sumFunction

        );
    }


}
