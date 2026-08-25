/*
 * @(#)AnyVertexPathSearchAlgo.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph.path.algo;

import org.jhotdraw8.graph.algo.AddToSet;
import org.jhotdraw8.graph.path.backlink.VertexBackLink;
import org.jhotdraw8.graph.path.backlink.VertexBackLinkWithCost;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntBiFunction;

/// See [AnyArcPathSearchAlgo] for a description of this
/// algorithm.
///
/// @param <V> the vertex data type
/// @param <C> the cost number type
public class AnyVertexPathSearchAlgo<V> implements VertexPathSearchAlgo<V> {
    public AnyVertexPathSearchAlgo() {
    }

    /// {@inheritDoc}
    ///
    /// @param startVertices        the set of start vertices
    /// @param goalPredicate        the goal predicate
    /// @param nextVerticesFunction the next vertices function
    /// @param maxDepth             the maximal depth (inclusive) of the search
    ///                             Must be {@literal >= 0}.
    /// @param costLimit            the cost limit is **ignored**
    /// @param costFunction         the cost function
    /// @param visited
    /// @return
    @Override
    public @Nullable VertexBackLinkWithCost<V> search(
            Iterable<V> startVertices,
            Predicate<V> goalPredicate,
            Function<V, Iterable<V>> nextVerticesFunction,
            int maxDepth,
            int costLimit,
            ToIntBiFunction<V, V> costFunction,
            AddToSet<V> visited) {
        return VertexBackLink.toVertexBackLinkWithCost(
                search(startVertices, goalPredicate, nextVerticesFunction,
                        new HashSet<V>()::add,
                        maxDepth),
                costFunction);
    }

    /// Search engine method.
    ///
    /// @param startVertices        the set of start vertices
    /// @param goalPredicate        the goal predicate
    /// @param nextVerticesFunction the next vertices function
    /// @param visited              the set of visited vertices (see [AddToSet])
    /// @param maxDepth             the maximal depth
    /// @return on success: a back link, otherwise: null
    protected @Nullable VertexBackLink<V> search(Iterable<V> startVertices,
                                                 Predicate<V> goalPredicate,
                                                 Function<V, Iterable<V>> nextVerticesFunction,
                                                 AddToSet<V> visited,
                                                 int maxDepth) {
        AlgoArguments.checkMaxDepth(maxDepth);

        Queue<VertexBackLink<V>> queue = new ArrayDeque<>(16);
        for (V s : startVertices) {
            if (visited.add(s)) {
                queue.add(new VertexBackLink<>(s, null));
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
                        queue.add(new VertexBackLink<>(v, u));
                    }
                }
            }
        }

        return null;
    }
}