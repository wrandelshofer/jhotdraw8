/*
 * @(#)UniqueShortestArcPathSearchAlgo.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.path.algo;

import org.jhotdraw8.base.function.Function3;
import org.jhotdraw8.graph.Arc;
import org.jhotdraw8.graph.algo.AddToSet;
import org.jhotdraw8.graph.path.backlink.ArcBackLinkWithCost;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Searches a unique shortest path from a set of start vertices to a set of goal
 * vertices using Dijkstra's algorithm.
 * <p>
 * If the graph is acyclic, then the provided cost function must return values
 * {@literal >= 0} for all arrows.<br>
 * If the graph has cycles, then the provided cost function must return values
 * {@literal > 0} for all arrows. (If the graph has cycles and the cost function
 * returns values that are 0, then this algorithm incorrectly considers a
 * path as non-unique, if it can be reached by a walk).
 * <p>
 * Performance characteristics:
 * <dl>
 *     <dt>When the algorithm returns a back link</dt><dd>less or equal {@literal O( |A| + |V|*log|V| )} within max cost</dd>
 *     <dt>When the algorithm returns null</dt><dd>less or equal {@literal O( |A| + |V|*log|V| )} within max cost</dd>
 * </dl>
 * <p>
 * References:
 * <dl>
 *   <dt>Edsger W. Dijkstra (1959)</dt>
 *   <dd>A note on two problems in connexion with graphs,Problem 2.
 *   <a href="https://www-m3.ma.tum.de/twiki/pub/MN0506/WebHome/dijkstra.pdf">tum.de</a></dd>
 *
 *   <dt>Sampath Kannan, Sanjeef Khanna, Sudeepa Roy. (2008)</dt>
 *   <dd>STCON in Directed Unique-Path Graphs.
 *        Chapter 2.1 Properties of Unique-Path Graphs.
 *        <a href="https://www.cis.upenn.edu/~sanjeev/papers/fsttcs08_stcon.pdf">cis.upenn.edu</a></dd>
 * </dl>
 *
 * @param <V> the vertex data type
 * @param <A> the arrow data type
 * @param <C> the cost number type
 */
public class UniqueShortestArcPathSearchAlgo<V, A, C extends Number & Comparable<C>> implements ArcPathSearchAlgo<V, A, C> {
    public UniqueShortestArcPathSearchAlgo() {
    }

    /**
     * {@inheritDoc}
     *
     * @param startVertices    the set of start vertices
     * @param goalPredicate    the goal predicate
     * @param nextArcsFunction the next arcs function
     * @param maxDepth         the maximal depth (inclusive) of the search
     *                         Must be {@literal >= 0}.
     * @param zero             the zero cost value
     * @param costLimit        the maximal cost (inclusive) of a path.
     *                         Must be {@literal >= zero}.
     * @param costFunction     the cost function.<br>
     *                         The cost must be {@literal > 0} if the graph
     *                         has cycles.<br>
     *                         The cost must be {@literal >= 0} if the graph
     *                         is acyclic.
     * @param sumFunction      the sum function for adding two cost values
     * @param visited
     * @return on success: a back link, otherwise: null
     */
    @Override
    public @Nullable ArcBackLinkWithCost<V, A, C> search(
            final Iterable<V> startVertices,
            final Predicate<V> goalPredicate,
            final Function<V, Iterable<Arc<V, A>>> nextArcsFunction,
            int maxDepth, final C zero,
            final C costLimit,
            final Function3<V, V, A, C> costFunction,
            final BiFunction<C, C, C> sumFunction, AddToSet<V> visited) {

        AlgoArguments.checkMaxDepthMaxCostArguments(maxDepth, zero, costLimit);
        CheckedNonNegativeArcCostFunction3<V, A, C> costf = new CheckedNonNegativeArcCostFunction3<>(zero, costFunction);

        // Priority queue: back-links by lower cost and shallower depth.
        //          Ordering by depth prevents that the algorithm
        //          unnecessarily follows zero-length arrows.
        PriorityQueue<ArcBackLinkWithCost<V, A, C>> queue = new PriorityQueue<>(
                Comparator.<ArcBackLinkWithCost<V, A, C>, C>comparing(ArcBackLinkWithCost::getCost).thenComparing(ArcBackLinkWithCost::getDepth)
        );

        // Map with best known costs from start to a vertex and with the number
        // of times we have reached the map.
        // If an entry is missing, we assume infinity.
        Map<V, CostData<C>> costMap = new HashMap<>();

        CostData<C> infiniteCost = new CostData<>(null, 0);

        // Insert start itself in priority queue and initialize its cost as 0,
        // and number of paths with 1.
        for (V start : startVertices) {
            queue.add(new ArcBackLinkWithCost<>(start, null, null, zero));
            costMap.put(start, new CostData<>(zero, 1));
        }

        // Loop until we have reached the goal, or queue is exhausted.
        C maxCost = costLimit;
        ArcBackLinkWithCost<V, A, C> found = null;
        while (!queue.isEmpty()) {
            ArcBackLinkWithCost<V, A, C> u = queue.remove();
            C costToU = u.getCost();
            if (goalPredicate.test(u.getVertex())) {
                if (found == null) {
                    // We have found a shortest path for the first time.
                    // We can now limit the maxCost of further searches.
                    found = u;
                    maxCost = costToU;
                } else if (costToU.compareTo(maxCost) == 0) {
                    // We have found another shortest path with exactly
                    // the same cost!
                    return null;
                }
            }

            if (found != null && costToU.compareTo(maxCost) > 0) {
                // Once we have found a shortest path, we are only interested
                // in other paths that have the same cost.
                break;
            }

            if (u.getDepth() < maxDepth) {
                for (Arc<V, A> v : nextArcsFunction.apply(u.getVertex())) {
                    CostData<C> costDataV = costMap.getOrDefault(v.getEnd(), infiniteCost);
                    final C bestKnownCost = costDataV.getCost();
                    C cost = sumFunction.apply(costToU, costf.apply(u.getVertex(), v.getEnd(), v.getArrow()));

                    // If there is a shorter path to v through u.
                    if (cost.compareTo(maxCost) <= 0) {
                        final int compare = bestKnownCost == null ? -1 : cost.compareTo(bestKnownCost);
                        if (compare < 0) {
                            // Update cost data to v.
                            costMap.put(v.getEnd(), new CostData<>(cost, 1));
                            queue.add(new ArcBackLinkWithCost<>(v.getEnd(), v.getArrow(), u, cost));
                        } else if (compare == 0) {
                            // There is more than one shortest path to v!
                            costDataV.increaseVisitCount();
                        }
                    }
                }
            }
        }

        // The shortest path to the goal is only unique, if all vertices on the
        // path have been visited only once.
        for (ArcBackLinkWithCost<V, A, C> node = found; node != null; node = node.getParent()) {
            if (costMap.get(node.getVertex()).getVisiCount() != 1) {
                return null;
            }
        }

        return found;
    }
}
