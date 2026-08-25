/*
 * @(#)SimpleVertexSequenceFinder.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.path.algo;

import org.jhotdraw8.collection.pair.SimpleOrderedPair;
import org.jhotdraw8.graph.algo.AddToSet;
import org.jhotdraw8.graph.path.backlink.VertexBackLinkWithCost;
import org.jhotdraw8.icollection.persistent.PersistentList;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntBiFunction;

/// Implements the [VertexSequenceFinder] interface.
///
/// @param <V> the vertex data type
/// @param <C> the cost number type
public class SimpleVertexSequenceFinder<V> implements VertexSequenceFinder<V> {

    private final Function<V, Iterable<V>> nextVerticesFunction;
    private final ToIntBiFunction<V, V> costFunction;
    private final VertexPathSearchAlgo<V> algo;

    /// Creates a new instance.
    ///
    /// @param nextVerticesFunction a function that given a vertex,
    ///                             returns an [Iterable] for the next vertices
    ///                             of that vertex.
    /// @param costFunction         the cost function
    /// @param algo                 the search algorithm
    public SimpleVertexSequenceFinder(
            Function<V, Iterable<V>> nextVerticesFunction,
            ToIntBiFunction<V, V> costFunction,
            VertexPathSearchAlgo<V> algo) {

        this.nextVerticesFunction = nextVerticesFunction;
        this.costFunction = costFunction;
        this.algo = algo;
    }

    /// Creates a new instance with a cost of 1 per arrow.
    ///
    /// @param nextVerticesFunction a function that given a vertex,
    ///                             returns an [Iterable] for the next vertices
    ///                             of that vertex.
    /// @param costFunction         the cost function
    /// @param algo                 the search algorithm
    public SimpleVertexSequenceFinder(
            Function<V, Iterable<V>> nextVerticesFunction,
            VertexPathSearchAlgo<V> algo) {

        this(nextVerticesFunction, (u, v) -> 1, algo);
    }


    @Override
    public @Nullable SimpleOrderedPair<PersistentList<V>, Integer> findVertexSequence(Iterable<V> startVertices, Predicate<V> goalPredicate, int maxDepth, int costLimit, AddToSet<V> visited) {
        return VertexBackLinkWithCost.toVertexSequence(algo.search(
                startVertices, goalPredicate, nextVerticesFunction, maxDepth, costLimit, costFunction,
                visited), VertexBackLinkWithCost::getVertex);
    }

    @Override
    public @Nullable SimpleOrderedPair<PersistentList<V>, Integer> findVertexSequenceOverWaypoints(Iterable<V> waypoints, int maxDepth, int costLimit, Supplier<AddToSet<V>> visitedSetFactory) {
        return VertexSequenceFinder.findVertexSequenceOverWaypoints(
                waypoints,
                (start, goal) -> this.findVertexSequence(start, goal, maxDepth, costLimit, visitedSetFactory.get())

        );
    }


}
