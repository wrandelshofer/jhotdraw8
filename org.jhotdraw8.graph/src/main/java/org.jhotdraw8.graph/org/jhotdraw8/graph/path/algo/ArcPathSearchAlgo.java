/*
 * @(#)ArcPathSearchAlgo.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.path.algo;

import org.jhotdraw8.base.function.ToIntFunction3;
import org.jhotdraw8.graph.Arc;
import org.jhotdraw8.graph.algo.AddToSet;
import org.jhotdraw8.graph.path.backlink.ArcBackLinkWithCost;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Predicate;

/// Interface for an arc path search algorithm over a directed graph.
///
/// @param <V> the vertex data type
/// @param <A> the arrow data type
public interface ArcPathSearchAlgo<V, A> {

    /// Search engine method.
    ///
    /// @param startVertices    the set of start vertices
    /// @param goalPredicate    the goal predicate
    /// @param nextArcsFunction the next arcs function
    /// @param maxDepth         the maximal depth (inclusive) of the search
    ///                         Must be {@literal >= 0}.
    /// @param costLimit        the algorithm-specific cost limit.
    /// @param costFunction     the cost function
    /// @param visited          the visited function
    /// @return on success: a back link, otherwise: null
    @Nullable ArcBackLinkWithCost<V, A> search(
            Iterable<V> startVertices,
            Predicate<V> goalPredicate,
            Function<V, Iterable<Arc<V, A>>> nextArcsFunction,
            int maxDepth,
            int costLimit,
            ToIntFunction3<V, V, A> costFunction,
            AddToSet<V> visited);
}
