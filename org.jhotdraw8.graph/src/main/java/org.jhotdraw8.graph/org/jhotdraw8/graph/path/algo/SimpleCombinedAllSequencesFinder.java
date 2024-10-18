/*
 * @(#)SimpleCombinedAllSequencesFinder.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.path.algo;

import org.jhotdraw8.base.function.Function3;
import org.jhotdraw8.collection.pair.OrderedPair;
import org.jhotdraw8.collection.spliterator.SpliteratorIterable;
import org.jhotdraw8.graph.Arc;
import org.jhotdraw8.graph.path.backlink.ArcBackLinkWithCost;
import org.jhotdraw8.icollection.persistent.PersistentList;

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
    private final C zero;
    private final Function<V, Iterable<Arc<V, A>>> nextArcsFunction;
    private final Function3<V, V, A, C> costFunction;
    private final BiFunction<C, C, C> sumFunction;

    /**
     * Creates a new instance.
     *
     * @param nextArcsFunction the next arcs function
     * @param zero             the zero cost value
     * @param costFunction     the cost function
     * @param sumFunction      the sum function
     */
    public SimpleCombinedAllSequencesFinder(Function<V, Iterable<Arc<V, A>>> nextArcsFunction,
                                            C zero,
                                            Function3<V, V, A, C> costFunction,
                                            BiFunction<C, C, C> sumFunction) {
        this.zero = zero;
        this.nextArcsFunction = nextArcsFunction;
        this.costFunction = costFunction;
        this.sumFunction = sumFunction;
    }


    @Override
    public Iterable<OrderedPair<PersistentList<Arc<V, A>>, C>> findAllArcSequences(
            Iterable<V> startVertices,
            Predicate<V> goalPredicate,
            int maxDepth,
            C costLimit) {

        return new SpliteratorIterable<>(() -> new AllWalksSpliterator<>(
                startVertices, goalPredicate, nextArcsFunction,
                (backLink) -> ArcBackLinkWithCost.toArrowSequence(backLink, (a, b) -> new Arc<>(a.getVertex(), b.getVertex(), b.getArrow())),
                maxDepth, costLimit, zero, costFunction, sumFunction));
    }


    @Override
    public Iterable<OrderedPair<PersistentList<A>, C>> findAllArrowSequences(
            Iterable<V> startVertices,
            Predicate<V> goalPredicate,
            int maxDepth,
            C costLimit) {

        return new SpliteratorIterable<>(() -> new AllWalksSpliterator<>(
                startVertices, goalPredicate, nextArcsFunction,
                (backLink) -> ArcBackLinkWithCost.toArrowSequence(backLink, (a, b) -> b.getArrow()),
                maxDepth, costLimit, zero, costFunction, sumFunction));
    }


    @Override
    public Iterable<OrderedPair<PersistentList<V>, C>> findAllVertexSequences(
            Iterable<V> startVertices,
            Predicate<V> goalPredicate,
            int maxDepth,
            C costLimit) {

        return new SpliteratorIterable<>(() -> new AllWalksSpliterator<>(
                startVertices, goalPredicate, nextArcsFunction,
                (backLink) -> ArcBackLinkWithCost.toVertexSequence(backLink, ArcBackLinkWithCost::getVertex),
                maxDepth, costLimit, zero, costFunction, sumFunction));
    }


}
