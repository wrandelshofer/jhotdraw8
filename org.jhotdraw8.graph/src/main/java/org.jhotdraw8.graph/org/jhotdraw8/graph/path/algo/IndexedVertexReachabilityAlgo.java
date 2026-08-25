/*
 * @(#)IndexedVertexReachabilityAlgo.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.path.algo;

import org.jhotdraw8.graph.algo.AddToIntSet;

import java.util.Spliterator;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.ToIntBiFunction;

/// Interface for a reachability checker algorithm over a directed graph.
///
/// @param <C> the cost number type
public interface IndexedVertexReachabilityAlgo {
    /// Search engine method.
    ///
    /// @param startVertices        the set of start vertices
    /// @param goalPredicate        the goal predicate
    /// @param nextVerticesFunction the next vertices function
    /// @param maxDepth             the maximal depth (inclusive) of the search
    ///                             Must be {@literal >= 0}.
    /// @param costLimit            the algorithm-specific cost limit
    /// @param costFunction         the cost function
    /// @param visited              the visited function
    /// @return true on success
    boolean tryToReach(Iterable<Integer> startVertices,
                       IntPredicate goalPredicate,
                       Function<Integer, Spliterator.OfInt> nextVerticesFunction,
                       int maxDepth,
                       int costLimit,
                       ToIntBiFunction<Integer, Integer> costFunction,
                       AddToIntSet visited);
}
