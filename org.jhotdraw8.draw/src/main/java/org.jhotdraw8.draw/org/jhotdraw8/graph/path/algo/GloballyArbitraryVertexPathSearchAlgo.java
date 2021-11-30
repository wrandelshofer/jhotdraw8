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
 * <p>
 * Expected run time:
 * <dl>
 *     <dt>When a path can be found</dt><dd>less or equal O( |E| + |V| ) within max depth</dd>
 *     <dt>When no path can be found</dt><dd>exactly O( |E| + |V| ) within max depth</dd>
 * </dl>
 *
 * @param <V> the vertex data type
 */
public class GloballyArbitraryVertexPathSearchAlgo<V, C extends Number & Comparable<C>> implements VertexPathSearchAlgo<V, C> {


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
     * @return
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
                new HashSet<V>()::add, searchLimit.intValue(), zero,
                costFunction, sumFunction);
    }

    /**
     * Search engine method.
     *
     * @param startVertices    the set of start vertices
     * @param goalPredicate    the goal predicate
     * @param nextArcsFunction the next arcs function
     * @param visited          the set of visited vertices (see {@link AddToSet})
     * @param maxDepth          the maximal depth
     *                         Set this value as small as you can, to prevent
     *                         long search times if the goal can not be reached.
     * @return on success: a back link, otherwise: null
     */
    protected @Nullable VertexBackLink<V, C> search(@NonNull Iterable<V> startVertices,
                                                    @NonNull Predicate<V> goalPredicate,
                                                    @NonNull Function<V, Iterable<V>> nextArcsFunction,
                                                    @NonNull AddToSet<V> visited,
                                                    @NonNull int maxDepth,
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

            if (node.getDepth() < maxDepth) {
                for (V next : nextArcsFunction.apply(node.getVertex())) {
                    if (visited.add(next)) {
                        VertexBackLink<V, C> backLink = new VertexBackLink<>(next, node,
                                sumFunction.apply(node.getCost(), costFunction.apply(node.getVertex(), next)));
                        queue.add(backLink);
                    }
                }
            }
        }

        return null;
    }
}