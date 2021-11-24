/*
 * @(#)IntAnyPathBuilder.java
 * Copyright © 2021 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph.path.algo;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.graph.path.backlink.IndexedVertexBackLink;
import org.jhotdraw8.util.function.AddToIntSet;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Spliterator;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntPredicate;


public class IndexedArbitraryVertexPathSearchAlgo<C extends Number & Comparable<C>>
        implements IndexedVertexPathSearchAlgo<C> {


    private static class MyIntConsumer implements IntConsumer {
        int value;

        @Override
        public void accept(int value) {
            this.value = value;
        }
    }

    /**
     * Searches breadth-first for a path from root to goal.
     *
     * @param starts       the starting points of the search
     * @param goal         the goal of the search
     * @param visited      a predicate with side effect. The predicate returns true
     *                     if the specified vertex has been visited, and marks the specified vertex
     *                     as visited.
     * @param maxCost      the maximal path length
     * @param zero
     * @param costFunction
     * @return a back link on success, null on failure
     */
    @Override
    public @Nullable IndexedVertexBackLink<C> search(@NonNull Iterable<Integer> starts,
                                                     @NonNull IntPredicate goal,
                                                     @NonNull Function<Integer, Spliterator.OfInt> nextVerticesFunction,
                                                     @NonNull AddToIntSet visited,
                                                     @NonNull C maxCost,
                                                     @NonNull C zero,
                                                     @NonNull C positiveInfinity,
                                                     @NonNull BiFunction<Integer, Integer, C> costFunction,
                                                     @NonNull BiFunction<C, C, C> sumFunction) {
        Queue<IndexedVertexBackLink<C>> queue = new ArrayDeque<>(32);
        MyIntConsumer consumer = new MyIntConsumer();
        for (Integer start : starts) {
            IndexedVertexBackLink<C> rootBackLink = new IndexedVertexBackLink<C>(start, null, zero);
            if (visited.add(start)) {
                queue.add(rootBackLink);
            }
        }

        while (!queue.isEmpty()) {
            IndexedVertexBackLink<C> node = queue.remove();
            int vertex = node.getVertex();
            if (goal.test(vertex)) {
                return node;
            }

            C currentCost = node.getCost();
            Spliterator.OfInt spliterator = nextVerticesFunction.apply(vertex);
            while (spliterator.tryAdvance(consumer)) {
                if (visited.add(consumer.value)) {
                    C cost = sumFunction.apply(currentCost, costFunction.apply(vertex, consumer.value));
                    if (cost.compareTo(maxCost) <= 0) {
                        IndexedVertexBackLink<C> backLink = new IndexedVertexBackLink<C>(consumer.value, node,
                                cost);
                        queue.add(backLink);
                    }
                }
            }
        }

        return null;
    }
}
