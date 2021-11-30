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
public class GloballyUniqueVertexPathSearchAlgo<V, C extends Number & Comparable<C>> implements VertexPathSearchAlgo<V, C> {


    /**
     * {@inheritDoc}
     *
     * @param startVertices        the set of start vertices
     * @param goalPredicate        the goal predicate
     * @param nextVerticesFunction the next vertices function
     * @param zero                 the zero cost value
     * @param positiveInfinity     the positive infinity value
     * @param searchLimit          the maximal depth (inclusive) of a back link.
     *                             Set this value as small as you can, to prevent
     *                             long search times if the goal can not be reached.
     * @param costFunction         the cost function
     * @param sumFunction          the sum function for adding two cost values
     * @return on success: a back link, otherwise: null
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
        return search(startVertices, goalPredicate, nextVerticesFunction,
                zero, searchLimit.intValue(), costFunction, sumFunction);
    }

    /**
     * Search engine method.
     *
     * @param startVertices        the set of start vertices
     * @param goalPredicate        the goal predicate
     * @param nextVerticesFunction the next vertices function
     * @param zero                 the zero cost value
     * @param maxDepth             the maximal depth (inclusive) of a back link.
     *                             Set this value as small as you can, to prevent
     *                             long search times if the goal can not be reached.
     * @param costFunction         the cost function
     * @param sumFunction          the sum function for adding two cost values
     * @return on success: a back link, otherwise: null
     */
    public @Nullable VertexBackLink<V, C> search(
            @NonNull Iterable<V> startVertices,
            @NonNull Predicate<V> goalPredicate,
            @NonNull Function<V, Iterable<V>> nextVerticesFunction,
            @NonNull C zero,
            int maxDepth,
            @NonNull BiFunction<V, V, C> costFunction,
            @NonNull BiFunction<C, C, C> sumFunction) {

        Queue<VertexBackLink<V, C>> queue = new ArrayDeque<>(16);
        Map<V, Integer> visitedCount = new LinkedHashMap<>(16);
        for (V start : startVertices) {
            VertexBackLink<V, C> rootBackLink = new VertexBackLink<>(start, null, zero);
            if (visitedCount.put(start, 1) == null) {
                queue.add(rootBackLink);
            }
        }

        VertexBackLink<V, C> found = null;
        while (!queue.isEmpty()) {
            VertexBackLink<V, C> node = queue.remove();
            if (goalPredicate.test(node.getVertex())) {
                if (found != null) {
                    return null;// path is not unique!
                }
                found = node;
            }
            if (node.getDepth() < maxDepth) {
                for (V next : nextVerticesFunction.apply(node.getVertex())) {
                    if (visitedCount.merge(next, 1, Integer::sum) == 1) {
                        VertexBackLink<V, C> backLink = new VertexBackLink<V, C>(next, node,
                                sumFunction.apply(node.getCost(), costFunction.apply(node.getVertex(), next)));
                        queue.add(backLink);
                    }
                }
            }
        }

        // Check if any of the preceding nodes has a non-unique path
        for (VertexBackLink<V, C> node = found; node != null; node = node.getParent()) {
            if (visitedCount.get(node.getVertex()) > 1) {
                return null;// path is not unique!
            }
        }
        return found;
    }
}


