/*
 * @(#)AnyVertexPathSearchAlgo.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph.path.algo;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.graph.algo.AddToSet;
import org.jhotdraw8.graph.path.backlink.VertexBackLink;
import org.jhotdraw8.graph.path.backlink.VertexBackLinkWithCost;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * See {@link AnyArcPathSearchAlgo} for a description of this
 * algorithm.
 *
 * @param <V> the vertex data type
 * @param <C> the cost number type
 */
public class AnyVertexPathSearchAlgo<V, C extends Number & Comparable<C>> implements VertexPathSearchAlgo<V, C> {
    public AnyVertexPathSearchAlgo() {
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
     * @return
     */
    @Override
    public @Nullable VertexBackLinkWithCost<V, C> search(
            @NonNull Iterable<V> startVertices,
            @NonNull Predicate<V> goalPredicate,
            @NonNull Function<V, Iterable<V>> nextVerticesFunction,
            int maxDepth, @NonNull C zero,
            @NonNull C costLimit,
            @NonNull BiFunction<V, V, C> costFunction,
            @NonNull BiFunction<C, C, C> sumFunction, @NonNull AddToSet<V> visited) {
        AlgoArguments.checkZero(zero);
        return VertexBackLink.toVertexBackLinkWithCost(
                search(startVertices, goalPredicate, nextVerticesFunction,
                        new HashSet<V>()::add,
                        maxDepth),
                zero,
                costFunction, sumFunction);
    }

    /**
     * Search engine method.
     *
     * @param startVertices        the set of start vertices
     * @param goalPredicate        the goal predicate
     * @param nextVerticesFunction the next vertices function
     * @param visited              the set of visited vertices (see {@link AddToSet})
     * @param maxDepth             the maximal depth
     * @return on success: a back link, otherwise: null
     */
    protected @Nullable VertexBackLink<V> search(@NonNull Iterable<V> startVertices,
                                                 @NonNull Predicate<V> goalPredicate,
                                                 @NonNull Function<V, Iterable<V>> nextVerticesFunction,
                                                 @NonNull AddToSet<V> visited,
                                                 int maxDepth) {
        AlgoArguments.checkMaxDepth(maxDepth);

        Queue<VertexBackLink<V>> queue = new ArrayDeque<>(16);
        for (V s : startVertices) {
            if (visited.add(s)) {
                queue.add(new VertexBackLink<V>(s, null));
            }
        }

        while (!queue.isEmpty()) {
            VertexBackLink<V> u = queue.remove();
            if (goalPredicate.test(u.getVertex())) {
                return u;
            }

            if (u.getDepth() < maxDepth) {
                for (V v : nextVerticesFunction.apply(u.getVertex())) {
                    if (visited.add(v)) {
                        queue.add(new VertexBackLink<V>(v, u));
                    }
                }
            }
        }

        return null;
    }
}