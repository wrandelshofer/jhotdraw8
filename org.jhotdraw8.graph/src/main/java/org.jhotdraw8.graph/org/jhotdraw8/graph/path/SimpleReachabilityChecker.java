/*
 * @(#)SimpleReachabilityChecker.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.path;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.base.function.TriFunction;
import org.jhotdraw8.collection.function.AddToSet;
import org.jhotdraw8.graph.Arc;
import org.jhotdraw8.graph.path.algo.ArcReachabilityAlgo;

import java.util.Collections;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Implements the {@link ReachabilityChecker} interface.
 *
 * @param <V> the vertex data type
 * @param <A> the arrow data type
 * @param <C> the cost number type
 */
public class SimpleReachabilityChecker<V, A, C extends Number & Comparable<C>>
        implements ReachabilityChecker<V, C> {
    private final @NonNull ArcReachabilityAlgo<V, A, C> algo;
    private final @NonNull C zero;
    private final @NonNull Function<V, Iterable<Arc<V, A>>> nextArcsFunction;
    private final @NonNull TriFunction<V, V, A, C> costFunction;
    private final @NonNull BiFunction<C, C, C> sumFunction;

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
     * @param algo             The search algorithm.
     */
    public SimpleReachabilityChecker(
            @NonNull C zero,
            @NonNull Function<V, Iterable<Arc<V, A>>> nextArcsFunction,
            @NonNull TriFunction<V, V, A, C> costFunction,
            @NonNull BiFunction<C, C, C> sumFunction,
            @NonNull ArcReachabilityAlgo<V, A, C> algo) {
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
    public static <VV, AA> @NonNull SimpleReachabilityChecker<VV, AA, Integer> newIntCostInstance(
            @NonNull Function<VV, Iterable<Arc<VV, AA>>> nextArcsFunction,
            @NonNull TriFunction<VV, VV, AA, Integer> costFunction,
            @NonNull ArcReachabilityAlgo<VV, AA, Integer> algo) {
        return new SimpleReachabilityChecker<VV, AA, Integer>(0, nextArcsFunction, costFunction, Integer::sum, algo);
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
    public static <VV, AA> @NonNull SimpleReachabilityChecker<VV, AA, Integer> newIntCostInstance(
            @NonNull Function<VV, Iterable<Arc<VV, AA>>> nextArcsFunction,
            @NonNull ArcReachabilityAlgo<VV, AA, Integer> algo) {
        return new SimpleReachabilityChecker<VV, AA, Integer>(0, nextArcsFunction, (u, v, a) -> 1, Integer::sum, algo);
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
    public static <VV, AA> @NonNull SimpleReachabilityChecker<VV, AA, Long> newLongCostInstance(
            @NonNull Function<VV, Iterable<Arc<VV, AA>>> nextArcsFunction,
            @NonNull TriFunction<VV, VV, AA, Long> costFunction,
            @NonNull ArcReachabilityAlgo<VV, AA, Long> algo) {
        return new SimpleReachabilityChecker<VV, AA, Long>(0L, nextArcsFunction, costFunction, Long::sum, algo);
    }


    @Override
    public boolean isReachable(@NonNull Iterable<V> startVertices,
                               @NonNull Predicate<V> goalPredicate,
                               int maxDepth, @NonNull C costLimit, @NonNull AddToSet<V> visited) {
        return algo.tryToReach(
                startVertices, goalPredicate, nextArcsFunction, maxDepth, zero, costLimit,
                costFunction, sumFunction
        );
    }

    @Override
    public boolean isReachable(@NonNull V start,
                               @NonNull Predicate<V> goalPredicate, int maxDepth, @NonNull C costLimit, @NonNull AddToSet<V> visited) {
        return algo.tryToReach(
                Collections.singletonList(start), goalPredicate, nextArcsFunction, maxDepth, zero, costLimit,
                costFunction, sumFunction
        );
    }

    @Override
    public boolean isReachable(@NonNull V start, @NonNull V goal, int maxDepth, @NonNull C costLimit, @NonNull AddToSet<V> visited) {
        return algo.tryToReach(
                Collections.singletonList(start), goal::equals, nextArcsFunction, maxDepth, zero, costLimit, costFunction, sumFunction
        );
    }


}
