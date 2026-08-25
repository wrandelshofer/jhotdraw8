/*
 * @(#)ArcReachabilityAlgo.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.path.algo;

import org.jhotdraw8.base.function.ToIntFunction3;
import org.jhotdraw8.graph.Arc;

import java.util.function.Function;
import java.util.function.Predicate;

/// Interface for a reachability checker algorithm over a directed graph.
///
/// @param <V> the vertex data type
/// @param <A> the arrow data type
public interface ArcReachabilityAlgo<V, A> {

    /// Search engine method.
    ///
    /// @param startVertices    the set of start vertices
    /// @param goalPredicate    the goal predicate
    /// @param nextArcsFunction the next arcs function
    /// @param maxDepth         the maximal depth (inclusive) of the search
    ///                         Must be `>= 0`.
    /// @param costLimit        the algorithm-specific cost limit.
    /// @param costFunction     the cost function
    /// @return true on success
    boolean tryToReach(
            Iterable<V> startVertices,
            Predicate<V> goalPredicate,
            Function<V, Iterable<Arc<V, A>>> nextArcsFunction,
            int maxDepth,
            int costLimit,
            ToIntFunction3<V, V, A> costFunction);
}
