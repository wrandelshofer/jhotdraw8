/*
 * @(#)AnyPathBuilder.java
 * Copyright © 2021 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph.path;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.graph.Arc;
import org.jhotdraw8.util.TriFunction;
import org.jhotdraw8.util.function.AddToSet;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Builder for finding arbitrary paths on a directed graph.
 * <p>
 * The builder searches for paths using a breadth-first search.<br>
 * Returns the first path that it finds.<br>
 * Returns nothing if there is no path.
 *
 * @param <V> the vertex data type
 * @param <A> the arrow data type
 * @author Werner Randelshofer
 */
public class ArbitraryBreadthFirstSequenceBuilder<V, A> extends AbstractShortestSequenceBuilder<V, A, Integer> {

    /**
     * Creates a new instance which searches sequences up to
     * {@link Integer#MAX_VALUE} length.
     *
     * @param nextArcsFunction
     */
    public ArbitraryBreadthFirstSequenceBuilder(@NonNull Function<V, Iterable<Arc<V, A>>> nextArcsFunction) {
        this(Integer.MAX_VALUE, nextArcsFunction);
    }

    /**
     * Creates a new instance.
     *
     * @param maxPathLength    maximal path length
     * @param nextArcsFunction
     */
    public ArbitraryBreadthFirstSequenceBuilder(int maxPathLength, @NonNull Function<V, Iterable<Arc<V, A>>> nextArcsFunction) {
        super(0, Integer.MAX_VALUE, maxPathLength, nextArcsFunction, (v1, v2, a) -> 1, Integer::sum);
    }


    @Override
    protected @Nullable BackLink<V, A, Integer> search(@NonNull Iterable<V> startVertices, @NonNull Predicate<V> goalPredicate, @NonNull Integer zero, @NonNull Integer positiveInfinity, @NonNull Integer maxCost, @NonNull Function<V, Iterable<Arc<V, A>>> nextNodesFunction, @NonNull TriFunction<V, V, A, Integer> costFunction, @NonNull BiFunction<Integer, Integer, Integer> sumFunction) {
        return search(startVertices, goalPredicate, new HashSet<V>()::add, maxCost, nextNodesFunction);
    }

    protected @Nullable BackLink<V, A, Integer> search(@NonNull Iterable<V> startVertices, @NonNull Predicate<V> goal,
                                                       @NonNull AddToSet<V> visited,
                                                       @NonNull Integer maxLength, @NonNull Function<V, Iterable<Arc<V, A>>> nextNodesFunction) {
        Queue<BackLink<V, A, Integer>> queue = new ArrayDeque<>(16);
        for (V root : startVertices) {
            BackLink<V, A, Integer> rootBackLink = new BackLink<>(root, null, null, maxLength);
            if (visited.add(root)) {
                queue.add(rootBackLink);
            }
        }

        while (!queue.isEmpty()) {
            BackLink<V, A, Integer> node = queue.remove();
            if (goal.test(node.getVertex())) {
                return node;
            }

            if (node.getCost() > 0) {
                for (Arc<V, A> next : nextNodesFunction.apply(node.getVertex())) {
                    if (visited.add(next.getEnd())) {
                        BackLink<V, A, Integer> backLink = new BackLink<>(next.getEnd(), next.getData(), node, node.getCost() - 1);
                        queue.add(backLink);
                    }
                }
            }
        }

        return null;
    }
}