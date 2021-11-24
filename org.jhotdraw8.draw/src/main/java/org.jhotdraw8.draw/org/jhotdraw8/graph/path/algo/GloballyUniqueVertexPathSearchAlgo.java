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
 * Builder for creating unique paths from a directed graph.
 * <p>
 * The builder searches for unique paths using a breadth-first search.<br>
 * Returns only a path if it is unique.
 *
 * @param <V> the vertex data type
 * @author Werner Randelshofer
 */
public class GloballyUniqueVertexPathSearchAlgo<V, C extends Number & Comparable<C>> implements VertexPathSearchAlgo<V, C> {


    @Override
    public @Nullable VertexBackLink<V, C> search(@NonNull Iterable<V> startVertices, @NonNull Predicate<V> goalPredicate, @NonNull Function<V, Iterable<V>> nextVerticesFunction, @NonNull C zero, @NonNull C positiveInfinity, @NonNull C maxCost, @NonNull BiFunction<V, V, C> costFunction, @NonNull BiFunction<C, C, C> sumFunction) {

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
            for (V next : nextVerticesFunction.apply(node.getVertex())) {
                if (visitedCount.merge(next, 1, Integer::sum) == 1) {
                    if (node.getCost().compareTo(maxCost) <= 0) {
                        C cost = sumFunction.apply(node.getCost(), costFunction.apply(node.getVertex(), next));
                        VertexBackLink<V, C> backLink = new VertexBackLink<V, C>(next, node, cost);
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


