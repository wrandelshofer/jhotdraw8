package org.jhotdraw8.graph.path;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.ImmutableList;
import org.jhotdraw8.collection.ImmutableLists;
import org.jhotdraw8.collection.OrderedPair;
import org.jhotdraw8.graph.Arc;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * Defines an API for finding {@link Arc} sequences associated with a cost
 * through a directed graph.
 *
 * @param <V> the vertex data type
 * @param <A> the arrow data type
 * @param <C> the cost number type
 */
public interface ArcSequenceBuilder<V, A, C extends Number & Comparable<C>> {

    /**
     * Finds an arc sequence from a set of start vertices to a vertex
     * that satisfies the goal predicate.
     *
     * @param startVertices the start vertices
     * @param goalPredicate the goal vertex
     * @param maxCost       the maximal cost of the path
     * @return an ordered pair (arc sequence, cost),
     * or null if no sequence was found.
     */
    @Nullable
    OrderedPair<ImmutableList<Arc<V, A>>, C> findArcSequence(
            @NonNull Iterable<V> startVertices,
            @NonNull Predicate<V> goalPredicate,
            @NonNull C maxCost);

    /**
     * Finds an arc sequence from start to goal.
     *
     * @param start   the start vertex
     * @param goal    the goal vertex
     * @param maxCost the maximal cost of the path
     * @return an ordered pair (arc sequence, cost),
     * or null if no sequence was found.
     */
    default @Nullable OrderedPair<ImmutableList<Arc<V, A>>, C> findArcSequence(
            @NonNull V start,
            @NonNull V goal,
            @NonNull C maxCost) {
        return findArcSequence(List.of(start), goal::equals, maxCost);
    }


    /**
     * Finds an arc walk through the given waypoints.
     *
     * @param waypoints               an iterable of waypoints
     * @param maxCostBetweenWaypoints the maximal cost for paths between waypoints
     * @return an ordered pair (arc sequence, cost),
     * or null if no sequence was found.
     */
    OrderedPair<ImmutableList<Arc<V, A>>, C> findArcSequenceOverWaypoints(
            @NonNull Iterable<V> waypoints,
            @NonNull C maxCostBetweenWaypoints);


    /**
     * Helper function for implementing {@link #findArcSequenceOverWaypoints(Iterable, Number)}.
     *
     * @param waypoints               the waypoints
     * @param findArcSequenceFunction the search function, for example {@code this::findArrowSequence}
     * @param zero                    the zero value
     * @param sumFunction             the sum function
     * @param <VV>                    the vertex type
     * @param <CC>                    the number type
     * @return an ordered pair with the combined sequence
     */
    static <VV, AA, CC extends Number & Comparable<CC>> OrderedPair<ImmutableList<Arc<VV, AA>>, CC> findArcSequenceOverWaypoints(
            @NonNull Iterable<VV> waypoints,
            @NonNull BiFunction<VV, VV, OrderedPair<ImmutableList<Arc<VV, AA>>, CC>> findArcSequenceFunction,
            @NonNull CC zero,
            @NonNull BiFunction<CC, CC, CC> sumFunction) {
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
            return new OrderedPair<>(ImmutableLists.of(), zero);
        }

        return new OrderedPair<>(ImmutableLists.copyOf(sequence), sum);
    }
}
