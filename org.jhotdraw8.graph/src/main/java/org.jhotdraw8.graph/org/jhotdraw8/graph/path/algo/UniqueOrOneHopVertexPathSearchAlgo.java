/*
 * @(#)UniqueOrOneHopVertexPathSearchAlgo.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph.path.algo;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.graph.algo.AddToSet;
import org.jhotdraw8.graph.path.backlink.VertexBackLinkWithCost;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.SequencedSet;
import java.util.Queue;
import java.util.SequencedSet;
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
 * @param <C> the cost number type
 * @author Werner Randelshofer
 */
public class UniqueOrOneHopVertexPathSearchAlgo<V, C extends Number & Comparable<C>> implements VertexPathSearchAlgo<V, C> {
    public UniqueOrOneHopVertexPathSearchAlgo() {
    }

    @Override
    public @Nullable VertexBackLinkWithCost<V, C> search(@NonNull Iterable<V> startVertices, @NonNull Predicate<V> goalPredicate, @NonNull Function<V, Iterable<V>> nextVerticesFunction, int maxDepth, @NonNull C zero, @NonNull C costLimit, @NonNull BiFunction<V, V, C> costFunction, @NonNull BiFunction<C, C, C> sumFunction, @NonNull AddToSet<V> visited) {
        return search(startVertices, goalPredicate, nextVerticesFunction, new HashSet<>(16)::add, maxDepth, zero, costFunction, sumFunction);
    }


    /**
     * Search engine method.
     *
     * @param startVertices        the set of start vertices
     * @param goalPredicate        the goal predicate
     * @param nextVerticesFunction the next arcs function
     * @param visited              the set of visited vertices (see {@link AddToSet})
     * @param zero                 the zero cost value
     * @param costFunction         the cost function
     * @param sumFunction          the sum function for adding two cost values
     * @return on success: a back link, otherwise: null
     */
    public @Nullable VertexBackLinkWithCost<V, C> search(@NonNull Iterable<V> startVertices,
                                                         @NonNull Predicate<V> goalPredicate,
                                                         @NonNull Function<V, Iterable<V>> nextVerticesFunction,
                                                         @NonNull AddToSet<V> visited,
                                                         int maxDepth,
                                                         @NonNull C zero,
                                                         @NonNull BiFunction<V, V, C> costFunction,
                                                         @NonNull BiFunction<C, C, C> sumFunction) {
        AlgoArguments.checkZero(zero);
        AlgoArguments.checkMaxDepth(maxDepth);
        Queue<VertexBackLinkWithCost<V, C>> queue = new ArrayDeque<>(16);

        for (V start : startVertices) {
            VertexBackLinkWithCost<V, C> rootBackLink = new VertexBackLinkWithCost<>(start, null, zero);
            if (visited.add(start)) {
                queue.add(rootBackLink);
            }
        }

        VertexBackLinkWithCost<V, C> found = null;
        SequencedSet<V> nonUnique = new LinkedHashSet<>();
        while (!queue.isEmpty()) {
            VertexBackLinkWithCost<V, C> u = queue.remove();
            if (goalPredicate.test(u.getVertex())) {
                if (found != null) {
                    return null;// path is not unique!
                }
                if (u.getDepth() <= 1) {
                    return u; // Up to one hop is considered unique.
                }
                found = u;
            }

            if (u.getDepth() < maxDepth) {
                for (V v : nextVerticesFunction.apply(u.getVertex())) {
                    if (visited.add(v)) {
                        VertexBackLinkWithCost<V, C> backLink = new VertexBackLinkWithCost<>(v, u, sumFunction.apply(u.getCost(), costFunction.apply(u.getVertex(), v)));
                        queue.add(backLink);
                    } else {
                        nonUnique.add(v);
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
