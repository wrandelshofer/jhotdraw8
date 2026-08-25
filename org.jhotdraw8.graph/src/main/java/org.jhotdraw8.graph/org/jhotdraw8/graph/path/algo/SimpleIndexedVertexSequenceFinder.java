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
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntBiFunction;

/// Implements the [VertexSequenceFinder] interface.
///
/// @param <C> the cost number type
public class SimpleIndexedVertexSequenceFinder implements VertexSequenceFinder<Integer> {
    private final IndexedVertexPathSearchAlgo algo;

    private final Function<Integer, Spliterator.OfInt> nextVerticesFunction;
    private final ToIntBiFunction<Integer, Integer> costFunction;


    /// Creates a new instance.
    ///
    /// @param nextVerticesFunction a function that given a vertex,
    ///                             returns an [Iterable] for the next vertices
    ///                             of that vertex.
    /// @param costFunction         the cost function
    public SimpleIndexedVertexSequenceFinder(
            Function<Integer, Spliterator.OfInt> nextVerticesFunction,
            ToIntBiFunction<Integer, Integer> costFunction,
            IndexedVertexPathSearchAlgo algo) {
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
    public SimpleIndexedVertexSequenceFinder(
            Function<Integer, Spliterator.OfInt> nextVerticesFunction,
            IndexedVertexPathSearchAlgo algo) {
        this(nextVerticesFunction, (u, v) -> 1, algo);
    }


    @Override
    public @Nullable SimpleOrderedPair<PersistentList<Integer>, Integer> findVertexSequence(
            Iterable<Integer> startVertices, Predicate<Integer> goalPredicate,
            int maxDepth, int costLimit, AddToSet<Integer> visited) {
        AddToIntSet visitedAsInt = visited instanceof AddToIntSet ? (AddToIntSet) visited : visited::add;
        return IndexedVertexBackLinkWithCost.toVertexSequence(algo.search(
                startVertices, goalPredicate::test, nextVerticesFunction, maxDepth, costLimit, costFunction,
                visitedAsInt), IndexedVertexBackLinkWithCost::getVertex);
    }

    @Override
    public @Nullable SimpleOrderedPair<PersistentList<Integer>, Integer> findVertexSequenceOverWaypoints(
            Iterable<Integer> waypoints, int maxDepth, int costLimit, Supplier<AddToSet<Integer>> visitedSetFactory) {
        return VertexSequenceFinder.findVertexSequenceOverWaypoints(
                waypoints,
                (start, goal) -> this.findVertexSequence(start, goal, maxDepth, costLimit, visitedSetFactory.get())

        );
    }


}
