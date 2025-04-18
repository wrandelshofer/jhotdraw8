/*
 * @(#)VertexSequenceFinder.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.path.algo;

import org.jhotdraw8.collection.pair.OrderedPair;
import org.jhotdraw8.collection.pair.SimpleOrderedPair;
import org.jhotdraw8.graph.algo.AddToSet;
import org.jhotdraw8.icollection.VectorList;
import org.jhotdraw8.icollection.persistent.PersistentList;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Interface for finding vertex sequences up to (inclusive) a maximal cost
 * in a directed graph.
 *
 * @param <V> the vertex data type
 * @param <C> the cost number type
 */
public interface VertexSequenceFinder<V, C extends Number & Comparable<C>> {


    /**
     * Finds a vertex sequence from a set of start vertices to a vertex
     * that satisfies the goal predicate.
     *
     * @param startVertices the start vertices
     * @param goalPredicate the goal vertex
     * @param maxDepth      the maximal depth (inclusive) of the search
     *                      Must be {@literal >= 0}.
     * @param costLimit     the algorithm-specific cost limit
     * @param visited       the visited set
     * @return an ordered pair (vertex sequence, cost),
     * or null if no sequence was found.
     */
    @Nullable
    SimpleOrderedPair<PersistentList<V>, C> findVertexSequence(
            Iterable<V> startVertices,
            Predicate<V> goalPredicate,
            int maxDepth,
            C costLimit,
            AddToSet<V> visited);

    /**
     * Finds a vertex sequence from a set of start vertices to a vertex
     * that satisfies the goal predicate.
     *
     * @param startVertices the start vertices
     * @param goalPredicate the goal vertex
     * @param maxDepth      the maximal depth (inclusive) of the search
     *                      Must be {@literal >= 0}.
     * @param costLimit     the algorithm-specific cost limit
     * @return an ordered pair (vertex sequence, cost),
     * or null if no sequence was found.
     */
    default @Nullable OrderedPair<PersistentList<V>, C> findVertexSequence(
            Iterable<V> startVertices,
            Predicate<V> goalPredicate,
            int maxDepth,
            C costLimit) {
        return findVertexSequence(startVertices, goalPredicate, maxDepth, costLimit, new HashSet<>()::add);
    }

    /**
     * Finds a vertex sequence from a set of start vertices to a vertex
     * that satisfies the goal predicate.
     *
     * @param startVertices the start vertices
     * @param goalPredicate the goal vertex
     * @param costLimit     the algorithm-specific cost limit
     * @return an ordered pair (vertex sequence, cost),
     * or null if no sequence was found.
     */
    default @Nullable SimpleOrderedPair<PersistentList<V>, C> findVertexSequence(
            Iterable<V> startVertices,
            Predicate<V> goalPredicate,
            C costLimit) {
        return findVertexSequence(startVertices, goalPredicate, Integer.MAX_VALUE, costLimit, new HashSet<>()::add);
    }

    /**
     * Finds a vertex sequence from a start vertex to a vertex
     * that satisfies the goal predicate.
     *
     * @param start         the start vertex
     * @param goalPredicate the goal vertex
     * @param maxDepth      the maximal depth (inclusive) of the search
     *                      Must be {@literal >= 0}.
     * @param costLimit     the algorithm-specific cost limit
     * @param visited       the visited function
     * @return an ordered pair (vertex sequence, cost),
     * or null if no sequence was found.
     */
    default @Nullable OrderedPair<PersistentList<V>, C> findVertexSequence(
            V start,
            Predicate<V> goalPredicate,
            int maxDepth,
            C costLimit, AddToSet<V> visited) {
        return findVertexSequence(Collections.singletonList(start), goalPredicate, maxDepth, costLimit, visited);
    }

    /**
     * Finds a vertex sequence from a start vertex to a vertex
     * that satisfies the goal predicate.
     *
     * @param start         the start vertex
     * @param goalPredicate the goal vertex
     * @param maxDepth      the maximal depth (inclusive) of the search
     *                      Must be {@literal >= 0}.
     * @param costLimit     the algorithm-specific cost limit
     * @return an ordered pair (vertex sequence, cost),
     * or null if no sequence was found.
     */
    default @Nullable OrderedPair<PersistentList<V>, C> findVertexSequence(
            V start,
            Predicate<V> goalPredicate,
            int maxDepth,
            C costLimit) {
        return findVertexSequence(Collections.singletonList(start), goalPredicate, maxDepth, costLimit, new HashSet<>()::add);
    }

    /**
     * Finds a vertex sequence from a start vertex to a vertex
     * that satisfies the goal predicate.
     *
     * @param start         the start vertex
     * @param goalPredicate the goal vertex
     * @param costLimit     the algorithm-specific cost limit
     * @return an ordered pair (vertex sequence, cost),
     * or null if no sequence was found.
     */
    default @Nullable SimpleOrderedPair<PersistentList<V>, C> findVertexSequence(
            V start,
            Predicate<V> goalPredicate,
            C costLimit) {
        return findVertexSequence(Collections.singletonList(start), goalPredicate, Integer.MAX_VALUE, costLimit, new HashSet<>()::add);
    }

    /**
     * Finds a vertex sequence from start to goal.
     *
     * @param start     the start vertex
     * @param goal      the goal vertex
     * @param maxDepth  the maximal depth (inclusive) of the search
     *                  Must be {@literal >= 0}.
     * @param costLimit the algorithm-specific cost limit
     * @param visited   the visited function
     * @return an ordered pair (vertex sequence, cost),
     * or null if no sequence was found.
     */
    @SuppressWarnings("unchecked")
    default @Nullable SimpleOrderedPair<PersistentList<V>, C> findVertexSequence(
            V start,
            V goal,
            int maxDepth,
            C costLimit,
            AddToSet<V> visited) {
        return findVertexSequence(VectorList.of(start), goal::equals, maxDepth, costLimit, visited);
    }

    /**
     * Finds a vertex sequence from start to goal.
     *
     * @param start     the start vertex
     * @param goal      the goal vertex
     * @param maxDepth  the maximal depth (inclusive) of the search
     *                  Must be {@literal >= 0}.
     * @param costLimit the algorithm-specific cost limit
     * @return an ordered pair (vertex sequence, cost),
     * or null if no sequence was found.
     */
    @SuppressWarnings("unchecked")
    default @Nullable SimpleOrderedPair<PersistentList<V>, C> findVertexSequence(
            V start,
            V goal,
            int maxDepth,
            C costLimit) {
        return findVertexSequence(VectorList.of(start), goal::equals, maxDepth, costLimit, new HashSet<>()::add);
    }

    /**
     * Finds a vertex sequence from start to goal.
     *
     * @param start     the start vertex
     * @param goal      the goal vertex
     * @param costLimit the algorithm-specific cost limit
     * @return an ordered pair (vertex sequence, cost),
     * or null if no sequence was found.
     */
    @SuppressWarnings("unchecked")
    default @Nullable SimpleOrderedPair<PersistentList<V>, C> findVertexSequence(
            V start,
            V goal,
            C costLimit) {
        return findVertexSequence(VectorList.of(start), goal::equals, Integer.MAX_VALUE, costLimit, new HashSet<>()::add);
    }

    /**
     * Finds a vertex sequence through the given waypoints.
     *
     * @param waypoints         a list of waypoints
     * @param maxDepth          the maximal depth (inclusive) of the search
     *                          Must be {@literal >= 0}.
     * @param costLimit         the algorithm-specific cost limit for paths between waypoints
     * @param visitedSetFactory the visited set factory
     * @return an ordered pair (vertex sequence, cost),
     * or null if no sequence was found.
     */
    @Nullable
    SimpleOrderedPair<PersistentList<V>, C> findVertexSequenceOverWaypoints(
            Iterable<V> waypoints,
            int maxDepth,
            C costLimit,
            Supplier<AddToSet<V>> visitedSetFactory);


    /**
     * Finds a vertex sequence through the given waypoints.
     *
     * @param waypoints a list of waypoints
     * @param maxDepth  the maximal depth (inclusive) of the search
     *                  Must be {@literal >= 0}.
     * @param costLimit the algorithm-specific cost limit for paths between waypoints
     * @return an ordered pair (vertex sequence, cost),
     * or null if no sequence was found.
     */
    default @Nullable SimpleOrderedPair<PersistentList<V>, C> findVertexSequenceOverWaypoints(
            Iterable<V> waypoints,
            int maxDepth,
            C costLimit) {
        return findVertexSequenceOverWaypoints(waypoints, maxDepth, costLimit, () -> new HashSet<>()::add);
    }

    /**
     * Finds a vertex sequence through the given waypoints.
     *
     * @param waypoints a list of waypoints
     * @param costLimit the algorithm-specific cost limit for paths between waypoints
     * @return an ordered pair (vertex sequence, cost),
     * or null if no sequence was found.
     */
    default @Nullable SimpleOrderedPair<PersistentList<V>, C> findVertexSequenceOverWaypoints(
            Iterable<V> waypoints,
            C costLimit) {
        return findVertexSequenceOverWaypoints(waypoints, Integer.MAX_VALUE, costLimit, () -> new HashSet<>()::add);
    }

    /**
     * Helper function for implementing {@link #findVertexSequenceOverWaypoints(Iterable, int, Number, Supplier)}.
     *
     * @param <VV>                       the vertex type
     * @param <CC>                       the number type
     * @param waypoints                  the waypoints
     * @param findVertexSequenceFunction the search function, for example {@code this::findVertexSequence}
     * @param zero                       the zero value
     * @param sumFunction                the sum function
     * @return an ordered pair with the combined sequence
     */

    @SuppressWarnings("unchecked")
    static <VV, CC extends Number & Comparable<CC>> @Nullable SimpleOrderedPair<PersistentList<VV>, CC> findVertexSequenceOverWaypoints(
            Iterable<VV> waypoints,
            BiFunction<VV, VV, OrderedPair<PersistentList<VV>, CC>> findVertexSequenceFunction,
            CC zero,
            BiFunction<CC, CC, CC> sumFunction) {
        List<VV> sequence = new ArrayList<>();
        VV prev = null;
        CC sum = zero;
        int count = 0;
        for (VV next : waypoints) {
            if (prev != null) {
                final OrderedPair<PersistentList<VV>, CC> result = findVertexSequenceFunction.apply(prev, next);
                if (result == null) {
                    return null;
                } else {
                    final List<VV> nextSequence = result.first().asList();
                    sequence.addAll(sequence.isEmpty() ? nextSequence : nextSequence.subList(1, nextSequence.size()));
                    sum = sumFunction.apply(sum, result.second());
                }
            }
            prev = next;
            count++;
        }

        if (count == 1) {
            // the set of waypoints is degenerate
            return new SimpleOrderedPair<>(VectorList.of(prev), zero);
        }

        return new SimpleOrderedPair<>(VectorList.copyOf(sequence), sum);
    }
}