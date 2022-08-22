/*
 * @(#)GloballyUniqueOnDagVertexPathSearchAlgo.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph.path.algo;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.graph.path.backlink.VertexBackLink;
import org.jhotdraw8.graph.path.backlink.VertexBackLinkWithCost;
import org.jhotdraw8.util.function.AddToSet;

import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Queue;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * See {@link GloballyUniqueOnDagArcPathSearchAlgo} for a description of this
 * algorithm.
 *
 * @param <V> the vertex data type
 * @param <C> the cost number type
 */
public class GloballyUniqueOnDagVertexPathSearchAlgo<V, C extends Number & Comparable<C>> implements VertexPathSearchAlgo<V, C> {
    public GloballyUniqueOnDagVertexPathSearchAlgo() {
    }

    /**
     * {@inheritDoc}
     *
     * @param startVertices        the set of start vertices
     * @param goalPredicate        the goal predicate
     * @param nextVerticesFunction the next vertices function
     * @param maxDepth             the maximal depth (inclusive) of the search
     *                             Must be {@literal >= 0}.
     * @param zero                 the zero cost value
     * @param costLimit            the cost limit is <b>ignored</b>
     * @param costFunction         the cost function
     * @param sumFunction          the sum function for adding two cost values
     * @param visited
     * @return on success: a back link, otherwise: null
     */
    @Override
    public @Nullable VertexBackLinkWithCost<V, C> search(
            @NonNull Iterable<V> startVertices,
            @NonNull Predicate<V> goalPredicate,
            @NonNull Function<V, Iterable<V>> nextVerticesFunction,
            int maxDepth,
            @NonNull C zero,
            @NonNull C costLimit,
            @NonNull BiFunction<V, V, C> costFunction,
            @NonNull BiFunction<C, C, C> sumFunction, @NonNull AddToSet<V> visited) {
        AlgoArguments.checkZero(zero);
        return VertexBackLink.toVertexBackLinkWithCost(
                search(startVertices, goalPredicate, nextVerticesFunction, maxDepth),
                zero, costFunction, sumFunction);
    }

    /**
     * Search engine method.
     *
     * @param startVertices        the set of start vertices
     * @param goalPredicate        the goal predicate
     * @param nextVerticesFunction the next vertices function
     * @param maxDepth             the maximal depth (inclusive) of the search.
     * @return on success: a back link, otherwise: null
     */
    public @Nullable VertexBackLink<V> search(
            @NonNull Iterable<V> startVertices,
            @NonNull Predicate<V> goalPredicate,
            @NonNull Function<V, Iterable<V>> nextVerticesFunction,
            int maxDepth) {
        AlgoArguments.checkMaxDepth(maxDepth);

        Queue<VertexBackLink<V>> queue = new ArrayDeque<>(16);
        Map<V, Integer> visitedCount = new LinkedHashMap<>(16);
        for (V s : startVertices) {
            if (visitedCount.put(s, 1) == null) {
                queue.add(new VertexBackLink<>(s, null));
            }
        }

        VertexBackLink<V> found = null;
        while (!queue.isEmpty()) {
            VertexBackLink<V> u = queue.remove();
            if (goalPredicate.test(u.getVertex())) {
                if (found != null) {
                    return null;// path is not unique!
                }
                found = u;
            }
            if (u.getDepth() < maxDepth) {
                for (V v : nextVerticesFunction.apply(u.getVertex())) {
                    if (visitedCount.merge(v, 1, Integer::sum) == 1) {
                        queue.add(new VertexBackLink<>(v, u));
                    }
                }
            }
        }

        // Check if any of the preceding nodes has a non-unique path
        for (VertexBackLink<V> node = found; node != null; node = node.getParent()) {
            if (visitedCount.get(node.getVertex()) > 1) {
                return null;// path is not unique!
            }
        }
        return found;
    }
}


