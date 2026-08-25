/*
 * @(#)AnyShortestVertexPathSearchAlgo.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.path.algo;

import org.jhotdraw8.graph.algo.AddToSet;
import org.jhotdraw8.graph.path.backlink.VertexBackLinkWithCost;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntBiFunction;

/// See [AnyShortestArcPathSearchAlgo] for a description of this
/// algorithm.
///
/// @param <V> the vertex data type
/// @param <C> the cost number type
public class AnyShortestVertexPathSearchAlgo<V> implements VertexPathSearchAlgo<V> {
    public AnyShortestVertexPathSearchAlgo() {
    }

    /// {@inheritDoc}
    ///
    /// @param startVertices        the set of start vertices
    /// @param goalPredicate        the goal predicate
    /// @param nextVerticesFunction the next vertices function
    /// @param maxDepth             the maximal depth (inclusive) of the search
    ///                             Must be {@literal >= 0}.
    /// @param costLimit            the maximal cost (inclusive) of a path.
    ///                             Must be {@literal >= zero}.
    /// @param costFunction         the cost function
    /// @param visited
    /// @return on success: a back link, otherwise: null
    @Override
    public @Nullable VertexBackLinkWithCost<V> search(
            Iterable<V> startVertices,
            Predicate<V> goalPredicate,
            Function<V, Iterable<V>> nextVerticesFunction,
            int maxDepth,
            int costLimit,
            ToIntBiFunction<V, V> costFunction,
            AddToSet<V> visited) {

        AlgoArguments.checkMaxDepthMaxCostArguments(maxDepth, costLimit);
        CheckedNonNegativeVertexCostFunction<V> costf = new CheckedNonNegativeVertexCostFunction<>(costFunction);

        // Priority queue: back-links by lower cost and shallower depth.
        //          Ordering by shallower depth prevents that the algorithm
        //          unnecessarily follows zero-length arrows.
        PriorityQueue<VertexBackLinkWithCost<V>> queue = new PriorityQueue<>(
                Comparator.<VertexBackLinkWithCost<V>, Integer>comparing(VertexBackLinkWithCost::getCost).thenComparing(VertexBackLinkWithCost::getDepth));

        // Map with best known costs from start to a specific vertex.
        // If an entry is missing, we assume infinity.
        Map<V, Integer> costMap = new HashMap<>();

        // Insert start itself in priority queue and initialize its cost to 0.
        for (V start : startVertices) {
            queue.add(new VertexBackLinkWithCost<>(start, null, 0));
            costMap.put(start, 0);
        }

        // Loop until we have reached the goal, or queue is exhausted.
        while (!queue.isEmpty()) {
            VertexBackLinkWithCost<V> u = queue.remove();
            if (goalPredicate.test(u.getVertex())) {
                return u;
            }

            if (u.getDepth() < maxDepth) {
                for (V v : nextVerticesFunction.apply(u.getVertex())) {
                    int bestKnownCost = costMap.getOrDefault(v, Integer.MAX_VALUE);
                    int cost = (u.getCost() + costf.applyAsInt(u.getVertex(), v));

                    // If there is a cheaper path to v through u.
                    if (cost < bestKnownCost && cost <= costLimit) {
                        // Update cost to v and add v again to the queue.
                        costMap.put(v, cost);
                        queue.add(new VertexBackLinkWithCost<>(v, u, cost));
                    }
                }
            }
        }

        return null;
    }
}
