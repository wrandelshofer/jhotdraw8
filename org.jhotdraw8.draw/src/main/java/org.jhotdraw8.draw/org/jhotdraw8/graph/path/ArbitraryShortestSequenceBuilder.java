package org.jhotdraw8.graph.path;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.graph.Arc;
import org.jhotdraw8.util.TriFunction;

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
 */
public class ArbitraryShortestSequenceBuilder<V, A, C extends Number & Comparable<C>> extends AbstractShortestSequenceBuilder<V, A, C> {

    /**
     * @param zero
     * @param positiveInfinity
     * @param maxCost
     * @param nextArcsFunction
     * @param costFunction
     * @param sumFunction
     */
    public ArbitraryShortestSequenceBuilder(@NonNull C zero, @NonNull C positiveInfinity, @NonNull C maxCost, @NonNull Function<V, Iterable<Arc<V, A>>> nextArcsFunction, @NonNull TriFunction<V, V, A, C> costFunction, @NonNull BiFunction<C, C, C> sumFunction) {
        super(zero, positiveInfinity, maxCost, nextArcsFunction, costFunction, sumFunction);
    }

    @Override
    protected @Nullable BackLink<V, A, C> search(
            @NonNull Iterable<V> starts,
            @NonNull Predicate<V> goalPredicate,
            @NonNull C zero,
            @NonNull C positiveInfinity,
            @NonNull C maxCost,
            @NonNull Function<V, Iterable<Arc<V, A>>> nextf,
            @NonNull TriFunction<V, V, A, C> costf,
            @NonNull BiFunction<C, C, C> sumf) {

        // Priority queue: back-links with lower cost come first.
        PriorityQueue<BackLink<V, A, C>> queue = new PriorityQueue<>();

        // Map with best known costs from start to a specific vertex.
        // If an entry is missing, we assume infinity.
        Map<V, C> costMap = new HashMap<>();

        // Insert start itself in priority queue and initialize its cost to 0.
        for (V start : starts) {
            queue.add(new BackLink<>(start, null, null, zero));
            costMap.put(start, zero);
        }

        // Loop until we have reached the goal, or queue is exhausted.
        while (!queue.isEmpty()) {
            BackLink<V, A, C> node = queue.remove();
            final V u = node.getVertex();
            if (goalPredicate.test(u)) {
                return node;
            }
            C costToU = node.getCost();

            for (Arc<V, A> arc : nextf.apply(u)) {
                C bestKnownCostToV = costMap.getOrDefault(arc.getEnd(), positiveInfinity);
                C costThroughU = sumf.apply(costToU, costf.apply(u, arc.getEnd(), arc.getData()));

                // If there is a cheaper path to v through u.
                if (costThroughU.compareTo(bestKnownCostToV) < 0 && costThroughU.compareTo(maxCost) <= 0) {
                    // Update cost to v.
                    costMap.put(arc.getEnd(), costThroughU);
                    queue.add(new BackLink<>(arc.getEnd(), arc.getData(), node, costThroughU));
                }
            }
        }

        return null;
    }
}
