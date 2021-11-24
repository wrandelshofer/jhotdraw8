/*
 * @(#)AnyPathBuilder.java
 * Copyright © 2021 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph.path.algo;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.graph.Arc;
import org.jhotdraw8.graph.path.backlink.ArcBackLink;
import org.jhotdraw8.util.TriFunction;
import org.jhotdraw8.util.function.AddToSet;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public class ArbitraryPathSearchAlgo<V, A, C extends Number & Comparable<C>> implements PathSearchAlgo<V, A, C> {

    @Override
    public @Nullable ArcBackLink<V, A, C> search(@NonNull Iterable<V> startVertices,
                                                 @NonNull Predicate<V> goalPredicate,
                                                 @NonNull Function<V, Iterable<Arc<V, A>>> nextArcsFunction, @NonNull C zero,
                                                 @NonNull C positiveInfinity,
                                                 @NonNull C maxCost,
                                                 @NonNull TriFunction<V, V, A, C> costFunction,
                                                 @NonNull BiFunction<C, C, C> sumFunction) {
        return search(startVertices, goalPredicate, new HashSet<V>()::add, zero, maxCost, nextArcsFunction,
                costFunction, sumFunction);
    }

    public @Nullable ArcBackLink<V, A, C> search(@NonNull Iterable<V> startVertices,
                                                 @NonNull Predicate<V> goal,
                                                 @NonNull AddToSet<V> visited,
                                                 @NonNull C zero,
                                                 @NonNull C maxCost,
                                                 @NonNull Function<V, Iterable<Arc<V, A>>> nextNodesFunction,
                                                 @NonNull TriFunction<V, V, A, C> costFunction,
                                                 @NonNull BiFunction<C, C, C> sumFunction) {
        Queue<ArcBackLink<V, A, C>> queue = new ArrayDeque<>(16);
        for (V root : startVertices) {
            ArcBackLink<V, A, C> rootBackLink = new ArcBackLink<>(root, null, null, zero);
            if (visited.add(root)) {
                queue.add(rootBackLink);
            }
        }

        while (!queue.isEmpty()) {
            ArcBackLink<V, A, C> node = queue.remove();
            if (goal.test(node.getVertex())) {
                return node;
            }

            for (Arc<V, A> next : nextNodesFunction.apply(node.getVertex())) {
                if (visited.add(next.getEnd())) {
                    C cost = sumFunction.apply(node.getCost(), costFunction.apply(node.getVertex(), next.getEnd(), next.getData()));
                    if (cost.compareTo(maxCost) <= 0) {
                        ArcBackLink<V, A, C> backLink = new ArcBackLink<>(next.getEnd(), next.getData(), node, cost);
                        queue.add(backLink);
                    }
                }
            }
        }

        return null;
    }
}