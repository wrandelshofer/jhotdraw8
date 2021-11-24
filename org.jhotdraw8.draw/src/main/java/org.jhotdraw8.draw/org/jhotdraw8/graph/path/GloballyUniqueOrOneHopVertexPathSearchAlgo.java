/*
 * @(#)UniqueOrOneHopPathBuilder.java
 * Copyright © 2021 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph.path;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
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
public class GloballyUniqueOrOneHopVertexPathSearchAlgo<V, C extends Number & Comparable<C>> implements VertexPathSearchAlgo<V, C> {


    @Override
    public @Nullable VertexBackLink<V, C> search(@NonNull Iterable<V> startVertices, @NonNull Predicate<V> goalPredicate, @NonNull C zero, @NonNull C positiveInfinity, @NonNull C maxCost, @NonNull Function<V, Iterable<V>> nextNodesFunction, @NonNull BiFunction<V, V, C> costFunction, @NonNull BiFunction<C, C, C> sumFunction) {
        return search(startVertices, goalPredicate, nextNodesFunction, new HashSet<>(16)::add, zero, maxCost, costFunction, sumFunction);
    }


    public @Nullable VertexBackLink<V, C> search(@NonNull Iterable<V> starts,
                                                 @NonNull Predicate<V> goal,
                                                 @NonNull Function<V, Iterable<V>> nextNodesFunction,
                                                 @NonNull AddToSet<V> visited,
                                                 @NonNull C zero,
                                                 @NonNull C maxCost,
                                                 @NonNull BiFunction<V, V, C> costFunction,
                                                 @NonNull BiFunction<C, C, C> sumFunction) {

        Queue<VertexBackLink<V, C>> queue = new ArrayDeque<>(16);

        for (V start : starts) {
            VertexBackLink<V, C> rootBackLink = new VertexBackLink<>(start, null, zero);
            if (visited.add(start)) {
                queue.add(rootBackLink);
            }
        }

        VertexBackLink<V, C> found = null;
        Set<V> nonUnique = new LinkedHashSet<>();
        while (!queue.isEmpty()) {
            VertexBackLink<V, C> node = queue.remove();
            if (goal.test(node.getVertex())) {
                if (found != null) {
                    return null;// path is not unique!
                }
                if (node.getDepth() <= 1) {
                    return node; // Up to one hop is considered unique.
                }
                found = node;
            }

            for (V next : nextNodesFunction.apply(node.getVertex())) {
                if (visited.add(next)) {
                    C cost = sumFunction.apply(node.getCost(), costFunction.apply(node.getVertex(), next));
                    if (cost.compareTo(maxCost) <= 0) {
                        VertexBackLink<V, C> backLink = new VertexBackLink<V, C>(next, node, cost);
                        queue.add(backLink);
                    }
                } else {
                    nonUnique.add(next);
                }
            }
        }

        for (VertexBackLink<V, C> node = found; node != null; node = node.getParent()) {
            if (nonUnique.contains(node.getVertex())) {
                // path is not unique!
                return null;
            }
        }
        return found;
    }

}
