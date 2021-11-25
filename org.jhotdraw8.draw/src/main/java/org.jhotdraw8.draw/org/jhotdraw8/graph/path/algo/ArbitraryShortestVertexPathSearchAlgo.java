package org.jhotdraw8.graph.path.algo;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.graph.path.backlink.VertexBackLink;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Searches an arbitrary vertex path from a set of start vertices to a set of goal
 * vertices using Dijkstra's algorithm.
 * <p>
 * The provided cost function must return values {@literal >= 0} for all arrows.
 * <p>
 * References:
 * <dl>
 * <dt>Esger W. Dijkstra (1959), A note on two problems in connexion with graphs,
 * Problem 2.
 * </dt>
 * <dd><a href="https://www-m3.ma.tum.de/twiki/pub/MN0506/WebHome/dijkstra.pdf">tum.de</a></dd>
 * </dl>
 *
 * @param <V> the vertex data type
 * @param <A> the arrow data type
 * @param <C> the cost number type
 */
public class ArbitraryShortestVertexPathSearchAlgo<V, C extends Number & Comparable<C>> implements VertexPathSearchAlgo<V, C> {


    @Override
    public @Nullable VertexBackLink<V, C> search(
            @NonNull Iterable<V> startVertices,
            @NonNull Predicate<V> goalPredicate,
            @NonNull Function<V, Iterable<V>> nextVerticesFunction, @NonNull C zero,
            @NonNull C positiveInfinity,
            @NonNull C maxCost,
            @NonNull BiFunction<V, V, C> costf,
            @NonNull BiFunction<C, C, C> sumf) {
        // Priority queue: back-links with shortest distance from start come first.
        PriorityQueue<VertexBackLink<V, C>> queue = new PriorityQueue<>(
                Comparator.<VertexBackLink<V, C>, C>comparing(VertexBackLink::getCost).thenComparing(VertexBackLink::getDepth));

        // Map with best known costs from start to a specific vertex. If an entry is missing, we assume infinity.
        Map<V, C> costMap = new HashMap<>();

        // Insert start itself in priority queue and initialize its cost as 0.
        for (V start : startVertices) {
            queue.add(new VertexBackLink<>(start, null, zero));
            costMap.put(start, zero);
        }

        // Loop until we have reached the goal, or queue is exhausted.
        while (!queue.isEmpty()) {
            VertexBackLink<V, C> node = queue.remove();
            final V u = node.getVertex();
            if (goalPredicate.test(u)) {
                return node;
            }
            C costToU = node.getCost();

            for (V v : nextVerticesFunction.apply(u)) {
                C bestKnownCost = costMap.getOrDefault(v, positiveInfinity);
                C costFromUToV = costf.apply(u, v);
                if (costFromUToV.compareTo(zero) < 0) {
                    throw new IllegalArgumentException("cost(" + costFromUToV + ") is negative for arrow from " + u + " to " + v);
                }
                C costThroughU = sumf.apply(costToU, costFromUToV);

                // If there is a shorter path to v through u.
                if (costThroughU.compareTo(bestKnownCost) < 0 && costThroughU.compareTo(maxCost) <= 0) {
                    // Update cost to v.
                    costMap.put(v, costThroughU);
                    queue.add(new VertexBackLink<>(v, node, costThroughU));
                }
            }
        }

        return null;
    }
}
