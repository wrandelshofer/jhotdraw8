/*
 * @(#)SimpleCombinedAllSequencesFinder.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.path;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.base.function.TriFunction;
import org.jhotdraw8.collection.immutable.ImmutableList;
import org.jhotdraw8.collection.pair.OrderedPair;
import org.jhotdraw8.collection.spliterator.SpliteratorIterable;
import org.jhotdraw8.graph.Arc;
import org.jhotdraw8.graph.path.algo.AllWalksSpliterator;
import org.jhotdraw8.graph.path.backlink.ArcBackLinkWithCost;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Implements the {@link CombinedAllSequencesFinder} interface.
 * <p>
 * See {@link AllWalksSpliterator} for a description of the underlying algorithm.
 *
 * @param <V> the vertex data type
 * @param <A> the arrow data type
 * @param <C> the cost number type
 */
public class SimpleCombinedAllSequencesFinder<V, A, C extends Number & Comparable<C>> implements CombinedAllSequencesFinder<V, A, C> {
    private final @NonNull C zero;
    private final @NonNull Function<V, Iterable<Arc<V, A>>> nextArcsFunction;
    private final @NonNull TriFunction<V, V, A, C> costFunction;
    private final @NonNull BiFunction<C, C, C> sumFunction;

    /**
     * Creates a new instance.
     *
     * @param nextArcsFunction the next arcs function
     * @param zero             the zero cost value
     * @param costFunction     the cost function
     * @param sumFunction      the sum function
     */
    public SimpleCombinedAllSequencesFinder(@NonNull Function<V, Iterable<Arc<V, A>>> nextArcsFunction,
                                            @NonNull C zero,
                                            @NonNull TriFunction<V, V, A, C> costFunction,
                                            @NonNull BiFunction<C, C, C> sumFunction) {
        this.zero = zero;
        this.nextArcsFunction = nextArcsFunction;
        this.costFunction = costFunction;
        this.sumFunction = sumFunction;
    }


    @Override
    public @NonNull Iterable<OrderedPair<ImmutableList<Arc<V, A>>, C>> findAllArcSequences(
            @NonNull Iterable<V> startVertices,
            @NonNull Predicate<V> goalPredicate,
            int maxDepth,
            @NonNull C costLimit) {

        return new SpliteratorIterable<>(() -> new AllWalksSpliterator<>(
                startVertices, goalPredicate, nextArcsFunction,
                (backLink) -> ArcBackLinkWithCost.toArrowSequence(backLink, (a, b) -> new Arc<>(a.getVertex(), b.getVertex(), b.getArrow())),
                maxDepth, costLimit, zero, costFunction, sumFunction));
    }


    @Override
    public @NonNull Iterable<OrderedPair<ImmutableList<A>, C>> findAllArrowSequences(
            @NonNull Iterable<V> startVertices,
            @NonNull Predicate<V> goalPredicate,
            int maxDepth,
            @NonNull C costLimit) {

        return new SpliteratorIterable<>(() -> new AllWalksSpliterator<>(
                startVertices, goalPredicate, nextArcsFunction,
                (backLink) -> ArcBackLinkWithCost.toArrowSequence(backLink, (a, b) -> b.getArrow()),
                maxDepth, costLimit, zero, costFunction, sumFunction));
    }


    @Override
    public @NonNull Iterable<OrderedPair<ImmutableList<V>, C>> findAllVertexSequences(
            @NonNull Iterable<V> startVertices,
            @NonNull Predicate<V> goalPredicate,
            int maxDepth,
            @NonNull C costLimit) {

        return new SpliteratorIterable<>(() -> new AllWalksSpliterator<>(
                startVertices, goalPredicate, nextArcsFunction,
                (backLink) -> ArcBackLinkWithCost.toVertexSequence(backLink, ArcBackLinkWithCost::getVertex),
                maxDepth, costLimit, zero, costFunction, sumFunction));
    }


}
