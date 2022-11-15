/*
 * @(#)ArrowSequenceFinder.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.path;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.OrderedPair;
import org.jhotdraw8.collection.function.AddToSet;
import org.jhotdraw8.collection.immutable.ImmutableArrayList;
import org.jhotdraw8.collection.immutable.ImmutableList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Interface for finding arrow sequences up to (inclusive) a maximal cost
 * in a directed graph.
 *
 * @param <V> the vertex data type
 * @param <A> the arrow data type
 * @param <C> the cost number type
 */
public interface ArrowSequenceFinder<V, A, C extends Number & Comparable<C>> {


    /**
     * Finds an arrow sequence from a set of start vertices to a vertex
     * that satisfies the goal predicate.
     *
     * @param startVertices the start vertex
     * @param goalPredicate the goal predicate
     * @param maxDepth      the maximal depth (inclusive) of the search
     *                      Must be {@literal >= 0}.
     * @param costLimit     the algorithm-specific cost limit
     * @param visited       the visited function
     * @return an ordered pair (arrow sequence, cost),
     * or null if no sequence was found.
     */
    @Nullable OrderedPair<ImmutableList<A>, C> findArrowSequence(
            @NonNull Iterable<V> startVertices,
            @NonNull Predicate<V> goalPredicate,
            int maxDepth,
            @NonNull C costLimit,
            @NonNull AddToSet<V> visited);

    /**
     * Finds an arrow sequence from a set of start vertices to a vertex
     * that satisfies the goal predicate.
     *
     * @param startVertices the start vertex
     * @param goalPredicate the goal predicate
     * @param maxDepth      the maximal depth (inclusive) of the search
     *                      Must be {@literal >= 0}.
     * @param costLimit     the algorithm-specific cost limit
     * @return an ordered pair (arrow sequence, cost),
     * or null if no sequence was found.
     */
    default @Nullable OrderedPair<ImmutableList<A>, C> findArrowSequence(
            @NonNull Iterable<V> startVertices,
            @NonNull Predicate<V> goalPredicate,
            int maxDepth,
            @NonNull C costLimit) {
        return findArrowSequence(startVertices, goalPredicate, maxDepth, costLimit, new HashSet<>()::add);
    }

    /**
     * Finds an arrow sequence from a set of start vertices to a vertex
     * that satisfies the goal predicate.
     *
     * @param startVertices the start vertex
     * @param goalPredicate the goal predicate
     * @param costLimit     the algorithm-specific cost limit
     * @return an ordered pair (arrow sequence, cost),
     * or null if no sequence was found.
     */
    default @Nullable OrderedPair<ImmutableList<A>, C> findArrowSequence(
            @NonNull Iterable<V> startVertices,
            @NonNull Predicate<V> goalPredicate,
            @NonNull C costLimit) {
        return findArrowSequence(startVertices, goalPredicate, Integer.MAX_VALUE, costLimit, new HashSet<>()::add);
    }

    /**
     * Finds an arrow sequence from start to goal.
     *
     * @param start     the start vertex
     * @param goal      the goal vertex
     * @param maxDepth  the maximal depth (inclusive) of the search
     *                  Must be {@literal >= 0}.
     * @param costLimit the algorithm-specific cost limit
     * @param visited   the visited function
     * @return an ordered pair (arrow sequence, cost),
     * or null if no sequence was found.
     */
    default @Nullable OrderedPair<ImmutableList<A>, C> findArrowSequence(
            @NonNull V start,
            @NonNull V goal,
            int maxDepth,
            @NonNull C costLimit, @NonNull AddToSet<V> visited) {
        return findArrowSequence(ImmutableArrayList.of(start), goal::equals, maxDepth, costLimit, visited);
    }

    /**
     * Finds an arrow sequence from start to goal.
     *
     * @param start     the start vertex
     * @param goal      the goal vertex
     * @param maxDepth  the maximal depth (inclusive) of the search
     *                  Must be {@literal >= 0}.
     * @param costLimit the algorithm-specific cost limit
     * @return an ordered pair (arrow sequence, cost),
     * or null if no sequence was found.
     */
    default @Nullable OrderedPair<ImmutableList<A>, C> findArrowSequence(
            @NonNull V start,
            @NonNull V goal,
            int maxDepth,
            @NonNull C costLimit) {
        return findArrowSequence(ImmutableArrayList.of(start), goal::equals, maxDepth, costLimit, new HashSet<>()::add);
    }

    /**
     * Finds an arrow sequence from start to goal.
     *
     * @param start     the start vertex
     * @param goal      the goal vertex
     * @param costLimit the algorithm-specific cost limit
     * @return an ordered pair (arrow sequence, cost),
     * or null if no sequence was found.
     */
    default @Nullable OrderedPair<ImmutableList<A>, C> findArrowSequence(
            @NonNull V start,
            @NonNull V goal,
            @NonNull C costLimit) {
        return findArrowSequence(ImmutableArrayList.of(start), goal::equals, Integer.MAX_VALUE, costLimit, new HashSet<>()::add);
    }

    /**
     * Finds an arrow sequence through the given waypoints.
     *
     * @param waypoints         a list of waypoints
     * @param maxDepth          the maximal depth (inclusive) of the search
     *                          Must be {@literal >= 0}.
     * @param costLimit         the algorithm-specific cost limit for paths between waypoints
     * @param visitedSetFactory the visited set factory
     * @return an ordered pair (arrow sequence, cost),
     * or null if no sequence was found.
     */
    @Nullable OrderedPair<ImmutableList<A>, C> findArrowSequenceOverWaypoints(
            @NonNull Iterable<V> waypoints, int maxDepth, @NonNull C costLimit,
            @NonNull Supplier<AddToSet<V>> visitedSetFactory);

    /**
     * Finds an arrow sequence through the given waypoints.
     *
     * @param waypoints a list of waypoints
     * @param maxDepth  the maximal depth (inclusive) of the search
     *                  Must be {@literal >= 0}.
     * @param costLimit the algorithm-specific cost limit for paths between waypoints
     * @return an ordered pair (arrow sequence, cost),
     * or null if no sequence was found.
     */
    default @Nullable OrderedPair<ImmutableList<A>, C> findArrowSequenceOverWaypoints(
            @NonNull Iterable<V> waypoints, int maxDepth, @NonNull C costLimit) {
        return findArrowSequenceOverWaypoints(waypoints, maxDepth, costLimit, () -> new HashSet<>()::add);
    }

    /**
     * Finds an arrow sequence through the given waypoints.
     *
     * @param waypoints a list of waypoints
     * @param costLimit the algorithm-specific cost limit for paths between waypoints
     * @return an ordered pair (arrow sequence, cost),
     * or null if no sequence was found.
     */
    default @Nullable OrderedPair<ImmutableList<A>, C> findArrowSequenceOverWaypoints(
            @NonNull Iterable<V> waypoints, @NonNull C costLimit) {
        return findArrowSequenceOverWaypoints(waypoints, Integer.MAX_VALUE, costLimit, () -> new HashSet<>()::add);
    }

    /**
     * Helper function for implementing {@link #findArrowSequenceOverWaypoints(Iterable, int, Number, Supplier)}.
     *
     * @param <VV>                      the vertex type
     * @param <CC>                      the number type
     * @param waypoints                 the waypoints
     * @param findArrowSequenceFunction the search function, for example {@code this::findArrowSequence}
     * @param zero                      the zero value
     * @param sumFunction               the sum function
     * @return an ordered pair with the combined sequence
     */
    static <VV, AA, CC extends Number & Comparable<CC>> @Nullable OrderedPair<ImmutableList<AA>, CC> findArrowSequenceOverWaypoints(
            @NonNull Iterable<VV> waypoints,
            @NonNull BiFunction<VV, VV, OrderedPair<ImmutableList<AA>, CC>> findArrowSequenceFunction,
            @NonNull CC zero,
            @NonNull BiFunction<CC, CC, CC> sumFunction) {
        List<AA> sequence = new ArrayList<>();
        CC sum = zero;
        VV prev = null;
        int count = 0;
        for (VV next : waypoints) {
            if (prev != null) {
                final OrderedPair<ImmutableList<AA>, CC> result = findArrowSequenceFunction.apply(prev, next);
                if (result == null) {
                    return null;
                } else {
                    final List<AA> nextSequence = result.first().asList();
                    sequence.addAll(nextSequence);
                    sum = sumFunction.apply(sum, result.second());
                }
            }
            prev = next;
            count++;
        }

        if (count == 1) {
            // the set of waypoints is degenerate
            return new OrderedPair<>(ImmutableArrayList.of(), zero);
        }

        return new OrderedPair<>(ImmutableArrayList.copyOf(sequence), sum);
    }

}
