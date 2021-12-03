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
 * See {@link GloballyArbitraryArcPathSearchAlgo} for a description of this
 * algorithm.
 * <p>
 * This implementation is optimized for {@link org.jhotdraw8.graph.IndexedDirectedGraph}
 * and for cost functions that return an int value.
 */
public class GloballyArbitraryIndexedVertexReachabilityAlgoWithIntCost implements IndexedVertexReachabilityAlgo<Integer> {

    /**
     * A SearchNode stores for a given vertex, how long the remaining
     * path to gaol may be until we abort the search.
     *
     * @param vertex a vertex
     * @param depth  number of remaining path elements until abort
     * @return a SearchNode
     */
    private static long newSearchNode(int vertex, int depth) {
        return (long) vertex << 32 | (long) depth;
    }

    /**
     * Gets the cost from a SearchNode.
     *
     * @param searchNode a SearchNode
     * @return the cost
     */
    private static int searchNodeGetDepth(long searchNode) {
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

    /**
     * {@inheritDoc}
     * @param startVertices        the set of start vertices
     * @param goalPredicate        the goal predicate
     * @param nextVerticesFunction the next vertices function
     * @param maxDepth             the maximal depth (inclusive) of the search
     *                             Must be {@literal >= 0}.
     * @param zero                 the zero cost value
     * @param costLimit            the maximal cost (inclusive) of a path
     *                             Must be {@literal >= zero).
     * @param costFunction         the cost function
     * @param sumFunction          the sum function for adding two cost values
     * @return
     */
    @Override
    public boolean tryToReach(@NonNull Iterable<Integer> startVertices,
                              @NonNull IntPredicate goalPredicate,
                              @NonNull Function<Integer, Spliterator.OfInt> nextVerticesFunction,
                              int maxDepth, @NonNull Integer zero, @NonNull Integer costLimit,
                              @NonNull BiFunction<Integer, Integer, Integer> costFunction,
                              @NonNull BiFunction<Integer, Integer, Integer> sumFunction) {
        return tryToReach(startVertices, goalPredicate, nextVerticesFunction,
                AddToIntSet.addToBitSet(new BitSet()),
                costLimit);
    }

    /**
     * Searches breadth-first whether a path from root to goal exists.
     *
     * @param startVertices the starting points of the search
     *                      Must be {@literal >= 0}.
     * @param goalPredicate the goal of the search
     *                      Must be {@literal >= 0}.
     * @param visited       a predicate with side effect. The predicate returns true
     *                      if the specified vertex has been visited, and marks the specified vertex
     *                      as visited.
     * @param maxDepth      the maximal depth (inclusive) of the search.
     * @return true on success, false on failure
     */
    public boolean tryToReach(@NonNull Iterable<Integer> startVertices,
                              @NonNull IntPredicate goalPredicate,
                              @NonNull Function<Integer, Spliterator.OfInt> nextVerticesFunction,
                              @NonNull AddToIntSet visited,
                              @NonNull Integer maxDepth) {
        if (maxDepth < 0) {
            throw new IllegalArgumentException("maxDepth must be >= 0. maxDepth=" + maxDepth);
        }

        LongArrayDeque queue = new LongArrayDeque(32);
        MyIntConsumer consumer = new MyIntConsumer();
        for (int s : startVertices) {
            if (visited.add(s)) {
                queue.addLast(newSearchNode(s, 0));
            }
        }

        while (!queue.isEmpty()) {
            long u = queue.removeFirst();
            int vertex = searchNodeGetVertex(u);
            if (goalPredicate.test(vertex)) {
                return true;
            }

            if (searchNodeGetDepth(u) < maxDepth) {
                Spliterator.OfInt spliterator = nextVerticesFunction.apply(vertex);
                while (spliterator.tryAdvance(consumer)) {
                    final int v = consumer.value;
                    if (visited.add(v)) {
                        queue.addLast(newSearchNode(v, searchNodeGetDepth(u) + 1));
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
