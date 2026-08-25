/*
 * @(#)VertexReachabilityAlgo.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.path.algo;

import org.jhotdraw8.graph.algo.AddToSet;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntBiFunction;

/// Interface for a reachability test algorithm.
///
/// @param <V> the vertex data type
public interface VertexReachabilityAlgo<V, C extends Number & Comparable<C>> {

    /// Search engine method.
    ///
    /// @param startVertices        the set of start vertices
    /// @param goalPredicate        the goal predicate
    /// @param maxDepth             the maximal depth (inclusive) of the search
    ///                             Must be `>= 0`.
    /// @param costLimit            the algorithm-specific cost limit
    /// @param nextVerticesFunction the next nodes function
    /// @param costFunction         the cost function
    /// @param visited              the visited function
    /// @return true on success
    boolean tryToReach(
            Iterable<V> startVertices,
            Predicate<V> goalPredicate,
            int maxDepth,
            int costLimit,
            Function<V, Iterable<V>> nextVerticesFunction,
            ToIntBiFunction<V, V> costFunction,
            AddToSet<V> visited);
}
