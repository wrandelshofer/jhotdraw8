package org.jhotdraw8.graph.path;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.ImmutableList;
import org.jhotdraw8.collection.OrderedPair;
import org.jhotdraw8.graph.path.algo.IndexedVertexPathSearchAlgo;
import org.jhotdraw8.graph.path.backlink.IndexedVertexBackLinkWithCost;

import java.util.Spliterator;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Implements the {@link VertexSequenceFinder} interface.
 *
 * @param <C> the cost number type
 */
public class SimpleIndexedVertexSequenceFinder<C extends Number & Comparable<C>> implements VertexSequenceFinder<Integer, C> {
    private final @NonNull IndexedVertexPathSearchAlgo<C> algo;
    private final @NonNull C zero;
    private final @NonNull C positiveInfinity;
    private final @NonNull Function<Integer, Spliterator.OfInt> nextVerticesFunction;
    private final @NonNull BiFunction<Integer, Integer, C> costFunction;
    private final @NonNull BiFunction<C, C, C> sumFunction;

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
     */
    public SimpleIndexedVertexSequenceFinder(
            @NonNull C zero,
            @NonNull C positiveInfinity,
            @NonNull Function<Integer, Spliterator.OfInt> nextVerticesFunction,
            @NonNull BiFunction<Integer, Integer, C> costFunction,
            @NonNull BiFunction<C, C, C> sumFunction,
            @NonNull IndexedVertexPathSearchAlgo<C> algo) {
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
     * @return the new {@link SimpleIndexedVertexSequenceFinder} instance.
     */
    public static SimpleIndexedVertexSequenceFinder<Integer> newIntCostInstance(
            @NonNull Function<Integer, Spliterator.OfInt> nextVerticesFunction,
            @NonNull BiFunction<Integer, Integer, Integer> costFunction,
            @NonNull IndexedVertexPathSearchAlgo<Integer> algo) {
        return new SimpleIndexedVertexSequenceFinder<>(0, Integer.MAX_VALUE, nextVerticesFunction, costFunction, Integer::sum, algo);
    }

    /**
     * Creates a new instance which has a int cost function that counts the
     * number of arrows.
     *
     * @param nextVerticesFunction a function that given a vertex,
     *                             returns an {@link Iterable} for the next vertices
     *                             of that vertex.
     * @param algo                 the search algorithm
     * @return the new {@link SimpleIndexedVertexSequenceFinder} instance.
     */
    public static SimpleIndexedVertexSequenceFinder<Integer> newIntCostInstance(
            @NonNull Function<Integer, Spliterator.OfInt> nextVerticesFunction,
            @NonNull IndexedVertexPathSearchAlgo<Integer> algo) {
        return new SimpleIndexedVertexSequenceFinder<>(0, Integer.MAX_VALUE, nextVerticesFunction, (u, v) -> 1, Integer::sum, algo);
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
     * @return the new {@link SimpleIndexedVertexSequenceFinder} instance.
     */
    public static SimpleIndexedVertexSequenceFinder<Long> newLongCostInstance(
            @NonNull Function<Integer, Spliterator.OfInt> nextVerticesFunction,
            @NonNull BiFunction<Integer, Integer, Long> costFunction,
            @NonNull IndexedVertexPathSearchAlgo<Long> algo) {
        return new SimpleIndexedVertexSequenceFinder<>(0L, Long.MAX_VALUE, nextVerticesFunction, costFunction, Long::sum, algo);
    }

    @Override
    public @Nullable OrderedPair<ImmutableList<Integer>, C> findVertexSequence(
            @NonNull Iterable<Integer> startVertices, @NonNull Predicate<Integer> goalPredicate,
            int maxDepth, @NonNull C costLimit) {
        return IndexedVertexBackLinkWithCost.toVertexSequence(algo.search(
                startVertices, goalPredicate::test, nextVerticesFunction, maxDepth, costLimit, zero, positiveInfinity, costFunction, sumFunction
        ), IndexedVertexBackLinkWithCost::getVertex);
    }

    @Override
    public @Nullable OrderedPair<ImmutableList<Integer>, C> findVertexSequenceOverWaypoints(
            @NonNull Iterable<Integer> waypoints, int maxDepth, @NonNull C costLimit) {
        return VertexSequenceFinder.findVertexSequenceOverWaypoints(
                waypoints,
                (start, goal) -> this.findVertexSequence(start, goal, maxDepth, costLimit),
                zero,
                sumFunction

        );
    }


}
