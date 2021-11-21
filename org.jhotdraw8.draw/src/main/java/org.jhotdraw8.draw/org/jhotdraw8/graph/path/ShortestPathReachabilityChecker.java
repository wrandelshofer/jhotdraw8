package org.jhotdraw8.graph.path;

import org.jhotdraw8.annotation.NonNull;

import java.util.Collections;
import java.util.function.Predicate;

/**
 * Defines an API for checking if a set of vertices can be reached from
 * a set of start vertices with a shortest path.
 *
 * @param <V>
 * @param <C>
 */
public interface ShortestPathReachabilityChecker<V, C extends Number & Comparable<C>> extends VertexSequenceBuilder<V, C> {
    /**
     * Checks if a vertex sequence from a set of start vertices to a vertex
     * that satisfies the goal predicate exists.
     *
     * @param startVertices the start vertices
     * @param goalPredicate the goal vertex
     * @param maxCost
     * @return an ordered pair (vertex sequence, cost),
     * or null if no sequence was found.
     */
    default boolean isReachable(@NonNull Iterable<V> startVertices,
                                @NonNull Predicate<V> goalPredicate,
                                @NonNull C maxCost) {
        return findVertexSequence(startVertices, goalPredicate, maxCost) != null;
    }

    /**
     * Checks if a vertex sequence from a start vertex to a vertex
     * that satisfies the goal predicate exists.
     *
     * @param start         the start vertex
     * @param goalPredicate the goal vertex
     * @param maxCost
     * @return an ordered pair (vertex sequence, cost),
     * or null if no sequence was found.
     */
    default boolean isReachable(@NonNull V start,
                                @NonNull Predicate<V> goalPredicate, @NonNull C maxCost) {
        return findVertexSequence(Collections.singletonList(start), goalPredicate, maxCost) != null;
    }

    /**
     * Checks if a vertex sequence from start to goal exists.
     *
     * @param start   the start vertex
     * @param goal    the goal vertex
     * @param maxCost
     * @return an ordered pair (vertex sequence, cost),
     * or null if no sequence was found.
     */

    default boolean isReachable(@NonNull V start, @NonNull V goal, @NonNull C maxCost) {
        return findVertexSequence(start, goal, maxCost) != null;
    }

    /**
     * Checks if a vertex sequence through the given waypoints exists.
     *
     * @param waypoints               a list of waypoints
     * @param maxCostBetweenWaypoints
     * @return an ordered pair (vertex sequence, cost),
     * or null if no sequence was found.
     */
    default boolean isReachableOverWaypoints(@NonNull Iterable<V> waypoints, @NonNull C maxCostBetweenWaypoints) {
        return findVertexSequenceOverWaypoints(waypoints, maxCostBetweenWaypoints) != null;
    }
}
