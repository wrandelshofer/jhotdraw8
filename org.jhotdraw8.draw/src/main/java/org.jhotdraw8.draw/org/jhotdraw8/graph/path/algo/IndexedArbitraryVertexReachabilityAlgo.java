/*
 * @(#)IntAnyPathBuilder.java
 * Copyright © 2021 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph.path.algo;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.LongArrayDeque;
import org.jhotdraw8.util.function.AddToIntSet;

import java.util.Spliterator;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntPredicate;


public class IndexedArbitraryVertexReachabilityAlgo implements IndexedVertexReachabilityCheckerAlgo<Integer> {


    private static class MyIntConsumer implements IntConsumer {
        int value;

        @Override
        public void accept(int value) {
            this.value = value;
        }
    }


    /**
     * Searches breadth-first whether a path from root to goal exists.
     *
     * @param startVertices the starting points of the search
     * @param goalPredicate the goal of the search
     * @param visited       a predicate with side effect. The predicate returns true
     *                      if the specified vertex has been visited, and marks the specified vertex
     *                      as visited.
     * @param maxCost       the maximal path length
     * @param sumFunction
     * @return true on success, false on failure
     */
    @Override
    public boolean tryToReach(@NonNull Iterable<Integer> startVertices,
                              @NonNull IntPredicate goalPredicate,
                              @NonNull Function<Integer, Spliterator.OfInt> nextVerticesFunction,
                              @NonNull AddToIntSet visited,
                              @NonNull Integer maxCost,
                              @NonNull Integer zero,
                              @NonNull BiFunction<Integer, Integer, Integer> costFunction,
                              @NonNull BiFunction<Integer, Integer, Integer> sumFunction) {
        LongArrayDeque queue = new LongArrayDeque(32);
        MyIntConsumer consumer = new MyIntConsumer();
        for (int root : startVertices) {
            long rootBackLink = newSearchNode(root, zero);

            if (visited.add(root)) {
                queue.addLast(rootBackLink);
            }
        }

        while (!queue.isEmpty()) {
            long node = queue.removeFirst();
            int vertex = searchNodeGetVertex(node);
            if (goalPredicate.test(vertex)) {
                return true;
            }

            int currentCost = searchNodeGetMaxRemaining(node);
            Spliterator.OfInt spliterator = nextVerticesFunction.apply(vertex);
            while (spliterator.tryAdvance(consumer)) {
                final int next = consumer.value;
                if (visited.add(next)) {
                    int cost = currentCost + costFunction.apply(vertex, next);
                    if (cost < maxCost) {
                        long backLink = newSearchNode(next, currentCost - 1);
                        queue.addLast(backLink);
                    }
                }
            }
        }

        return false;
    }

    /**
     * A SearchNode stores for a given vertex, how long the remaining
     * path to gaol may be until we abort the search.
     *
     * @param vertex  a vertex
     * @param maxCost number of remaining path elements until abort
     * @return a SearchNode
     */
    private long newSearchNode(int vertex, int maxCost) {
        return (long) vertex << 32 | (long) maxCost;
    }

    private int searchNodeGetVertex(long primitiveBackLink) {
        return (int) (primitiveBackLink >> 32);
    }

    private int searchNodeGetMaxRemaining(long primitiveBackLink) {
        return (int) primitiveBackLink;
    }


}
