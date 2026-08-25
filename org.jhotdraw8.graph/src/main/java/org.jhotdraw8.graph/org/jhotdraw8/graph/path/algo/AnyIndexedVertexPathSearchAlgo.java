/*
 * @(#)AnyIndexedVertexPathSearchAlgo.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph.path.algo;

import org.jhotdraw8.collection.primitive.GrowableIntSet8Bit;
import org.jhotdraw8.graph.algo.AddToIntSet;
import org.jhotdraw8.graph.path.backlink.IndexedVertexBackLink;
import org.jhotdraw8.graph.path.backlink.IndexedVertexBackLinkWithCost;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Spliterator;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntPredicate;
import java.util.function.ToIntBiFunction;


/// See [AnyArcPathSearchAlgo] for a description of this
/// algorithm.
///
/// This implementation is optimized for [org.jhotdraw8.graph.IndexedDirectedGraph].
public class AnyIndexedVertexPathSearchAlgo
        implements IndexedVertexPathSearchAlgo {
    public AnyIndexedVertexPathSearchAlgo() {
    }

    private static class MyIntConsumer implements IntConsumer {
        int value;

        @Override
        public void accept(int value) {
            this.value = value;
        }
    }

    @Override
    public @Nullable IndexedVertexBackLinkWithCost search(
            Iterable<Integer> startVertices,
            IntPredicate goalPredicate,
            Function<Integer, Spliterator.OfInt> nextVerticesFunction,
            int maxDepth, int costLimit,
            ToIntBiFunction<Integer, Integer> costFunction,
            AddToIntSet visited) {
        return IndexedVertexBackLink.toIndexedVertexBackLinkWithCost(
                search(startVertices, goalPredicate, nextVerticesFunction,
                        new GrowableIntSet8Bit()::addAsInt, maxDepth),
                costFunction
        );
    }

    /// Search engine method.
    ///
    /// @param startVertices        the set of start vertices
    /// @param goalPredicate        the goal predicate
    /// @param nextVerticesFunction the next vertices function
    /// @param visited              the set of visited vertices (see [AddToIntSet])
    /// @param maxDepth             the maximal depth (inclusive) of the search.
    /// @return on success: a back link, otherwise: null
    public @Nullable IndexedVertexBackLink search(Iterable<Integer> startVertices,
                                                  IntPredicate goalPredicate,
                                                  Function<Integer, Spliterator.OfInt> nextVerticesFunction,
                                                  AddToIntSet visited,
                                                  int maxDepth) {
        AlgoArguments.checkMaxDepth(maxDepth);

        Queue<IndexedVertexBackLink> queue = new ArrayDeque<>(32);
        MyIntConsumer consumer = new MyIntConsumer();
        for (Integer s : startVertices) {
            if (visited.addAsInt(s)) {
                queue.add(new IndexedVertexBackLink(s, null));
            }
        }

        while (!queue.isEmpty()) {
            IndexedVertexBackLink u = queue.remove();
            int vertex = u.getVertex();
            if (goalPredicate.test(vertex)) {
                return u;
            }

            if (u.getDepth() < maxDepth) {
                Spliterator.OfInt spliterator = nextVerticesFunction.apply(vertex);
                while (spliterator.tryAdvance(consumer)) {
                    if (visited.addAsInt(consumer.value)) {
                        queue.add(new IndexedVertexBackLink(consumer.value, u));
                    }
                }
            }
        }

        return null;
    }
}
