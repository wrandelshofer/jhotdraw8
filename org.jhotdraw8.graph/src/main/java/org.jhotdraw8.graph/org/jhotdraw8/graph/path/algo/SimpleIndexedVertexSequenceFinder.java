/*
 * @(#)SimpleIndexedVertexSequenceFinder.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.path.algo;

import org.jhotdraw8.collection.pair.SimpleOrderedPair;
import org.jhotdraw8.graph.algo.AddToIntSet;
import org.jhotdraw8.graph.algo.AddToSet;
import org.jhotdraw8.graph.path.backlink.IndexedVertexBackLinkWithCost;
import org.jhotdraw8.icollection.persistent.PersistentList;
import org.jspecify.annotations.Nullable;

import java.util.Spliterator;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Implements the {@link VertexSequenceFinder} interface.
 *
 * @param <C> the cost number type
 */
public class SimpleIndexedVertexSequenceFinder<C extends Number & Comparable<C>> implements VertexSequenceFinder<Integer, C> {
    private final IndexedVertexPathSearchAlgo<C> algo;
    private final C zero;
    private final Function<Integer, Spliterator.OfInt> nextVerticesFunction;
    private final BiFunction<Integer, Integer, C> costFunction;
    private final BiFunction<C, C, C> sumFunction;

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
     */
    public SimpleIndexedVertexSequenceFinder(
            C zero,
            Function<Integer, Spliterator.OfInt> nextVerticesFunction,
            BiFunction<Integer, Integer, C> costFunction,
            BiFunction<C, C, C> sumFunction,
            IndexedVertexPathSearchAlgo<C> algo) {
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
     * @return the new {@link SimpleIndexedVertexSequenceFinder} instance.
     */
    public static SimpleIndexedVertexSequenceFinder<Integer> newIntCostInstance(
            Function<Integer, Spliterator.OfInt> nextVerticesFunction,
            BiFunction<Integer, Integer, Integer> costFunction,
            IndexedVertexPathSearchAlgo<Integer> algo) {
        return new SimpleIndexedVertexSequenceFinder<>(0, nextVerticesFunction, costFunction, Integer::sum, algo);
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
            Function<Integer, Spliterator.OfInt> nextVerticesFunction,
            IndexedVertexPathSearchAlgo<Integer> algo) {
        return new SimpleIndexedVertexSequenceFinder<>(0, nextVerticesFunction, (u, v) -> 1, Integer::sum, algo);
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
            Function<Integer, Spliterator.OfInt> nextVerticesFunction,
            BiFunction<Integer, Integer, Long> costFunction,
            IndexedVertexPathSearchAlgo<Long> algo) {
        return new SimpleIndexedVertexSequenceFinder<>(0L, nextVerticesFunction, costFunction, Long::sum, algo);
    }

    @Override
    public @Nullable SimpleOrderedPair<PersistentList<Integer>, C> findVertexSequence(
            Iterable<Integer> startVertices, Predicate<Integer> goalPredicate,
            int maxDepth, C costLimit, AddToSet<Integer> visited) {
        AddToIntSet visitedAsInt = visited instanceof AddToIntSet ? (AddToIntSet) visited : visited::add;
        return IndexedVertexBackLinkWithCost.toVertexSequence(algo.search(
                startVertices, goalPredicate::test, nextVerticesFunction, maxDepth, zero, costLimit, costFunction, sumFunction,
                visitedAsInt), IndexedVertexBackLinkWithCost::getVertex);
    }

    @Override
    public @Nullable SimpleOrderedPair<PersistentList<Integer>, C> findVertexSequenceOverWaypoints(
            Iterable<Integer> waypoints, int maxDepth, C costLimit, Supplier<AddToSet<Integer>> visitedSetFactory) {
        return VertexSequenceFinder.findVertexSequenceOverWaypoints(
                waypoints,
                (start, goal) -> this.findVertexSequence(start, goal, maxDepth, costLimit, visitedSetFactory.get()),
                zero,
                sumFunction

        );
    }


}
