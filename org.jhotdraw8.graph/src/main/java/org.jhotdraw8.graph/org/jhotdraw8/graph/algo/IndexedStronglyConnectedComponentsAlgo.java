/*
 * @(#)IndexedStronglyConnectedComponentsAlgo.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph.algo;

import org.jhotdraw8.collection.enumerator.Enumerator;
import org.jhotdraw8.collection.enumerator.IntRangeEnumerator;
import org.jhotdraw8.collection.primitive.IntArrayDeque;
import org.jhotdraw8.collection.primitive.IntArrayList;
import org.jhotdraw8.collection.primitive.IntDeque;
import org.jhotdraw8.collection.primitive.IntList;
import org.jhotdraw8.graph.IndexedDirectedGraph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.function.Function;

import static java.lang.Math.min;

/**
 * Computes the sets of strongly connected components in an indexed directed graph.
 * <p>
 * References:
 * <dl>
 *     <dt>Stackoverflow. Non-recursive version of Tarjan's algorithm.
 *     Copyright Ivan Stoev. CC BY-SA 4.0 license.</dt>
 *     <dd><a href="https://stackoverflow.com/questions/46511682/non-recursive-version-of-tarjans-algorithm">stackoverflow.com</a></dd>
 * </dl>
 */
public class IndexedStronglyConnectedComponentsAlgo {

    public IndexedStronglyConnectedComponentsAlgo() {

    }

    public List<IntList> findStronglyConnectedComponents(
            final IndexedDirectedGraph graph) {
        return findStronglyConnectedComponents(graph.getVertexCount(), graph::nextVerticesEnumerator);
    }


    /**
     * Returns all strongly connected components in the specified graph.
     *
     * @param vertexCount      the vertices of the graph
     * @param nextNodeFunction returns the next nodes of a given node
     * @return set of strongly connected components (sets of vertices).
     */
    public List<IntList> findStronglyConnectedComponents(
            int vertexCount, Function<Integer, Enumerator.OfInt> nextNodeFunction) {
        // The following non-recursive implementation "Tarjan's strongly connected components"
        // algorithm has been taken from
        // https://stackoverflow.com/questions/46511682/non-recursive-version-of-tarjans-algorithm

        final List<IntList> sccs = new ArrayList<>(vertexCount);
        final int[] lows = new int[vertexCount];
        Arrays.fill(lows, -1);

        int pre = 0;
        IntDeque stack = new IntArrayDeque();

        IntDeque minStack = new IntArrayDeque();
        Deque<Enumerator.OfInt> enumeratorStack = new ArrayDeque<>();
        Enumerator.OfInt enumerator = new IntRangeEnumerator(vertexCount);

        while (true) {
            if (enumerator.moveNext()) {
                int v = enumerator.currentAsInt();
                int low = lows[v];
                if (low == -1) {
                    lows[v] = low = pre++;
                    stack.pushAsInt(v);
                    // Level down:
                    minStack.pushAsInt(low);
                    enumeratorStack.push(enumerator);
                    enumerator = nextNodeFunction.apply(v);
                } else {
                    if (!minStack.isEmpty()) {
                        minStack.pushAsInt(min(low, minStack.popAsInt()));
                    }
                }
            } else {
                // Level up:
                if (enumeratorStack.isEmpty()) {
                    break;
                }

                enumerator = enumeratorStack.pop();
                int v = enumerator.currentAsInt();
                int min = minStack.popAsInt();
                int low = lows[v];
                if (min < low) {
                    lows[v] = low = min;
                } else {
                    IntList component = new IntArrayList();
                    int w;
                    do {
                        w = stack.popAsInt();
                        component.addAsInt(w);
                        lows[w] = vertexCount;
                    } while (w != v);
                    sccs.add(component);
                }

                if (!minStack.isEmpty()) {
                    minStack.pushAsInt(min(low, minStack.popAsInt()));
                }
            }
        }
        return sccs;
    }
}
