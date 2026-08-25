/*
 * @(#)UniqueShortestVertexPathSearchAlgo.java
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

/// See [UniqueShortestArcPathSearchAlgo] for a description of this
/// algorithm.
///
/// @param <V> the vertex data type
public class UniqueShortestVertexPathSearchAlgo<V> implements VertexPathSearchAlgo<V> {
    public UniqueShortestVertexPathSearchAlgo() {
    }

    /// {@inheritDoc}
    ///
    /// @param startVertices the set of start vertices
    /// @param goalPredicate the goal predicate
    /// @param nextVertices  the next vertices function
    /// @param maxDepth      the maximal depth (inclusive) of the search
    ///                      Must be `>= 0`.
    /// @param costLimit     the maximal cost (inclusive) of a path.
    ///                      Must be `>= zero`.
    /// @param costFunction  the cost function
    ///                      The cost must be `> 0` if the graph
    ///                      has cycles.
    ///                      The cost must be `>= 0` if the graph
    ///                      is acyclic.
    /// @param visited
    /// @return on success: a back link, otherwise: null
    @Override
    public @Nullable VertexBackLinkWithCost<V> search(
            final Iterable<V> startVertices,
            final Predicate<V> goalPredicate,
            final Function<V, Iterable<V>> nextVertices,
            int maxDepth,
            final int costLimit,
            final ToIntBiFunction<V, V> costFunction,
            AddToSet<V> visited) {

        AlgoArguments.checkMaxDepthMaxCostArguments(maxDepth, costLimit);
        CheckedNonNegativeVertexCostFunction<V> costf = new CheckedNonNegativeVertexCostFunction<>(costFunction);

        // Priority queue: back-links by lower cost and shallower depth.
        //          Ordering by depth prevents that the algorithm
        //          unnecessarily follows zero-length arrows.
        PriorityQueue<VertexBackLinkWithCost<V>> queue = new PriorityQueue<>(
                Comparator.<VertexBackLinkWithCost<V>, Integer>comparing(VertexBackLinkWithCost::getCost).thenComparing(VertexBackLinkWithCost::getDepth)
        );

        // Map with best known costs from start to a vertex and with the number
        // of times we have reached the map.
        // If an entry is missing, we assume infinity.
        Map<V, CostData> costMap = new HashMap<>();

        CostData infiniteCost = new CostData(Integer.MAX_VALUE, 0);

        // Insert start itself in priority queue and initialize its cost as 0,
        // and number of paths with 1.
        for (V start : startVertices) {
            queue.add(new VertexBackLinkWithCost<>(start, null, 0));
            costMap.put(start, new CostData(0, 1));
        }

        // Loop until we have reached the goal, or queue is exhausted.
        int maxCost = costLimit;
        VertexBackLinkWithCost<V> found = null;
        while (!queue.isEmpty()) {
            VertexBackLinkWithCost<V> u = queue.remove();
            int costToU = u.getCost();
            if (goalPredicate.test(u.getVertex())) {
                if (found == null) {
                    // We have found a shortest path for the first time.
                    // We can now limit the maxCost of further searches.
                    found = u;
                    maxCost = costToU;
                } else if (costToU == maxCost) {
                    // We have found another shortest path with exactly
                    // the same cost!
                    return null;
                }
            }

            if (found != null && costToU > maxCost) {
                // Once we have found a shortest path, we are only interested
                // in other paths that have the same cost.
                break;
            }

            if (u.getDepth() < maxDepth) {
                for (V v : nextVertices.apply(u.getVertex())) {
                    CostData costDataV = costMap.getOrDefault(v, infiniteCost);
                    final int bestKnownCost = costDataV.getCost();
                    int cost = (costToU + costf.applyAsInt(u.getVertex(), v));

                    // If there is a shorter path to v through u.
                    if (cost <= maxCost) {
                        if (cost < bestKnownCost) {
                            // Update cost data to v.
                            costMap.put(v, new CostData(cost, 1));
                            queue.add(new VertexBackLinkWithCost<>(v, u, cost));
                        } else if (cost == bestKnownCost) {
                            // There is more than one shortest path to v!
                            costDataV.increaseVisitCount();
                        }
                    }
                }
            }
        }

        // The shortest path to the goal is only unique, if all vertices on the
        // path have been visited only once.
        for (VertexBackLinkWithCost<V> node = found; node != null; node = node.getParent()) {
            if (costMap.get(node.getVertex()).getVisiCount() != 1) {
                return null;
            }
        }

        return found;
    }
}
