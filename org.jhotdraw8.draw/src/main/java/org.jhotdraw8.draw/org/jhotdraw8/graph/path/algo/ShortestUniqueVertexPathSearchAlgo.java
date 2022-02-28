package org.jhotdraw8.graph.path.algo;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.graph.path.backlink.VertexBackLinkWithCost;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * See {@link ShortestUniqueArcPathSearchAlgo} for a description of this
 * algorithm.
 *
 * @param <V> the vertex data type
 * @param <C> the cost number type
 */
public class ShortestUniqueVertexPathSearchAlgo<V, C extends Number & Comparable<C>> implements VertexPathSearchAlgo<V, C> {
    public ShortestUniqueVertexPathSearchAlgo() {
    }

    /**
     * {@inheritDoc}
     *  @param startVertices    the set of start vertices
     * @param goalPredicate    the goal predicate
     * @param nextVertices     the next vertices function
     * @param maxDepth         the maximal depth (inclusive) of the search
     *                         Must be {@literal >= 0}.
     * @param zero             the zero cost value
     * @param costLimit        the maximal cost (inclusive) of a path.
     *                         Must be {@literal >= zero).
     * @param costFunction     the cost function<br>
     *                         The cost must be {@literal > 0} if the graph
     *                         has cycles.<br>
     *                         The cost must be {@literal >= 0} if the graph
     *                         is acyclic.
     * @param sumFunction      the sum function for adding two cost values
     * @return on success: a back link, otherwise: null
     */
    @Override
    public @Nullable VertexBackLinkWithCost<V, C> search(
            final @NonNull Iterable<V> startVertices,
            final @NonNull Predicate<V> goalPredicate,
            final @NonNull Function<V, Iterable<V>> nextVertices,
            int maxDepth,
            final @NonNull C zero,
            final @NonNull C costLimit,
            final @NonNull BiFunction<V, V, C> costFunction,
            final @NonNull BiFunction<C, C, C> sumFunction) {

        AlgoArguments.checkMaxDepthMaxCostArguments(maxDepth, zero, costLimit);
        CheckedNonNegativeVertexCostFunction<V, C> costf = new CheckedNonNegativeVertexCostFunction<>(zero, costFunction);

        // Priority queue: back-links by lower cost and shallower depth.
        //          Ordering by depth prevents that the algorithm
        //          unnecessarily follows zero-length arrows.
        PriorityQueue<VertexBackLinkWithCost<V, C>> queue = new PriorityQueue<>(
                Comparator.<VertexBackLinkWithCost<V, C>, C>comparing(VertexBackLinkWithCost::getCost).thenComparing(VertexBackLinkWithCost::getDepth)
        );

        // Map with best known costs from start to a vertex and with the number
        // of times we have reached the map.
        // If an entry is missing, we assume infinity.
        Map<V, CostData<C>> costMap = new HashMap<>();

        CostData<C> infiniteCost = new CostData<>(null, 0);

        // Insert start itself in priority queue and initialize its cost as 0,
        // and number of paths with 1.
        for (V start : startVertices) {
            queue.add(new VertexBackLinkWithCost<>(start, null, zero));
            costMap.put(start, new CostData<>(zero, 1));
        }

        // Loop until we have reached the goal, or queue is exhausted.
        C maxCost = costLimit;
        VertexBackLinkWithCost<V, C> found = null;
        while (!queue.isEmpty()) {
            VertexBackLinkWithCost<V, C> u = queue.remove();
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
                for (V v : nextVertices.apply(u.getVertex())) {
                    CostData<C> costDataV = costMap.getOrDefault(v, infiniteCost);
                    final C bestKnownCost = costDataV.getCost();
                    C cost = sumFunction.apply(costToU, costf.apply(u.getVertex(), v));

                    // If there is a shorter path to v through u.
                    if (cost.compareTo(maxCost) <= 0) {
                        final int compare = bestKnownCost == null ? -1 : cost.compareTo(bestKnownCost);
                        if (compare < 0) {
                            // Update cost data to v.
                            costMap.put(v, new CostData<>(cost, 1));
                            queue.add(new VertexBackLinkWithCost<>(v, u, cost));
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
        for (VertexBackLinkWithCost<V, C> node = found; node != null; node = node.getParent()) {
            if (costMap.get(node.getVertex()).getVisiCount() != 1) {
                return null;
            }
        }

        return found;
    }
}
