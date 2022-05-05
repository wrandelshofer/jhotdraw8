/*
 * @(#)ShortestArbitraryVertexPathSearchAlgo.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.path.algo;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.graph.path.backlink.VertexBackLinkWithCost;
import org.jhotdraw8.util.function.AddToSet;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * See {@link ShortestArbitraryArcPathSearchAlgo} for a description of this
 * algorithm.
 *
 * @param <V> the vertex data type
 * @param <C> the cost number type
 */
public class ShortestArbitraryVertexPathSearchAlgo<V, C extends Number & Comparable<C>> implements VertexPathSearchAlgo<V, C> {
    public ShortestArbitraryVertexPathSearchAlgo() {
    }

    /**
     * {@inheritDoc}
     * @param startVertices        the set of start vertices
     * @param goalPredicate        the goal predicate
     * @param nextVerticesFunction the next vertices function
     * @param maxDepth             the maximal depth (inclusive) of the search
     *                             Must be {@literal >= 0}.
     * @param zero                 the zero cost value
     * @param costLimit            the maximal cost (inclusive) of a path.
     *                             Must be {@literal >= zero}.
     * @param costFunction         the cost function
     * @param sumFunction          the sum function for adding two cost values
     * @return on success: a back link, otherwise: null
     * @param visited
     */
    @Override
    public @Nullable VertexBackLinkWithCost<V, C> search(
            @NonNull Iterable<V> startVertices,
            @NonNull Predicate<V> goalPredicate,
            @NonNull Function<V, Iterable<V>> nextVerticesFunction,
            int maxDepth,
            @NonNull C zero,
            @NonNull C costLimit,
            @NonNull BiFunction<V, V, C> costFunction,
            @NonNull BiFunction<C, C, C> sumFunction, @NonNull AddToSet<V> visited) {

        AlgoArguments.checkMaxDepthMaxCostArguments(maxDepth, zero, costLimit);
        CheckedNonNegativeVertexCostFunction<V, C> costf = new CheckedNonNegativeVertexCostFunction<>(zero, costFunction);

        // Priority queue: back-links by lower cost and shallower depth.
        //          Ordering by shallower depth prevents that the algorithm
        //          unnecessarily follows zero-length arrows.
        PriorityQueue<VertexBackLinkWithCost<V, C>> queue = new PriorityQueue<>(
                Comparator.<VertexBackLinkWithCost<V, C>, C>comparing(VertexBackLinkWithCost::getCost).thenComparing(VertexBackLinkWithCost::getDepth));

        // Map with best known costs from start to a specific vertex.
        // If an entry is missing, we assume infinity.
        Map<V, C> costMap = new HashMap<>();

        // Insert start itself in priority queue and initialize its cost to 0.
        for (V start : startVertices) {
            queue.add(new VertexBackLinkWithCost<>(start, null, zero));
            costMap.put(start, zero);
        }

        // Loop until we have reached the goal, or queue is exhausted.
        while (!queue.isEmpty()) {
            VertexBackLinkWithCost<V, C> u = queue.remove();
            if (goalPredicate.test(u.getVertex())) {
                return u;
            }

            if (u.getDepth() < maxDepth) {
                for (V v : nextVerticesFunction.apply(u.getVertex())) {
                    C bestKnownCost = costMap.get(v);
                    C cost = sumFunction.apply(u.getCost(), costf.apply(u.getVertex(), v));

                    // If there is a cheaper path to v through u.
                    if ((bestKnownCost == null || cost.compareTo(bestKnownCost) < 0)
                            && cost.compareTo(costLimit) <= 0) {
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
