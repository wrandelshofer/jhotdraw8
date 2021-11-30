/*
 * @(#)UniquePathBuilder.java
 * Copyright © 2021 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph.path.algo;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.graph.path.backlink.VertexBackLink;

import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Queue;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Searches a globally unique vertex path from a set of start vertices to a
 * set of goal vertices using a breadth-first search algorithm.
 * <p>
 * Uniqueness is global up to (inclusive) the specified maximal cost.
 * <p>
 * Performance characteristics:
 * <dl>
 *     <dt>When a path can be found</dt><dd>exactly O( |A| + |V| ) within max depth</dd>
 *     <dt>When no path can be found</dt><dd>exactly O( |A| + |V| ) within max depth</dd>
 * </dl>
 *
 * @param <V> the vertex data type
 */
public class GloballyUniqueVertexPathSearchAlgo<V> implements VertexPathSearchAlgo<V, Integer> {


    /**
     * {@inheritDoc}
     *
     * @param startVertices        the set of start vertices
     * @param goalPredicate        the goal predicate
     * @param nextVerticesFunction the next vertices function
     * @param zero                 the zero cost value
     * @param positiveInfinity     the positive infinity value
     * @param searchLimit          the maximal depth of a back link.
     *                             Set this value as small as you can, to prevent
     *                             long search times if the goal can not be reached.
     * @param costFunction         the cost function
     * @param sumFunction          the sum function for adding two cost values
     * @return
     */
    @Override
    public @Nullable VertexBackLink<V, Integer> search(
            @NonNull Iterable<V> startVertices,
            @NonNull Predicate<V> goalPredicate,
            @NonNull Function<V, Iterable<V>> nextVerticesFunction,
            @NonNull Integer zero,
            @NonNull Integer positiveInfinity,
            @NonNull Integer searchLimit,
            @NonNull BiFunction<V, V, Integer> costFunction,
            @NonNull BiFunction<Integer, Integer, Integer> sumFunction) {
        return search(startVertices, goalPredicate, nextVerticesFunction, searchLimit);
    }

    public @Nullable VertexBackLink<V, Integer> search(
            @NonNull Iterable<V> startVertices,
            @NonNull Predicate<V> goalPredicate,
            @NonNull Function<V, Iterable<V>> nextVerticesFunction,
            int maxDepth) {

        Queue<VertexBackLink<V, Integer>> queue = new ArrayDeque<>(16);
        Map<V, Integer> visitedCount = new LinkedHashMap<>(16);
        for (V start : startVertices) {
            VertexBackLink<V, Integer> rootBackLink = new VertexBackLink<>(start, null, 0);
            if (visitedCount.put(start, 1) == null) {
                queue.add(rootBackLink);
            }
        }

        VertexBackLink<V, Integer> found = null;
        while (!queue.isEmpty()) {
            VertexBackLink<V, Integer> node = queue.remove();
            if (goalPredicate.test(node.getVertex())) {
                if (found != null) {
                    return null;// path is not unique!
                }
                found = node;
            }
            if (node.getDepth() < maxDepth) {
                for (V next : nextVerticesFunction.apply(node.getVertex())) {
                    if (visitedCount.merge(next, 1, Integer::sum) == 1) {
                        VertexBackLink<V, Integer> backLink = new VertexBackLink<V, Integer>(next, node, 0);
                        queue.add(backLink);
                    }
                }
            }
        }

        // Check if any of the preceding nodes has a non-unique path
        for (VertexBackLink<V, Integer> node = found; node != null; node = node.getParent()) {
            if (visitedCount.get(node.getVertex()) > 1) {
                return null;// path is not unique!
            }
        }
        return found;
    }
}


