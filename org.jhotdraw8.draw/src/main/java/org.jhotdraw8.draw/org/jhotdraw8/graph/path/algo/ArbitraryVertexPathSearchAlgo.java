/*
 * @(#)AnyPathBuilder.java
 * Copyright © 2021 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph.path.algo;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.graph.path.backlink.VertexBackLink;
import org.jhotdraw8.util.function.AddToSet;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Builder for creating arbitrary paths from a directed graph.
 * <p>
 * The builder searches for paths using a breadth-first search.<br>
 * Returns the first path that it finds.<br>
 * Returns nothing if there is no path.
 *
 * @param <V> the vertex data type
 * @author Werner Randelshofer
 */
public class ArbitraryVertexPathSearchAlgo<V, C extends Number & Comparable<C>> implements VertexPathSearchAlgo<V, C> {


    @Override
    public @Nullable VertexBackLink<V, C> search(
            @NonNull Iterable<V> startVertices,
            @NonNull Predicate<V> goalPredicate,
            @NonNull Function<V, Iterable<V>> nextVerticesFunction, @NonNull C zero,
            @NonNull C positiveInfinity,
            @NonNull C maxCost,
            @NonNull BiFunction<V, V, C> costFunction,
            @NonNull BiFunction<C, C, C> sumFunction) {
        return search(startVertices, goalPredicate, new HashSet<V>()::add, maxCost, zero, nextVerticesFunction,
                costFunction, sumFunction);
    }

    protected @Nullable VertexBackLink<V, C> search(@NonNull Iterable<V> startVertices,
                                                    @NonNull Predicate<V> goal,
                                                    @NonNull AddToSet<V> visited,
                                                    @NonNull C maxCost,
                                                    @NonNull C zero,
                                                    @NonNull Function<V, Iterable<V>> nextNodesFunction,
                                                    @NonNull BiFunction<V, V, C> costFunction,
                                                    @NonNull BiFunction<C, C, C> sumFunction) {
        Queue<VertexBackLink<V, C>> queue = new ArrayDeque<>(16);
        for (V root : startVertices) {
            VertexBackLink<V, C> rootBackLink = new VertexBackLink<>(root, null, zero);
            if (visited.add(root)) {
                queue.add(rootBackLink);
            }
        }

        while (!queue.isEmpty()) {
            VertexBackLink<V, C> node = queue.remove();
            if (goal.test(node.getVertex())) {
                return node;
            }

            for (V next : nextNodesFunction.apply(node.getVertex())) {
                if (visited.add(next)) {
                    C cost = sumFunction.apply(node.getCost(), costFunction.apply(node.getVertex(), next));
                    if (cost.compareTo(maxCost) <= 0) {
                        VertexBackLink<V, C> backLink = new VertexBackLink<>(next, node, cost);
                        queue.add(backLink);
                    }
                }
            }
        }

        return null;
    }
}