/*
 * @(#)SimpleCombinedSequenceFinder.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.path.algo;

import org.jhotdraw8.base.function.Function3;
import org.jhotdraw8.collection.pair.OrderedPair;
import org.jhotdraw8.collection.pair.SimpleOrderedPair;
import org.jhotdraw8.graph.Arc;
import org.jhotdraw8.graph.algo.AddToSet;
import org.jhotdraw8.graph.path.backlink.ArcBackLinkWithCost;
import org.jhotdraw8.icollection.persistent.PersistentList;
import org.jspecify.annotations.Nullable;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Implements the {@link CombinedSequenceFinder} interface.
 *
 * @param <V> the vertex data type
 * @param <A> the arrow data type
 * @param <C> the cost number type
 */
public class SimpleCombinedSequenceFinder<V, A, C extends Number & Comparable<C>> implements CombinedSequenceFinder<V, A, C> {

    private final C zero;
    private final Function<V, Iterable<Arc<V, A>>> nextArcsFunction;
    private final Function3<V, V, A, C> costFunction;
    private final BiFunction<C, C, C> sumFunction;
    private final ArcPathSearchAlgo<V, A, C> algo;


    /**
     * Creates a new instance.
     *
     * @param zero             the zero value, e.g. {@code 0}, {@code 0.0}.
     * @param nextArcsFunction a function that given a vertex,
     *                         returns an {@link Iterable} for the {@link Arc}s
     *                         starting at that vertex.
     * @param costFunction     the cost function
     * @param sumFunction      the sum function, which adds two numbers,
     *                         e.g. {@link Integer#sum}, {@link Double#sum}.
     * @param algo             the search algorithm
     */
    public SimpleCombinedSequenceFinder(
            C zero,
            Function<V, Iterable<Arc<V, A>>> nextArcsFunction,
            Function3<V, V, A, C> costFunction,
            BiFunction<C, C, C> sumFunction,
            ArcPathSearchAlgo<V, A, C> algo) {
        if (zero.doubleValue() != 0.0) {
            throw new IllegalArgumentException("zero(" + zero + ") is != 0");
        }
        this.zero = zero;
        this.nextArcsFunction = nextArcsFunction;
        this.costFunction = costFunction;
        this.sumFunction = sumFunction;
        this.algo = algo;
    }


    /**
     * Creates a new instance with a cost function that returns integer
     * numbers.
     *
     * @param nextArcsFunction a function that given a vertex,
     *                         returns an {@link Iterable} for the next arcs
     *                         of that vertex.
     * @param costFunction     the cost function
     * @param algo             the search algorithm
     * @param <VV>             the vertex data type
     * @param <AA>             the arrow data type
     * @return the new {@link SimpleCombinedSequenceFinder} instance.
     */
    public static <VV, AA> SimpleCombinedSequenceFinder<VV, AA, Integer> newIntCostInstance(
            Function<VV, Iterable<Arc<VV, AA>>> nextArcsFunction,
            Function3<VV, VV, AA, Integer> costFunction,
            ArcPathSearchAlgo<VV, AA, Integer> algo) {
        return new SimpleCombinedSequenceFinder<>(0, nextArcsFunction, costFunction, Integer::sum, algo);
    }

    /**
     * Creates a new instance with a cost function that returns integer
     * numbers.
     *
     * @param nextArcsFunction a function that given a vertex,
     *                         returns an {@link Iterable} for the next arcs
     *                         of that vertex.
     * @param costFunction     the cost function
     * @param algo             the search algorithm
     * @param <VV>             the vertex data type
     * @param <AA>             the arrow data type
     * @return the new {@link SimpleCombinedSequenceFinder} instance.
     */
    public static <VV, AA> SimpleCombinedSequenceFinder<VV, AA, Integer> newIntCostInstance(
            Function<VV, Iterable<Arc<VV, AA>>> nextArcsFunction,
            BiFunction<VV, VV, Integer> costFunction,
            ArcPathSearchAlgo<VV, AA, Integer> algo) {
        return new SimpleCombinedSequenceFinder<>(0, nextArcsFunction, (u, v, a) -> costFunction.apply(u, v), Integer::sum, algo);
    }

    /**
     * Creates a new instance with a cost function that counts the number
     * of arrows.
     *
     * @param nextArcsFunction a function that given a vertex,
     *                         returns an {@link Iterable} for the next arcs
     *                         of that vertex.
     * @param algo             the search algorithm
     * @param <VV>             the vertex data type
     * @param <AA>             the arrow data type
     * @return the new {@link SimpleCombinedSequenceFinder} instance.
     */
    public static <VV, AA> SimpleCombinedSequenceFinder<VV, AA, Integer> newIntCostInstance(
            Function<VV, Iterable<Arc<VV, AA>>> nextArcsFunction,
            ArcPathSearchAlgo<VV, AA, Integer> algo) {
        return new SimpleCombinedSequenceFinder<>(0, nextArcsFunction, (u, v, a) -> 1, Integer::sum, algo);
    }

    /**
     * Creates a new instance with a cost function that returns double
     * numbers.
     *
     * @param nextArcsFunction a function that given a vertex,
     *                         returns an {@link Iterable} for the next arcs
     *                         of that vertex.
     * @param costFunction     the cost function
     * @param algo             the search algorithm
     * @param <VV>             the vertex data type
     * @param <AA>             the arrow data type
     * @return the new {@link SimpleCombinedSequenceFinder} instance.
     */
    public static <VV, AA> SimpleCombinedSequenceFinder<VV, AA, Double> newDoubleCostInstance(
            Function<VV, Iterable<Arc<VV, AA>>> nextArcsFunction,
            Function3<VV, VV, AA, Double> costFunction,
            ArcPathSearchAlgo<VV, AA, Double> algo) {
        return new SimpleCombinedSequenceFinder<>(0.0, nextArcsFunction, costFunction, Double::sum, algo);
    }

    /**
     * Creates a new instance with a cost function that returns long
     * numbers.
     *
     * @param nextArcsFunction a function that given a vertex,
     *                         returns an {@link Iterable} for the next arcs
     *                         of that vertex.
     * @param costFunction     the cost function
     * @param algo             the search algorithm
     * @param <VV>             the vertex data type
     * @param <AA>             the arrow data type
     * @return the new {@link SimpleCombinedSequenceFinder} instance.
     */
    public static <VV, AA> SimpleCombinedSequenceFinder<VV, AA, Long> newLongCostInstance(
            Function<VV, Iterable<Arc<VV, AA>>> nextArcsFunction,
            Function3<VV, VV, AA, Long> costFunction,
            ArcPathSearchAlgo<VV, AA, Long> algo) {
        return new SimpleCombinedSequenceFinder<>(0L, nextArcsFunction, costFunction, Long::sum, algo);
    }

    @Override
    public @Nullable OrderedPair<PersistentList<Arc<V, A>>, C> findArcSequence(Iterable<V> startVertices, Predicate<V> goalPredicate, int maxDepth, C costLimit, AddToSet<V> visited) {
        return ArcBackLinkWithCost.toArrowSequence(algo.search(
                startVertices, goalPredicate, nextArcsFunction, maxDepth, zero, costLimit, costFunction, sumFunction,
                visited), (a, b) -> new Arc<>(a.getVertex(), b.getVertex(), b.getArrow()));
    }

    @Override
    public @Nullable OrderedPair<PersistentList<Arc<V, A>>, C> findArcSequenceOverWaypoints(Iterable<V> waypoints, int maxDepth, C costLimit, Supplier<AddToSet<V>> visitedSetFactory) {
        return ArcSequenceFinder.findArcSequenceOverWaypoints(waypoints, (start, goal) -> findArcSequence(start, goal, maxDepth, costLimit, visitedSetFactory.get()), zero, sumFunction);
    }

    @Override
    public @Nullable SimpleOrderedPair<PersistentList<A>, C> findArrowSequence(Iterable<V> startVertices, Predicate<V> goalPredicate, int maxDepth, C costLimit, AddToSet<V> visited) {
        return ArcBackLinkWithCost.toArrowSequence(algo.search(
                startVertices, goalPredicate, nextArcsFunction, maxDepth, zero, costLimit, costFunction, sumFunction,
                visited), (a, b) -> b.getArrow());
    }

    @Override
    public @Nullable SimpleOrderedPair<PersistentList<A>, C> findArrowSequenceOverWaypoints(Iterable<V> waypoints, int maxDepth, C costLimit, Supplier<AddToSet<V>> visitedSetFactory) {
        return ArrowSequenceFinder.findArrowSequenceOverWaypoints(waypoints, (start, goal) -> findArrowSequence(start, goal, maxDepth, costLimit, visitedSetFactory.get()), zero, sumFunction);
    }

    @Override
    public @Nullable SimpleOrderedPair<PersistentList<V>, C> findVertexSequence(Iterable<V> startVertices, Predicate<V> goalPredicate, int maxDepth, C costLimit, AddToSet<V> visited) {
        return ArcBackLinkWithCost.toVertexSequence(algo.search(
                startVertices, goalPredicate, nextArcsFunction, maxDepth, zero, costLimit, costFunction, sumFunction,
                visited), ArcBackLinkWithCost::getVertex);
    }

    @Override
    public @Nullable SimpleOrderedPair<PersistentList<V>, C> findVertexSequenceOverWaypoints(Iterable<V> waypoints, int maxDepth, C costLimit, Supplier<AddToSet<V>> visitedSetFactory) {
        return VertexSequenceFinder.findVertexSequenceOverWaypoints(waypoints, (start, goal) -> findVertexSequence(start, goal, maxDepth, costLimit, visitedSetFactory.get()), zero, sumFunction);
    }


}
