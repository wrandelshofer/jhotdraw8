/*
 * @(#)AllArrowsSequencesFinder.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.path.algo;

import org.jhotdraw8.collection.pair.OrderedPair;
import org.jhotdraw8.icollection.persistent.PersistentList;

import java.util.function.Predicate;

/**
 * Interface for finding all sequences between a set of source
 * vertices and goal vertices up to a maximal depth in a directed graph.
 *
 * @param <V> the vertex data type
 * @param <A> the arrow data type
 * @param <C> the cost number type
 */
public interface AllArrowsSequencesFinder<V, A, C extends Number & Comparable<C>> {

    /**
     * Finds all arrow paths up to (including) the specified maximal cost.
     *
     * @param startVertices the set of start vertices
     * @param goalPredicate the goal predicate
     * @param maxDepth      the maximal depth (inclusive) of the search
     *                      Must be {@literal >= 0}.
     * @param costLimit     the algorithm-specific cost limit
     * @return all paths
     */
    Iterable<OrderedPair<PersistentList<A>, C>> findAllArrowSequences(
            Iterable<V> startVertices,
            Predicate<V> goalPredicate,
            int maxDepth, C costLimit);

}
