package org.jhotdraw8.graph.path;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.graph.Arc;
import org.jhotdraw8.util.TriFunction;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * This sequence builder uses Diijkstra's 'shortest path' algorithm.
 * If more than one shortest path is possible, the builder returns an arbitrary
 * one.
 * <p>
 * This algorithm needs a cost function that returns values {@literal >= 0}
 * for all arrows.
 * <p>
 * References:
 * <dl>
 * <dt>Esger W. Dijkstra (1959), A note on two problems in connexion with graphs,
 * Problem 2.
 * </dt>
 * <dd><a href="https://www-m3.ma.tum.de/twiki/pub/MN0506/WebHome/dijkstra.pdf">tum.de</a></dd>
 * </dl>
 */
public class ArbitraryShortestPathSearchAlgo<V, A, C extends Number & Comparable<C>> implements PathSearchAlgo<V, A, C> {


    @Override
    public @Nullable ArcBackLink<V, A, C> search(
            @NonNull Iterable<V> starts,
            @NonNull Predicate<V> goalPredicate,
            @NonNull C zero,
            @NonNull C positiveInfinity,
            @NonNull C maxCost,
            @NonNull Function<V, Iterable<Arc<V, A>>> nextf,
            @NonNull TriFunction<V, V, A, C> costf,
            @NonNull BiFunction<C, C, C> sumf) {

        // Priority queue: back-links by lower cost and shallower depth.
        //          Ordering by shallower depth prevents that the algorithm
        //          unnecessarily follows zero-length arrows.
        PriorityQueue<ArcBackLink<V, A, C>> queue = new PriorityQueue<ArcBackLink<V, A, C>>(
                Comparator.<ArcBackLink<V, A, C>, C>comparing(ArcBackLink::getCost).thenComparing(ArcBackLink::getDepth)
        );

        // Map with best known costs from start to a specific vertex.
        // If an entry is missing, we assume infinity.
        Map<V, C> costMap = new HashMap<>();

        // Insert start itself in priority queue and initialize its cost to 0.
        for (V start : starts) {
            queue.add(new ArcBackLink<>(start, null, null, zero));
            costMap.put(start, zero);
        }

        // Loop until we have reached the goal, or queue is exhausted.
        while (!queue.isEmpty()) {
            ArcBackLink<V, A, C> node = queue.remove();
            final V u = node.getVertex();
            if (goalPredicate.test(u)) {
                return node;
            }
            C costToU = node.getCost();

            for (Arc<V, A> arc : nextf.apply(u)) {
                V v = arc.getEnd();
                C bestKnownCostToV = costMap.getOrDefault(v, positiveInfinity);
                C costFromUToV = costf.apply(u, v, arc.getData());
                if (costFromUToV.compareTo(zero) < 0) {
                    throw new IllegalArgumentException("cost(" + costFromUToV + ") is negative for " + arc);
                }
                C costThroughU = sumf.apply(costToU, costFromUToV);

                // If there is a cheaper path to v through u.
                if (costThroughU.compareTo(bestKnownCostToV) < 0 && costThroughU.compareTo(maxCost) <= 0) {
                    // Update cost to v.
                    costMap.put(v, costThroughU);
                    queue.add(new ArcBackLink<>(v, arc.getData(), node, costThroughU));
                }
            }
        }

        return null;
    }
}
