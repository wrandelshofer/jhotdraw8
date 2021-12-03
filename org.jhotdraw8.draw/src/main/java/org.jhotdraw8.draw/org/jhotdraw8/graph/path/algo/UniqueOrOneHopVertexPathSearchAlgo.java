/*
 * @(#)UniqueOrOneHopPathBuilder.java
 * Copyright © 2021 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph.path.algo;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.graph.path.backlink.VertexBackLinkWithCost;
import org.jhotdraw8.util.function.AddToSet;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Queue;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Builder for creating unique paths from a directed graph.
 * <p>
 * The builder searches for unique paths using a breadth-first search.<br>
 * Returns only a path if it is unique or if there is only one hop
 * from start to goal.
 *
 * @param <V> the vertex data type
 * @author Werner Randelshofer
 */
public class UniqueOrOneHopVertexPathSearchAlgo<V, C extends Number & Comparable<C>> implements VertexPathSearchAlgo<V, C> {


    @Override
    public @Nullable VertexBackLinkWithCost<V, C> search(@NonNull Iterable<V> startVertices, @NonNull Predicate<V> goalPredicate, @NonNull Function<V, Iterable<V>> nextVerticesFunction, int maxDepth, @NonNull C zero, @NonNull C positiveInfinity, @NonNull C costLimit, @NonNull BiFunction<V, V, C> costFunction, @NonNull BiFunction<C, C, C> sumFunction) {
        return search(startVertices, goalPredicate, nextVerticesFunction, new HashSet<>(16)::add, zero, costLimit, costFunction, sumFunction);
    }


    /**
     * Search engine method.
     *
     * @param startVertices        the set of start vertices
     * @param goalPredicate        the goal predicate
     * @param nextVerticesFunction the next arcs function
     * @param visited              the set of visited vertices (see {@link AddToSet})
     * @param costLimit            the algorithm-specific cost limit
     *                             Must be {@literal >= zero).
     * @param zero                 the zero cost value
     * @param costFunction         the cost function
     * @param sumFunction          the sum function for adding two cost values
     * @return on success: a back link, otherwise: null
     */
    public @Nullable VertexBackLinkWithCost<V, C> search(@NonNull Iterable<V> startVertices,
                                                         @NonNull Predicate<V> goalPredicate,
                                                         @NonNull Function<V, Iterable<V>> nextVerticesFunction,
                                                         @NonNull AddToSet<V> visited,
                                                         @NonNull C zero,
                                                         @NonNull C costLimit,
                                                         @NonNull BiFunction<V, V, C> costFunction,
                                                         @NonNull BiFunction<C, C, C> sumFunction) {

        Queue<VertexBackLinkWithCost<V, C>> queue = new ArrayDeque<>(16);

        for (V start : startVertices) {
            VertexBackLinkWithCost<V, C> rootBackLink = new VertexBackLinkWithCost<>(start, null, zero);
            if (visited.add(start)) {
                queue.add(rootBackLink);
            }
        }

        VertexBackLinkWithCost<V, C> found = null;
        Set<V> nonUnique = new LinkedHashSet<>();
        while (!queue.isEmpty()) {
            VertexBackLinkWithCost<V, C> node = queue.remove();
            if (goalPredicate.test(node.getVertex())) {
                if (found != null) {
                    return null;// path is not unique!
                }
                if (node.getDepth() <= 1) {
                    return node; // Up to one hop is considered unique.
                }
                found = node;
            }

            for (V next : nextVerticesFunction.apply(node.getVertex())) {
                C cost = sumFunction.apply(node.getCost(), costFunction.apply(node.getVertex(), next));
                if (cost.compareTo(costLimit) <= 0) {
                    if (visited.add(next)) {
                        VertexBackLinkWithCost<V, C> backLink = new VertexBackLinkWithCost<V, C>(next, node, cost);
                        queue.add(backLink);
                    } else {
                        nonUnique.add(next);
                    }
                }
            }
        }

        for (VertexBackLinkWithCost<V, C> node = found; node != null; node = node.getParent()) {
            if (nonUnique.contains(node.getVertex())) {
                // path is not unique!
                return null;
            }
        }
        return found;
    }

}
