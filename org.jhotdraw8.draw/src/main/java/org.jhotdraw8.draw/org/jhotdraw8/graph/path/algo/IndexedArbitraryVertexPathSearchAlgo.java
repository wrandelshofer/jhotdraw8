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
import java.util.BitSet;
import java.util.Queue;
import java.util.Spliterator;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntPredicate;


/**
 * Searches an arbitrary vertex path from a set of start vertices to a set of goal
 * vertices using a breadth-first search algorithm.
 * <p>
 * This algorithm is optimized for directed graphs with indexed vertices.
 *
 * @param <C> the cost number type
 */
public class IndexedArbitraryVertexPathSearchAlgo<C extends Number & Comparable<C>>
        implements IndexedVertexPathSearchAlgo<C> {


    private static class MyIntConsumer implements IntConsumer {
        int value;

        @Override
        public void accept(int value) {
            this.value = value;
        }
    }

    @Override
    public @Nullable IndexedVertexBackLink<C> search(
            @NonNull Iterable<Integer> startVertices,
            @NonNull IntPredicate goalPredicate,
            @NonNull Function<Integer, Spliterator.OfInt> nextVerticesFunction,
            @NonNull C maxCost,
            @NonNull C zero,
            @NonNull C positiveInfinity,
            @NonNull BiFunction<Integer, Integer, C> costFunction,
            @NonNull BiFunction<C, C, C> sumFunction) {
        return search(startVertices, goalPredicate, nextVerticesFunction,
                AddToIntSet.addToBitSet(new BitSet()), maxCost, zero,
                positiveInfinity,
                costFunction, sumFunction);
    }

    /**
     * Search engine method.
     *
     * @param startVertices        the set of start vertices
     * @param goalPredicate        the goal predicate
     * @param nextVerticesFunction the next vertices function
     * @param visited              the set of visited vertices (see {@link AddToIntSet})
     * @param maxCost              the maximal cost (inclusive) that a sequence may have
     * @param zero                 the zero cost value
     * @param costFunction         the cost function
     * @param sumFunction          the sum function for adding two cost values
     * @return on success: a back link, otherwise: null
     */
    public @Nullable IndexedVertexBackLink<C> search(@NonNull Iterable<Integer> startVertices,
                                                     @NonNull IntPredicate goalPredicate,
                                                     @NonNull Function<Integer, Spliterator.OfInt> nextVerticesFunction,
                                                     @NonNull AddToIntSet visited,
                                                     @NonNull C maxCost,
                                                     @NonNull C zero,
                                                     @NonNull C positiveInfinity,
                                                     @NonNull BiFunction<Integer, Integer, C> costFunction,
                                                     @NonNull BiFunction<C, C, C> sumFunction) {
        Queue<IndexedVertexBackLink<C>> queue = new ArrayDeque<>(32);
        MyIntConsumer consumer = new MyIntConsumer();
        for (Integer start : startVertices) {
            IndexedVertexBackLink<C> rootBackLink = new IndexedVertexBackLink<>(start, null, zero);
            if (visited.add(start)) {
                queue.add(rootBackLink);
            }
        }

        while (!queue.isEmpty()) {
            IndexedVertexBackLink<C> node = queue.remove();
            int vertex = node.getVertex();
            if (goalPredicate.test(vertex)) {
                return node;
            }

            C currentCost = node.getCost();
            Spliterator.OfInt spliterator = nextVerticesFunction.apply(vertex);
            while (spliterator.tryAdvance(consumer)) {
                if (visited.add(consumer.value)) {
                    C cost = sumFunction.apply(currentCost, costFunction.apply(vertex, consumer.value));
                    if (cost.compareTo(maxCost) <= 0) {
                        IndexedVertexBackLink<C> backLink = new IndexedVertexBackLink<>(consumer.value, node,
                                cost);
                        queue.add(backLink);
                    }
                }
            }
        }

        return null;
    }
}
