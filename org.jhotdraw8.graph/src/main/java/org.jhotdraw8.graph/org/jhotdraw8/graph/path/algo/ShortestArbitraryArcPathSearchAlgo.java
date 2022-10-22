/*
 * @(#)ShortestArbitraryArcPathSearchAlgo.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.path.algo;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.function.TriFunction;
import org.jhotdraw8.collection.function.AddToSet;
import org.jhotdraw8.graph.Arc;
import org.jhotdraw8.graph.path.backlink.ArcBackLinkWithCost;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Searches an arbitrary shortest path from a set of start vertices to a set of goal
 * vertices using Dijkstra's algorithm.
 * <p>
 * The provided cost function must return values {@literal >= 0} for all arrows.
 * <p>
 * Performance characteristics:
 * <dl>
 *     <dt>When the algorithm returns a back link</dt><dd>less or equal {@literal O( (|A| + |V|)*log|V| )} within max cost</dd>
 *     <dt>When the algorithm returns null</dt><dd>exactly {@literal O( (|A| + |V|)*log|V| )} within max cost</dd>
 * </dl>
 * References:
 * <dl>
 *   <dt> Edsger W. Dijkstra (1959)</dt>
 *   <dd>A note on two problems in connexion with graphs, Problem 2.
 *    <a href="https://www-m3.ma.tum.de/twiki/pub/MN0506/WebHome/dijkstra.pdf">tum.de</a></dd>
 * </dl>
 *
 * @param <V> the vertex data type
 * @param <A> the arrow data type
 * @param <C> the cost number type
 */
public class ShortestArbitraryArcPathSearchAlgo<V, A, C extends Number & Comparable<C>> implements ArcPathSearchAlgo<V, A, C> {
    public ShortestArbitraryArcPathSearchAlgo() {
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
     * @param costFunction     the cost function
     * @param sumFunction      the sum function for adding two cost values
     * @param visited
     * @return on success: a back link, otherwise: null
     */
    @Override
    public @Nullable ArcBackLinkWithCost<V, A, C> search(
            final @NonNull Iterable<V> startVertices,
            final @NonNull Predicate<V> goalPredicate,
            final @NonNull Function<V, Iterable<Arc<V, A>>> nextArcsFunction,
            int maxDepth,
            final @NonNull C zero,
            final @NonNull C costLimit,
            final @NonNull TriFunction<V, V, A, C> costFunction,
            final @NonNull BiFunction<C, C, C> sumFunction, @NonNull AddToSet<V> visited) {

        AlgoArguments.checkMaxDepthMaxCostArguments(maxDepth, zero, costLimit);
        CheckedNonNegativeArcCostFunction<V, A, C> costf = new CheckedNonNegativeArcCostFunction<>(zero, costFunction);


        // Priority queue: back-links by lower cost and shallower depth.
        //          Ordering by depth prevents that the algorithm
        //          unnecessarily follows zero-length arrows.
        PriorityQueue<ArcBackLinkWithCost<V, A, C>> queue = new PriorityQueue<ArcBackLinkWithCost<V, A, C>>(
                Comparator.<ArcBackLinkWithCost<V, A, C>, C>comparing(ArcBackLinkWithCost::getCost).thenComparing(ArcBackLinkWithCost::getDepth)
        );

        // Map with best known costs from start to a specific vertex.
        // If an entry is missing, we assume infinity.
        Map<V, C> costMap = new HashMap<>();

        // Insert start itself in priority queue and initialize its cost to 0.
        for (V start : startVertices) {
            queue.add(new ArcBackLinkWithCost<>(start, null, null, zero));
            costMap.put(start, zero);
        }

        // Loop until we have reached the goal, or queue is exhausted.
        while (!queue.isEmpty()) {
            ArcBackLinkWithCost<V, A, C> u = queue.remove();
            if (goalPredicate.test(u.getVertex())) {
                return u;
            }

            if (u.getDepth() < maxDepth) {
                for (Arc<V, A> arc : nextArcsFunction.apply(u.getVertex())) {
                    V v = arc.getEnd();
                    C bestKnownCost = costMap.get(v);
                    C cost = sumFunction.apply(u.getCost(), costf.apply(u.getVertex(), v, arc.getArrow()));

                    // If there is a cheaper path to v through u.
                    if ((bestKnownCost == null || cost.compareTo(bestKnownCost) < 0)
                            && cost.compareTo(costLimit) <= 0) {
                        // Update cost to v and add v again to the queue.
                        costMap.put(v, cost);
                        queue.add(new ArcBackLinkWithCost<>(v, arc.getArrow(), u, cost));
                    }
                }
            }
        }

        return null;
    }
}
