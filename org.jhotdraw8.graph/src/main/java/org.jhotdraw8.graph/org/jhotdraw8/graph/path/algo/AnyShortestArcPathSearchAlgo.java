/*
 * @(#)AnyShortestArcPathSearchAlgo.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.path.algo;

import org.jhotdraw8.base.function.ToIntFunction3;
import org.jhotdraw8.graph.Arc;
import org.jhotdraw8.graph.algo.AddToSet;
import org.jhotdraw8.graph.path.backlink.ArcBackLinkWithCost;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.function.Function;
import java.util.function.Predicate;

/// Searches an arbitrary shortest path from a set of start vertices to a set of goal
/// vertices using Dijkstra's algorithm.
///
/// The provided cost function must return values `>= 0` for all arrows.
///
/// Performance characteristics:
/// <dl>
///     <dt>When the algorithm returns a back link</dt><dd>less or equal `O( (|A| + |V|)*log|V| )` within max cost</dd>
///     <dt>When the algorithm returns null</dt><dd>exactly `O( (|A| + |V|)*log|V| )` within max cost</dd>
/// </dl>
/// References:
/// <dl>
///   <dt> Edsger W. Dijkstra (1959)</dt>
///   <dd>A note on two problems in connexion with graphs, Problem 2.
///    <a href="https://www-m3.ma.tum.de/twiki/pub/MN0506/WebHome/dijkstra.pdf">tum.de</a></dd>
/// </dl>
///
/// @param <V> the vertex data type
/// @param <A> the arrow data type
public class AnyShortestArcPathSearchAlgo<V, A> implements ArcPathSearchAlgo<V, A> {
    public AnyShortestArcPathSearchAlgo() {
    }

    /// {@inheritDoc}
    ///
    /// @param startVertices    the set of start vertices
    /// @param goalPredicate    the goal predicate
    /// @param nextArcsFunction the next arcs function
    /// @param maxDepth         the maximal depth (inclusive) of the search
    ///                         Must be `>= 0`.
    /// @param costLimit        the maximal cost (inclusive) of a path.
    ///                         Must be `>= zero`.
    /// @param costFunction     the cost function
    /// @param visited
    /// @return on success: a back link, otherwise: null
    @Override
    public @Nullable ArcBackLinkWithCost<V, A> search(
            final Iterable<V> startVertices,
            final Predicate<V> goalPredicate,
            final Function<V, Iterable<Arc<V, A>>> nextArcsFunction,
            int maxDepth,
            final int costLimit,
            final ToIntFunction3<V, V, A> costFunction,
            AddToSet<V> visited) {

        AlgoArguments.checkMaxDepthMaxCostArguments(maxDepth, costLimit);
        CheckedNonNegativeArcCostFunction3<V, A> costf = new CheckedNonNegativeArcCostFunction3<>(costFunction);


        // Priority queue: back-links by lower cost and shallower depth.
        //          Ordering by depth prevents that the algorithm
        //          unnecessarily follows zero-length arrows.
        PriorityQueue<ArcBackLinkWithCost<V, A>> queue = new PriorityQueue<>(
                Comparator.<ArcBackLinkWithCost<V, A>, Integer>comparing(ArcBackLinkWithCost::getCost).thenComparing(ArcBackLinkWithCost::getDepth)
        );

        // Map with best known costs from start to a specific vertex.
        // If an entry is missing, we assume infinity.
        Map<V, Integer> costMap = new HashMap<>();

        // Insert start itself in priority queue and initialize its cost to 0.
        for (V start : startVertices) {
            queue.add(new ArcBackLinkWithCost<>(start, null, null, 0));
            costMap.put(start, 0);
        }

        // Loop until we have reached the goal, or queue is exhausted.
        while (!queue.isEmpty()) {
            ArcBackLinkWithCost<V, A> u = queue.remove();
            if (goalPredicate.test(u.getVertex())) {
                return u;
            }

            if (u.getDepth() < maxDepth) {
                for (Arc<V, A> arc : nextArcsFunction.apply(u.getVertex())) {
                    V v = arc.getEnd();
                    int bestKnownCost = costMap.getOrDefault(v, Integer.MAX_VALUE);
                    int cost = (u.getCost() + costf.apply(u.getVertex(), v, arc.getArrow()));

                    // If there is a cheaper path to v through u.
                    if (cost < bestKnownCost
                            && cost <= costLimit) {
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
