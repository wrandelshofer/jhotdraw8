/*
 * @(#)ArcSequenceFinder.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.path.algo;

import org.jhotdraw8.collection.pair.OrderedPair;
import org.jhotdraw8.collection.pair.SimpleOrderedPair;
import org.jhotdraw8.graph.Arc;
import org.jhotdraw8.graph.algo.AddToSet;
import org.jhotdraw8.icollection.PersistentVectorList;
import org.jhotdraw8.icollection.persistent.PersistentList;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

/// Interface for finding [Arc] sequences up to (inclusive)
/// a maximal cost in a directed graph.
///
/// @param <V> the vertex data type
/// @param <A> the arrow data type
public interface ArcSequenceFinder<V, A> {

    /// Finds an arc sequence from a set of start vertices to a vertex
    /// that satisfies the goal predicate.
    ///
    /// @param startVertices the start vertices
    /// @param goalPredicate the goal vertex
    /// @param maxDepth      the maximal depth (inclusive) of the search
    ///                      Must be `>= 0`.
    /// @param costLimit     the algorithm-specific search limit
    /// @param visited       the visited function
    /// @return an ordered pair (arc sequence, cost),
    /// or null if no sequence was found.
    @Nullable
    OrderedPair<PersistentList<Arc<V, A>>, Integer> findArcSequence(
            Iterable<V> startVertices,
            Predicate<V> goalPredicate,
            int maxDepth, int costLimit, AddToSet<V> visited);

    /// Finds an arc sequence from start to goal.
    ///
    /// @param start     the start vertex
    /// @param goal      the goal vertex
    /// @param maxDepth  the maximal depth (inclusive) of the search
    ///                  Must be `>= 0`.
    /// @param costLimit the algorithm-specific cost limit
    /// @param visited   the visited function
    /// @return an ordered pair (arc sequence, cost),
    /// or null if no sequence was found.
    default @Nullable OrderedPair<PersistentList<Arc<V, A>>, Integer> findArcSequence(
            V start,
            V goal,
            int maxDepth, int costLimit, AddToSet<V> visited) {
        return findArcSequence(PersistentVectorList.of(start), goal::equals, maxDepth, costLimit, visited);
    }


    /// Finds an arc sequence from start to goal.
    ///
    /// @param start     the start vertex
    /// @param goal      the goal vertex
    /// @param maxDepth  the maximal depth (inclusive) of the search
    ///                  Must be `>= 0`.
    /// @param costLimit the algorithm-specific cost limit
    /// @return an ordered pair (arc sequence, cost),
    /// or null if no sequence was found.
    default @Nullable OrderedPair<PersistentList<Arc<V, A>>, Integer> findArcSequence(
            V start,
            V goal,
            int maxDepth, int costLimit) {
        return findArcSequence(PersistentVectorList.of(start), goal::equals, maxDepth, costLimit, new HashSet<>()::add);
    }

    /// Finds an arc sequence from start to goal.
    ///
    /// @param start     the start vertex
    /// @param goal      the goal vertex
    /// @param costLimit the algorithm-specific cost limit
    /// @return an ordered pair (arc sequence, cost),
    /// or null if no sequence was found.
    default @Nullable OrderedPair<PersistentList<Arc<V, A>>, Integer> findArcSequence(
            V start,
            V goal,
            int costLimit) {
        return findArcSequence(PersistentVectorList.of(start), goal::equals, Integer.MAX_VALUE, costLimit, new HashSet<>()::add);
    }


    /// Finds an arc sequence through the given waypoints.
    ///
    /// @param waypoints         an iterable of waypoints
    /// @param maxDepth          the maximal depth (inclusive) of the search
    ///                          Must be `>= 0`.
    /// @param costLimit         the algorithm-specific cost limit for paths between waypoints
    /// @param visitedSetFactory the visited set factory
    /// @return an ordered pair (arc sequence, cost),
    /// or null if no sequence was found.
    @Nullable
    OrderedPair<PersistentList<Arc<V, A>>, Integer> findArcSequenceOverWaypoints(
            Iterable<V> waypoints,
            int maxDepth,
            int costLimit,
            Supplier<AddToSet<V>> visitedSetFactory);

    /// Finds an arc sequence through the given waypoints.
    ///
    /// @param waypoints an iterable of waypoints
    /// @param maxDepth  the maximal depth (inclusive) of the search
    ///                  Must be `>= 0`.
    /// @param costLimit the algorithm-specific cost limit for paths between waypoints
    /// @return an ordered pair (arc sequence, cost),
    /// or null if no sequence was found.
    default @Nullable OrderedPair<PersistentList<Arc<V, A>>, Integer> findArcSequenceOverWaypoints(
            Iterable<V> waypoints,
            int maxDepth,
            int costLimit) {
        return findArcSequenceOverWaypoints(waypoints, maxDepth, costLimit, () -> new HashSet<>()::add);
    }

    /// Finds an arc sequence through the given waypoints.
    ///
    /// @param waypoints an iterable of waypoints
    /// @param costLimit the algorithm-specific cost limit for paths between waypoints
    /// @return an ordered pair (arc sequence, cost),
    /// or null if no sequence was found.
    default @Nullable OrderedPair<PersistentList<Arc<V, A>>, Integer> findArcSequenceOverWaypoints(
            Iterable<V> waypoints,
            int costLimit) {
        return findArcSequenceOverWaypoints(waypoints, Integer.MAX_VALUE, costLimit, () -> new HashSet<>()::add);
    }


    /// Helper function for implementing [#findArcSequenceOverWaypoints(Iterable, int, int, Supplier)].
    ///
    /// @param <VV>                    the vertex type
    /// @param waypoints               the waypoints
    /// @param findArcSequenceFunction the search function, for example `this::findArrowSequence`
    /// @return an ordered pair with the combined sequence
    static <VV, AA> @Nullable OrderedPair<PersistentList<Arc<VV, AA>>, Integer>
    findArcSequenceOverWaypoints(
            Iterable<VV> waypoints,
            BiFunction<VV, VV, OrderedPair<PersistentList<Arc<VV, AA>>, Integer>> findArcSequenceFunction) {
        List<Arc<VV, AA>> sequence = new ArrayList<>();
        int sum = 0;
        VV prev = null;
        int count = 0;
        for (VV next : waypoints) {
            if (prev != null) {
                final OrderedPair<PersistentList<Arc<VV, AA>>, Integer> result = findArcSequenceFunction.apply(prev, next);
                if (result == null) {
                    return null;
                } else {
                    final List<Arc<VV, AA>> nextSequence = result.first().asList();
                    sequence.addAll(nextSequence);
                    sum = (sum + result.second());
                }
            }
            prev = next;
            count++;
        }
        if (count == 1) {
            // the set of waypoints is degenerate
            return new SimpleOrderedPair<>(PersistentVectorList.of(), 0);
        }

        return new SimpleOrderedPair<>(PersistentVectorList.copyOf(sequence), sum);
    }
}
