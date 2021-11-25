/*
 * @(#)IntAnyPathBuilder.java
 * Copyright © 2021 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph.path.algo;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.Enumerator;
import org.jhotdraw8.collection.LongArrayDeque;
import org.jhotdraw8.util.function.AddToIntSet;

import java.util.BitSet;
import java.util.Spliterator;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntPredicate;


/**
 * Checks if there is an arbitrary vertex path from a set of start vertices to a
 * set of goal vertices using a breadth-first search algorithm.
 * <p>
 * The provided cost function must return {@code int} values.
 * <p>
 * This algorithm is optimized for directed graphs with indexed vertices.
 */
public class IndexedArbitraryVertexReachabilityAlgo implements IndexedVertexReachabilityAlgo<Integer> {

    /**
     * A SearchNode stores for a given vertex, how long the remaining
     * path to gaol may be until we abort the search.
     *
     * @param vertex  a vertex
     * @param maxCost number of remaining path elements until abort
     * @return a SearchNode
     */
    private static long newSearchNode(int vertex, int maxCost) {
        return (long) vertex << 32 | (long) maxCost;
    }

    /**
     * Gets the cost from a SearchNode.
     *
     * @param searchNode a SearchNode
     * @return the cost
     */
    private static int searchNodeGetCost(long searchNode) {
        return (int) searchNode;
    }

    /**
     * Gets the vertex index from a SearchNode.
     *
     * @param searchNode a SearchNode
     * @return the vertex index
     */
    private static int searchNodeGetVertex(long searchNode) {
        return (int) (searchNode >> 32);
    }

    @Override
    public boolean tryToReach(@NonNull Iterable<Integer> startVertices,
                              @NonNull IntPredicate goalPredicate,
                              @NonNull Function<Integer, Spliterator.OfInt> nextVerticesFunction,
                              @NonNull Integer maxCost,
                              @NonNull Integer zero,
                              @NonNull Integer positiveInfinity,
                              @NonNull BiFunction<Integer, Integer, Integer> costFunction,
                              @NonNull BiFunction<Integer, Integer, Integer> sumFunction) {
        return tryToReach(startVertices, goalPredicate, nextVerticesFunction,
                AddToIntSet.addToBitSet(new BitSet()),
                maxCost, zero, positiveInfinity, costFunction, sumFunction);
    }

    /**
     * Searches breadth-first whether a path from root to goal exists.
     *
     * @param startVertices    the starting points of the search
     * @param goalPredicate    the goal of the search
     * @param visited          a predicate with side effect. The predicate returns true
     *                         if the specified vertex has been visited, and marks the specified vertex
     *                         as visited.
     * @param maxCost          the maximal path length
     * @param positiveInfinity
     * @param sumFunction
     * @return true on success, false on failure
     */
    public boolean tryToReach(@NonNull Iterable<Integer> startVertices,
                              @NonNull IntPredicate goalPredicate,
                              @NonNull Function<Integer, Spliterator.OfInt> nextVerticesFunction,
                              @NonNull AddToIntSet visited,
                              @NonNull Integer maxCost,
                              @NonNull Integer zero,
                              @NonNull Integer positiveInfinity,
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

            int currentCost = searchNodeGetCost(node);
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
     * Internal helper class, so that we can use a {@link Spliterator}
     * like an {@link Enumerator}.
     */
    private static class MyIntConsumer implements IntConsumer {
        int value;

        @Override
        public void accept(int value) {
            this.value = value;
        }
    }
}
