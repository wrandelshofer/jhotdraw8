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
 * Searches an arbitrary vertex path from a set of start vertices to a
 * set of goal vertices using a breadth-first search algorithm.
 *
 * @param <V> the vertex data type
 * @param <C> the cost number type
 */
public class ArbitraryVertexPathSearchAlgo<V, C extends Number & Comparable<C>> implements VertexPathSearchAlgo<V, C> {


    @Override
    public @Nullable VertexBackLink<V, C> search(
            @NonNull Iterable<V> startVertices,
            @NonNull Predicate<V> goalPredicate,
            @NonNull Function<V, Iterable<V>> nextVerticesFunction,
            @NonNull C zero,
            @NonNull C positiveInfinity,
            @NonNull C maxCost,
            @NonNull BiFunction<V, V, C> costFunction,
            @NonNull BiFunction<C, C, C> sumFunction) {
        return search(startVertices, goalPredicate, nextVerticesFunction,
                new HashSet<V>()::add, maxCost, zero,
                costFunction, sumFunction);
    }

    /**
     * Search engine method.
     *
     * @param startVertices    the set of start vertices
     * @param goalPredicate    the goal predicate
     * @param nextArcsFunction the next arcs function
     * @param visited          the set of visited vertices (see {@link AddToSet})
     * @param maxCost          the maximal cost (inclusive) that a sequence may have
     * @param zero             the zero cost value
     * @param costFunction     the cost function
     * @param sumFunction      the sum function for adding two cost values
     * @return on success: a back link, otherwise: null
     */
    protected @Nullable VertexBackLink<V, C> search(@NonNull Iterable<V> startVertices,
                                                    @NonNull Predicate<V> goalPredicate,
                                                    @NonNull Function<V, Iterable<V>> nextArcsFunction,
                                                    @NonNull AddToSet<V> visited,
                                                    @NonNull C maxCost,
                                                    @NonNull C zero,
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
            if (goalPredicate.test(node.getVertex())) {
                return node;
            }

            for (V next : nextArcsFunction.apply(node.getVertex())) {
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