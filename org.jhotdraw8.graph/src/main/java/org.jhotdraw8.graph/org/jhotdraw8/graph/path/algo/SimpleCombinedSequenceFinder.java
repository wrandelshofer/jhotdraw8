/*
 * @(#)SimpleCombinedSequenceFinder.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.path.algo;

import org.jhotdraw8.base.function.ToIntFunction3;
import org.jhotdraw8.collection.pair.OrderedPair;
import org.jhotdraw8.collection.pair.SimpleOrderedPair;
import org.jhotdraw8.graph.Arc;
import org.jhotdraw8.graph.algo.AddToSet;
import org.jhotdraw8.graph.path.backlink.ArcBackLinkWithCost;
import org.jhotdraw8.icollection.persistent.PersistentList;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/// Implements the [CombinedSequenceFinder] interface.
///
/// @param <V> the vertex data type
/// @param <A> the arrow data type
public class SimpleCombinedSequenceFinder<V, A> implements CombinedSequenceFinder<V, A> {


    private final Function<V, Iterable<Arc<V, A>>> nextArcsFunction;
    private final ToIntFunction3<V, V, A> costFunction;
    private final ArcPathSearchAlgo<V, A> algo;


    /// Creates a new instance.
    ///
    /// @param nextArcsFunction a function that given a vertex,
    ///                         returns an [Iterable] for the [Arc]s
    ///                         starting at that vertex.
    /// @param costFunction     the cost function
    /// @param algo             the search algorithm
    public SimpleCombinedSequenceFinder(
            Function<V, Iterable<Arc<V, A>>> nextArcsFunction,
            ToIntFunction3<V, V, A> costFunction,
            ArcPathSearchAlgo<V, A> algo) {
        this.nextArcsFunction = nextArcsFunction;
        this.costFunction = costFunction;
        this.algo = algo;
    }

    /// Creates a new instance with a cost of 1 per arrow.
    ///
    /// @param nextArcsFunction a function that given a vertex,
    ///                         returns an [Iterable] for the [Arc]s
    ///                         starting at that vertex.
    /// @param algo             the search algorithm
    public SimpleCombinedSequenceFinder(
            Function<V, Iterable<Arc<V, A>>> nextArcsFunction,
            ArcPathSearchAlgo<V, A> algo) {
        this(nextArcsFunction, (u, v, a) -> 1, algo);
    }


    @Override
    public @Nullable OrderedPair<PersistentList<Arc<V, A>>, Integer> findArcSequence(Iterable<V> startVertices, Predicate<V> goalPredicate, int maxDepth, int costLimit, AddToSet<V> visited) {
        return ArcBackLinkWithCost.toArrowSequence(algo.search(
                startVertices, goalPredicate, nextArcsFunction, maxDepth, costLimit, costFunction,
                visited), (a, b) -> new Arc<>(a.getVertex(), b.getVertex(), b.getArrow()));
    }

    @Override
    public @Nullable OrderedPair<PersistentList<Arc<V, A>>, Integer> findArcSequenceOverWaypoints(Iterable<V> waypoints, int maxDepth, int costLimit, Supplier<AddToSet<V>> visitedSetFactory) {
        return ArcSequenceFinder.<V, A>findArcSequenceOverWaypoints(waypoints, (start, goal) -> findArcSequence(start, goal, maxDepth, costLimit, visitedSetFactory.get()));
    }

    @Override
    public @Nullable SimpleOrderedPair<PersistentList<A>, Integer> findArrowSequence(Iterable<V> startVertices, Predicate<V> goalPredicate, int maxDepth, int costLimit, AddToSet<V> visited) {
        return ArcBackLinkWithCost.toArrowSequence(algo.search(
                startVertices, goalPredicate, nextArcsFunction, maxDepth, costLimit, costFunction,
                visited), (a, b) -> b.getArrow());
    }

    @Override
    public @Nullable SimpleOrderedPair<PersistentList<A>, Integer> findArrowSequenceOverWaypoints(Iterable<V> waypoints, int maxDepth, int costLimit, Supplier<AddToSet<V>> visitedSetFactory) {
        return ArrowSequenceFinder.<V, A>findArrowSequenceOverWaypoints(waypoints, (start, goal) -> findArrowSequence(start, goal, maxDepth, costLimit, visitedSetFactory.get()));
    }

    @Override
    public @Nullable SimpleOrderedPair<PersistentList<V>, Integer> findVertexSequence(Iterable<V> startVertices, Predicate<V> goalPredicate, int maxDepth, int costLimit, AddToSet<V> visited) {
        return ArcBackLinkWithCost.toVertexSequence(algo.search(
                startVertices, goalPredicate, nextArcsFunction, maxDepth, costLimit, costFunction,
                visited), ArcBackLinkWithCost::getVertex);
    }

    @Override
    public @Nullable SimpleOrderedPair<PersistentList<V>, Integer> findVertexSequenceOverWaypoints(Iterable<V> waypoints, int maxDepth, int costLimit, Supplier<AddToSet<V>> visitedSetFactory) {
        return VertexSequenceFinder.findVertexSequenceOverWaypoints(waypoints, (start, goal) -> findVertexSequence(start, goal, maxDepth, costLimit, visitedSetFactory.get()));
    }


}
