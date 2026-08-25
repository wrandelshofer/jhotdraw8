/*
 * @(#)SimpleCombinedAllSequencesFinder.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.path.algo;

import org.jhotdraw8.base.function.ToIntFunction3;
import org.jhotdraw8.collection.pair.OrderedPair;
import org.jhotdraw8.collection.spliterator.SpliteratorIterable;
import org.jhotdraw8.graph.Arc;
import org.jhotdraw8.graph.path.backlink.ArcBackLinkWithCost;
import org.jhotdraw8.icollection.persistent.PersistentList;

import java.util.function.Function;
import java.util.function.Predicate;

/// Implements the [CombinedAllSequencesFinder] interface.
///
/// See [AllWalksSpliterator] for a description of the underlying algorithm.
///
/// @param <V> the vertex data type
/// @param <A> the arrow data type
public class SimpleCombinedAllSequencesFinder<V, A> implements CombinedAllSequencesFinder<V, A> {
    private final Function<V, Iterable<Arc<V, A>>> nextArcsFunction;
    private final ToIntFunction3<V, V, A> costFunction;

    /// Creates a new instance.
    ///
    /// @param nextArcsFunction the next arcs function
    /// @param costFunction     the cost function
    public SimpleCombinedAllSequencesFinder(Function<V, Iterable<Arc<V, A>>> nextArcsFunction,
                                            ToIntFunction3<V, V, A> costFunction) {
        this.nextArcsFunction = nextArcsFunction;
        this.costFunction = costFunction;
    }

    /// Creates a new instance with a cost function of 1 per arrow.
    ///
    /// @param nextArcsFunction the next arcs function
    public SimpleCombinedAllSequencesFinder(Function<V, Iterable<Arc<V, A>>> nextArcsFunction) {
        this(nextArcsFunction, (u, v, a) -> 1);
    }


    @Override
    public Iterable<OrderedPair<PersistentList<Arc<V, A>>, Integer>> findAllArcSequences(
            Iterable<V> startVertices,
            Predicate<V> goalPredicate,
            int maxDepth,
            int costLimit) {

        return new SpliteratorIterable<>(() -> new AllWalksSpliterator<>(
                startVertices, goalPredicate, nextArcsFunction,
                (backLink) -> ArcBackLinkWithCost.toArrowSequence(backLink, (a, b) -> new Arc<>(a.getVertex(), b.getVertex(), b.getArrow())),
                maxDepth, costLimit, costFunction));
    }


    @Override
    public Iterable<OrderedPair<PersistentList<A>, Integer>> findAllArrowSequences(
            Iterable<V> startVertices,
            Predicate<V> goalPredicate,
            int maxDepth,
            int costLimit) {

        return new SpliteratorIterable<>(() -> new AllWalksSpliterator<>(
                startVertices, goalPredicate, nextArcsFunction,
                (backLink) -> ArcBackLinkWithCost.toArrowSequence(backLink, (a, b) -> b.getArrow()),
                maxDepth, costLimit, costFunction));
    }


    @Override
    public Iterable<OrderedPair<PersistentList<V>, Integer>> findAllVertexSequences(
            Iterable<V> startVertices,
            Predicate<V> goalPredicate,
            int maxDepth,
            int costLimit) {

        return new SpliteratorIterable<>(() -> new AllWalksSpliterator<>(
                startVertices, goalPredicate, nextArcsFunction,
                (backLink) -> ArcBackLinkWithCost.toVertexSequence(backLink, ArcBackLinkWithCost::getVertex),
                maxDepth, costLimit, costFunction));
    }


}
