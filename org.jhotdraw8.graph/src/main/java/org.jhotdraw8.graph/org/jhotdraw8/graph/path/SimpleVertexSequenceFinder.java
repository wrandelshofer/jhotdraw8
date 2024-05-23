/*
 * @(#)SimpleVertexSequenceFinder.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.path;

import org.jhotdraw8.collection.pair.SimpleOrderedPair;
import org.jhotdraw8.graph.algo.AddToSet;
import org.jhotdraw8.graph.path.algo.VertexPathSearchAlgo;
import org.jhotdraw8.graph.path.backlink.VertexBackLinkWithCost;
import org.jhotdraw8.icollection.immutable.ImmutableList;
import org.jspecify.annotations.Nullable;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Implements the {@link VertexSequenceFinder} interface.
 *
 * @param <V> the vertex data type
 * @param <C> the cost number type
 */
public class SimpleVertexSequenceFinder<V, C extends Number & Comparable<C>> implements VertexSequenceFinder<V, C> {
    private final C zero;
    private final Function<V, Iterable<V>> nextVerticesFunction;
    private final BiFunction<V, V, C> costFunction;
    private final BiFunction<C, C, C> sumFunction;
    private final VertexPathSearchAlgo<V, C> algo;

    /**
     * Creates a new instance.
     *
     * @param zero                 the zero value, e.g. {@code 0}, {@code 0.0}.
     * @param nextVerticesFunction a function that given a vertex,
     *                             returns an {@link Iterable} for the next vertices
     *                             of that vertex.
     * @param costFunction         the cost function
     * @param sumFunction          the sum function, which adds two numbers,
     *                             e.g. {@link Integer#sum}, {@link Double#sum}.
     * @param algo                 the search algorithm
     */
    public SimpleVertexSequenceFinder(
            C zero,
            Function<V, Iterable<V>> nextVerticesFunction,
            BiFunction<V, V, C> costFunction,
            BiFunction<C, C, C> sumFunction,
            VertexPathSearchAlgo<V, C> algo) {
        this.zero = zero;
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
            Function<VV, Iterable<VV>> nextVerticesFunction,
            BiFunction<VV, VV, Integer> costFunction,
            VertexPathSearchAlgo<VV, Integer> algo) {
        return new SimpleVertexSequenceFinder<>(0, nextVerticesFunction, costFunction, Integer::sum, algo);
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
            Function<VV, Iterable<VV>> nextVerticesFunction,
            VertexPathSearchAlgo<VV, Integer> algo) {
        return new SimpleVertexSequenceFinder<>(0, nextVerticesFunction, (u, v) -> 1, Integer::sum, algo);
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
            Function<VV, Iterable<VV>> nextVerticesFunction,
            BiFunction<VV, VV, Long> costFunction,
            VertexPathSearchAlgo<VV, Long> algo) {
        return new SimpleVertexSequenceFinder<>(0L, nextVerticesFunction, costFunction, Long::sum, algo);
    }

    @Override
    public @Nullable SimpleOrderedPair<ImmutableList<V>, C> findVertexSequence(Iterable<V> startVertices, Predicate<V> goalPredicate, int maxDepth, C costLimit, AddToSet<V> visited) {
        return VertexBackLinkWithCost.toVertexSequence(algo.search(
                startVertices, goalPredicate, nextVerticesFunction, maxDepth, zero, costLimit, costFunction, sumFunction,
                visited), VertexBackLinkWithCost::getVertex);
    }

    @Override
    public @Nullable SimpleOrderedPair<ImmutableList<V>, C> findVertexSequenceOverWaypoints(Iterable<V> waypoints, int maxDepth, C costLimit, Supplier<AddToSet<V>> visitedSetFactory) {
        return VertexSequenceFinder.findVertexSequenceOverWaypoints(
                waypoints,
                (start, goal) -> this.findVertexSequence(start, goal, maxDepth, costLimit, visitedSetFactory.get()),
                zero,
                sumFunction

        );
    }


}
