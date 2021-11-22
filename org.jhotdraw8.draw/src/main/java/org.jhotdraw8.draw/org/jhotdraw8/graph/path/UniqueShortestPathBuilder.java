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
 * This {@link VertexSequenceBuilder} uses a variant of Diijkstra's
 * 'shortest path' algorithm.
 * <p>
 * This algorithm needs a cost function that returns values {@literal >= 0}
 * for all arrows.
 * <p>
 * If more than one shortest path is possible, the builder returns null.
 */
public class UniqueShortestPathBuilder<V, A, C extends Number & Comparable<C>> extends AbstractCostAndBackLinksSequenceBuilder<V, A, C> {

    /**
     * @param zero             the zero value, e.g. {@code 0}, {@code 0.0}.
     * @param positiveInfinity the positive infinity value or max value,
     *                         e.g. {@link Integer#MAX_VALUE},
     *                         {@link Double#POSITIVE_INFINITY}.
     * @param nextArcsFunction a function that given a vertex,
     *                         returns an {@link Iterable} for the next arcs
     *                         of that vertex.
     * @param costFunction     the cost function, must return values {@literal >= 0}.
     * @param sumFunction      the sum function, which adds two numbers,
     *                         e.g. {@link Integer#sum}, {@link Double#sum}.
     */
    public UniqueShortestPathBuilder(@NonNull C zero, @NonNull C positiveInfinity, @NonNull Function<V, Iterable<Arc<V, A>>> nextArcsFunction, @NonNull TriFunction<V, V, A, C> costFunction, @NonNull BiFunction<C, C, C> sumFunction) {
        super(zero, positiveInfinity, nextArcsFunction, costFunction, sumFunction);
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
        // Priority queue: back-links by lower cost and shallower depth.
        //          Ordering by shallower depth prevents that the algorithm
        //          unnecessarily follows zero-length arrows.
        PriorityQueue<BackLink<V, A, C>> queue = new PriorityQueue<>(
                Comparator.<BackLink<V, A, C>, C>comparing(BackLink::getCost).thenComparing(BackLink::getDepth)
        );

        // Map: key=vertex, value=number of shortest paths to the vertex
        Map<V, Integer> numPathsMap = new HashMap<>();

        // Map with best known costs from start to a vertex. If an entry is missing, we assume infinity.
        Map<V, C> costMap = new HashMap<>();

        // Insert start itself in priority queue and initialize its cost as 0,
        // and number of paths with 1.
        for (V start : starts) {
            queue.add(new BackLink<>(start, null, null, zero));
            numPathsMap.put(start, 1);
            costMap.put(start, zero);

        }

        // Loop until we have reached the goal, or queue is exhausted.
        BackLink<V, A, C> found = null;
        while (!queue.isEmpty()) {
            BackLink<V, A, C> node = queue.remove();
            final V u = node.getVertex();
            C costToU = node.getCost();
            if (goalPredicate.test(u)) {
                if (found == null) {
                    // We have found a shortest path for the first time.
                    // We can now limit the maxCost of further searches.
                    found = node;
                    maxCost = costToU;
                }
            }
            if (found != null && costToU.compareTo(maxCost) > 0) {
                // Once we have found a shortest path, we are only interested
                // in more paths with the same maxCost.
                continue;
            }

            for (Arc<V, A> arc : nextf.apply(u)) {
                V v = arc.getEnd();
                A a = arc.getData();
                C bestCostToV = costMap.getOrDefault(v, positiveInfinity);
                C costFromUToV = costf.apply(u, v, a);
                if (costFromUToV.compareTo(zero) < 0) {
                    throw new IllegalArgumentException("cost(" + costFromUToV + ") is negative for " + arc);
                }
                C costThroughU = sumf.apply(costToU, costFromUToV);

                // If there is a shorter path to v through u.
                if (costThroughU.compareTo(maxCost) <= 0) {
                    if (costThroughU.compareTo(bestCostToV) < 0) {
                        // Update cost to v, and number of paths to v.
                        costMap.put(v, costThroughU);
                        queue.add(new BackLink<>(v, a, node, costThroughU));
                        numPathsMap.put(v, numPathsMap.get(u));
                    } else if (costThroughU.compareTo(bestCostToV) == 0) {
                        // Path to v is not unique
                        numPathsMap.merge(v, 1, Integer::sum);
                    }
                }
            }
        }

        // The shortest path is only unique, if all vertices of the path
        return found != null && numPathsMap.get(found.getVertex()) == 1 ? found : null;
    }
}
