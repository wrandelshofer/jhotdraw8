/*
 * @(#)ArcSequenceFinder.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.path;

import org.jhotdraw8.collection.pair.OrderedPair;
import org.jhotdraw8.collection.pair.SimpleOrderedPair;
import org.jhotdraw8.graph.Arc;
import org.jhotdraw8.graph.algo.AddToSet;
import org.jhotdraw8.icollection.VectorList;
import org.jhotdraw8.icollection.immutable.ImmutableList;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Interface for finding {@link Arc} sequences up to (inclusive)
 * a maximal cost in a directed graph.
 *
 * @param <V> the vertex data type
 * @param <A> the arrow data type
 * @param <C> the cost number type
 */
public interface ArcSequenceFinder<V, A, C extends Number & Comparable<C>> {

    /**
     * Finds an arc sequence from a set of start vertices to a vertex
     * that satisfies the goal predicate.
     *
     * @param startVertices the start vertices
     * @param goalPredicate the goal vertex
     * @param maxDepth      the maximal depth (inclusive) of the search
     *                      Must be {@literal >= 0}.
     * @param costLimit     the algorithm-specific search limit
     * @param visited       the visited function
     * @return an ordered pair (arc sequence, cost),
     * or null if no sequence was found.
     */
    @Nullable OrderedPair<ImmutableList<Arc<V, A>>, C> findArcSequence(
            Iterable<V> startVertices,
            Predicate<V> goalPredicate,
            int maxDepth, C costLimit, AddToSet<V> visited);

    /**
     * Finds an arc sequence from start to goal.
     *
     * @param start     the start vertex
     * @param goal      the goal vertex
     * @param maxDepth  the maximal depth (inclusive) of the search
     *                  Must be {@literal >= 0}.
     * @param costLimit the algorithm-specific cost limit
     * @param visited   the visited function
     * @return an ordered pair (arc sequence, cost),
     * or null if no sequence was found.
     */
    default @Nullable OrderedPair<ImmutableList<Arc<V, A>>, C> findArcSequence(
            V start,
            V goal,
            int maxDepth, C costLimit, AddToSet<V> visited) {
        return findArcSequence(VectorList.of(start), goal::equals, maxDepth, costLimit, visited);
    }


    /**
     * Finds an arc sequence from start to goal.
     *
     * @param start     the start vertex
     * @param goal      the goal vertex
     * @param maxDepth  the maximal depth (inclusive) of the search
     *                  Must be {@literal >= 0}.
     * @param costLimit the algorithm-specific cost limit
     * @return an ordered pair (arc sequence, cost),
     * or null if no sequence was found.
     */
    default @Nullable OrderedPair<ImmutableList<Arc<V, A>>, C> findArcSequence(
            V start,
            V goal,
            int maxDepth, C costLimit) {
        return findArcSequence(VectorList.of(start), goal::equals, maxDepth, costLimit, new HashSet<>()::add);
    }

    /**
     * Finds an arc sequence from start to goal.
     *
     * @param start     the start vertex
     * @param goal      the goal vertex
     * @param costLimit the algorithm-specific cost limit
     * @return an ordered pair (arc sequence, cost),
     * or null if no sequence was found.
     */
    default @Nullable OrderedPair<ImmutableList<Arc<V, A>>, C> findArcSequence(
            V start,
            V goal,
            C costLimit) {
        return findArcSequence(VectorList.of(start), goal::equals, Integer.MAX_VALUE, costLimit, new HashSet<>()::add);
    }


    /**
     * Finds an arc sequence through the given waypoints.
     *
     * @param waypoints         an iterable of waypoints
     * @param maxDepth          the maximal depth (inclusive) of the search
     *                          Must be {@literal >= 0}.
     * @param costLimit         the algorithm-specific cost limit for paths between waypoints
     * @param visitedSetFactory the visited set factory
     * @return an ordered pair (arc sequence, cost),
     * or null if no sequence was found.
     */
    @Nullable OrderedPair<ImmutableList<Arc<V, A>>, C> findArcSequenceOverWaypoints(
            Iterable<V> waypoints,
            int maxDepth,
            C costLimit,
            Supplier<AddToSet<V>> visitedSetFactory);

    /**
     * Finds an arc sequence through the given waypoints.
     *
     * @param waypoints an iterable of waypoints
     * @param maxDepth  the maximal depth (inclusive) of the search
     *                  Must be {@literal >= 0}.
     * @param costLimit the algorithm-specific cost limit for paths between waypoints
     * @return an ordered pair (arc sequence, cost),
     * or null if no sequence was found.
     */
    default @Nullable OrderedPair<ImmutableList<Arc<V, A>>, C> findArcSequenceOverWaypoints(
            Iterable<V> waypoints,
            int maxDepth,
            C costLimit) {
        return findArcSequenceOverWaypoints(waypoints, maxDepth, costLimit, () -> new HashSet<>()::add);
    }

    /**
     * Finds an arc sequence through the given waypoints.
     *
     * @param waypoints an iterable of waypoints
     * @param costLimit the algorithm-specific cost limit for paths between waypoints
     * @return an ordered pair (arc sequence, cost),
     * or null if no sequence was found.
     */
    default @Nullable OrderedPair<ImmutableList<Arc<V, A>>, C> findArcSequenceOverWaypoints(
            Iterable<V> waypoints,
            C costLimit) {
        return findArcSequenceOverWaypoints(waypoints, Integer.MAX_VALUE, costLimit, () -> new HashSet<>()::add);
    }


    /**
     * Helper function for implementing {@link #findArcSequenceOverWaypoints(Iterable, int, Number, Supplier)}.
     *
     * @param <VV>                    the vertex type
     * @param <CC>                    the number type
     * @param waypoints               the waypoints
     * @param findArcSequenceFunction the search function, for example {@code this::findArrowSequence}
     * @param zero                    the zero value
     * @param sumFunction             the sum function
     * @return an ordered pair with the combined sequence
     */
    static <VV, AA, CC extends Number & Comparable<CC>> @Nullable OrderedPair<ImmutableList<Arc<VV, AA>>, CC>
    findArcSequenceOverWaypoints(
            Iterable<VV> waypoints,
            BiFunction<VV, VV, OrderedPair<ImmutableList<Arc<VV, AA>>, CC>> findArcSequenceFunction,
            CC zero,
            BiFunction<CC, CC, CC> sumFunction) {
        List<Arc<VV, AA>> sequence = new ArrayList<>();
        CC sum = zero;
        VV prev = null;
        int count = 0;
        for (VV next : waypoints) {
            if (prev != null) {
                final OrderedPair<ImmutableList<Arc<VV, AA>>, CC> result = findArcSequenceFunction.apply(prev, next);
                if (result == null) {
                    return null;
                } else {
                    final List<Arc<VV, AA>> nextSequence = result.first().asList();
                    sequence.addAll(nextSequence);
                    sum = sumFunction.apply(sum, result.second());
                }
            }
            prev = next;
            count++;
        }
        if (count == 1) {
            // the set of waypoints is degenerate
            return new SimpleOrderedPair<>(VectorList.of(), zero);
        }

        return new SimpleOrderedPair<>(VectorList.copyOf(sequence), sum);
    }
}
