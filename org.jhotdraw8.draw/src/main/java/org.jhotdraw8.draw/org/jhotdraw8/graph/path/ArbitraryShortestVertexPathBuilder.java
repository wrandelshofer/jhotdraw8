package org.jhotdraw8.graph.path;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * This {@link VertexSequenceBuilder} uses Diijkstra's 'shortest path' algorithm.
 * If more than one shortest path is possible, the builder returns an arbitrary
 * one.
 * <p>
 * The provided cost function must always return a value &gt;= 0.
 * <p>
 * Invariants of a sequences returned by the {@code findSequence} methods
 * (excluding the {@code findSequenceOverWaypoints} methods):
 * <ul>
 *     <li>A sequence is a path.</li>
 *     <li>A sequence is directed. That is, the sequence respects
 *     the direction of the arrows in the graph.</li>
 *     <li>A sequence does not include zero-weight arrows that form cycles.</li>
 *     <li>A sequences has minimal cost and has minimal number of arrows/vertices.</li>
 * </ul>
 */
public class ArbitraryShortestVertexPathBuilder<V, C extends Number & Comparable<C>> extends AbstractCostAndBackLinksVertexSequenceBuilder<V, C> {

    /**
     * Creates a new instance.
     *
     * @param zero                 the zero value, e.g. {@code 0}, {@code 0.0}.
     * @param positiveInfinity     the positive infinity value or max value,
     *                             e.g. {@link Integer#MAX_VALUE},
     *                             {@link Double#POSITIVE_INFINITY}.
     * @param maxCost              the maximal cost of a sequence,
     *                             e.g. {@link Integer#MAX_VALUE}, {@link Double#MAX_VALUE}.
     * @param nextVerticesFunction a function that given a vertex,
     *                             returns an {@link Iterable} for the next vertices
     *                             of that vertex.
     * @param costFunction         the cost function, all costs must be &gt;= 0.
     * @param sumFunction          the sum function, which adds two numbers,
     *                             e.g. {@link Integer#sum}, {@link Double#sum}.
     */
    public ArbitraryShortestVertexPathBuilder(@NonNull C zero, @NonNull C positiveInfinity, @NonNull C maxCost, @NonNull Function<V, Iterable<V>> nextVerticesFunction, @NonNull BiFunction<V, V, C> costFunction, @NonNull BiFunction<C, C, C> sumFunction) {
        super(zero, positiveInfinity, maxCost, nextVerticesFunction, costFunction, sumFunction);
    }


    @Override
    protected @Nullable BackLink<V, C> search(
            @NonNull Iterable<V> starts,
            @NonNull Predicate<V> goalPredicate,
            @NonNull C zero,
            @NonNull C positiveInfinity,
            @NonNull C maxCost,
            @NonNull Function<V, Iterable<V>> nextf,
            @NonNull BiFunction<V, V, C> costf,
            @NonNull BiFunction<C, C, C> sumf) {
        // Priority queue: back-links with shortest distance from start come first.
        PriorityQueue<BackLink<V, C>> queue = new PriorityQueue<>(
                Comparator.<BackLink<V, C>, C>comparing(BackLink::getCost).thenComparing(BackLink::getDepth));

        // Map with best known costs from start to a specific vertex. If an entry is missing, we assume infinity.
        Map<V, C> costMap = new HashMap<>();

        // Insert start itself in priority queue and initialize its cost as 0.
        for (V start : starts) {
            queue.add(new BackLink<>(start, null, zero));
            costMap.put(start, zero);
        }

        // Loop until we have reached the goal, or queue is exhausted.
        while (!queue.isEmpty()) {
            BackLink<V, C> node = queue.remove();
            final V u = node.getVertex();
            if (goalPredicate.test(u)) {
                return node;
            }
            C costToU = node.getCost();

            for (V v : nextf.apply(u)) {
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
                    queue.add(new BackLink<>(v, node, costThroughU));
                }
            }
        }

        return null;
    }
}
