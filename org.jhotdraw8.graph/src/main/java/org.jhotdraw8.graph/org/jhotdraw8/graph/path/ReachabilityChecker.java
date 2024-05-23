/*
 * @(#)ReachabilityChecker.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.path;

import org.jhotdraw8.graph.algo.AddToSet;

import java.util.function.Predicate;

/**
 * Interface for checking if there is a vertex sequence
 * up to (inclusive) a maximal cost in a directed graph.
 *
 * @param <V> the vertex data type
 * @param <C> the cost number type
 */
public interface ReachabilityChecker<V, C extends Number & Comparable<C>> {
    /**
     * Checks if a vertex sequence from a set of start vertices to a vertex
     * that satisfies the goal predicate exists.
     *
     * @param startVertices the start vertices
     * @param goalPredicate the goal vertex
     * @param maxDepth      the maximal depth (inclusive) of the search
     *                      Must be {@literal >= 0}.
     * @param costLimit     the algorithm-specific cost limit
     * @param visited       the visited function
     * @return true if a sequence was found.
     */
    boolean isReachable(Iterable<V> startVertices,
                        Predicate<V> goalPredicate,
                        int maxDepth, C costLimit, AddToSet<V> visited);

    /**
     * Checks if a vertex sequence from a start vertex to a vertex
     * that satisfies the goal predicate exists.
     *
     * @param start         the start vertex
     * @param goalPredicate the goal vertex
     * @param maxDepth      the maximal depth (inclusive) of the search
     *                      Must be {@literal >= 0}.
     * @param costLimit     the algorithm-specific cost limit
     * @param visited       the visited function
     * @return true if a sequence was found.
     */
    boolean isReachable(V start,
                        Predicate<V> goalPredicate,
                        int maxDepth, C costLimit, AddToSet<V> visited);

    /**
     * Checks if a vertex sequence from start to goal exists.
     *
     * @param start     the start vertex
     * @param goal      the goal vertex
     * @param maxDepth  the maximal depth (inclusive) of the search
     *                  Must be {@literal >= 0}.
     * @param costLimit the algorithm-specific cost limit
     * @param visited   the visited function
     * @return true if a sequence was found.
     */
    boolean isReachable(V start, V goal, int maxDepth, C costLimit, AddToSet<V> visited);


}
