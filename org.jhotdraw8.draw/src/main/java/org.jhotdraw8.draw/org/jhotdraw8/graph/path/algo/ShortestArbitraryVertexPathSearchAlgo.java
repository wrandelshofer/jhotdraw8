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
 * Performance characteristics:
 * <dl>
 *     <dt>When a path can be found</dt><dd>less or equal O( |A| + |V|*log|V| ) within max cost</dd>
 *     <dt>When no path can be found</dt><dd>exactly O( |A| + |V|*log|V| ) within max cost</dd>
 * </dl>
 *
 * @param <V> the vertex data type
 * @param <C> the cost number type
 */
public class ShortestArbitraryVertexPathSearchAlgo<V, C extends Number & Comparable<C>> implements VertexPathSearchAlgo<V, C> {


    /**
     * {@inheritDoc}
     *
     * @param startVertices        the set of start vertices
     * @param goalPredicate        the goal predicate
     * @param nextVerticesFunction the next vertices function
     * @param zero                 the zero cost value
     * @param positiveInfinity     the positive infinity value
     * @param searchLimit          the maximal cost of a path.
     *                             Set this value as small as you can, to prevent
     *                             long search times if the goal can not be reached.
     * @param costFunction         the cost function
     * @param sumFunction          the sum function for adding two cost values
     * @return
     */
    @Override
    public @Nullable VertexBackLink<V, C> search(
            @NonNull Iterable<V> startVertices,
            @NonNull Predicate<V> goalPredicate,
            @NonNull Function<V, Iterable<V>> nextVerticesFunction,
            @NonNull C zero,
            @NonNull C positiveInfinity,
            @NonNull C searchLimit,
            @NonNull BiFunction<V, V, C> costFunction,
            @NonNull BiFunction<C, C, C> sumFunction) {

        final C maxCost = searchLimit;

        // Priority queue: back-links by lower cost and shallower depth.
        //          Ordering by shallower depth prevents that the algorithm
        //          unnecessarily follows zero-length arrows.
        PriorityQueue<VertexBackLink<V, C>> queue = new PriorityQueue<>(
                Comparator.<VertexBackLink<V, C>, C>comparing(VertexBackLink::getCost).thenComparing(VertexBackLink::getDepth));

        // Map with best known costs from start to a specific vertex.
        // If an entry is missing, we assume infinity.
        Map<V, C> costMap = new HashMap<>();

        // Insert start itself in priority queue and initialize its cost to 0.
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

            for (V v : nextVerticesFunction.apply(u)) {
                C bestKnownCost = costMap.getOrDefault(v, positiveInfinity);
                C cost = sumFunction.apply(node.getCost(), costFunction.apply(u, v));

                // If there is a cheaper path to v through u.
                if (cost.compareTo(bestKnownCost) < 0 && cost.compareTo(maxCost) <= 0) {
                    // Update cost to v and add v again to the queue.
                    costMap.put(v, cost);
                    queue.add(new VertexBackLink<>(v, node, cost));
                }
            }
        }

        return null;
    }
}
