/*
 * @(#)VertexPathSearchAlgo.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.path.algo;

import org.jhotdraw8.graph.algo.AddToSet;
import org.jhotdraw8.graph.path.backlink.VertexBackLinkWithCost;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntBiFunction;

/// Interface for a vertex path search algorithm.
///
/// @param <V> the vertex data type
public interface VertexPathSearchAlgo<V> {

    /// Search engine method.
    ///
    /// @param startVertices        the set of start vertices
    /// @param goalPredicate        the goal predicate
    /// @param nextVerticesFunction the next vertices function
    /// @param maxDepth             the maximal depth (inclusive) of the search
    ///                             Must be `>= 0`.
    /// @param costLimit            the algorithm-specific cost limit
    /// @param visited              the visited function
    /// @return on success: a back link, otherwise: null
    @Nullable VertexBackLinkWithCost<V> search(
            Iterable<V> startVertices,
            Predicate<V> goalPredicate,
            Function<V, Iterable<V>> nextVerticesFunction,
            int maxDepth,
            int costLimit,
            ToIntBiFunction<V, V> costFunction,
            AddToSet<V> visited);
}
