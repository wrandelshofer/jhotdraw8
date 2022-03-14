/*
 * @(#)IndexedStronglyConnectedComponentsAlgo.java
 * Copyright © 2021 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph.algo;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.IntArrayDeque;
import org.jhotdraw8.collection.IntArrayList;
import org.jhotdraw8.collection.IntEnumerator;
import org.jhotdraw8.collection.IntRangeEnumerator;
import org.jhotdraw8.graph.IndexedDirectedGraph;

import java.util.*;
import java.util.function.Function;

import static java.lang.Math.min;

/**
 * Computes the sets of strongly connected components in an indexed directed graph.
 * <p>
 * References:
 * <dl>
 *     <dt>Stackoverflow. Non-recursive implementation "Tarjan's strongly connected components".</dt>
 *     <dd><a href="https://stackoverflow.com/questions/46511682/non-recursive-version-of-tarjans-algorithm">stackoverflow.com</a></dd>
 * </dl>
 */
public class IndexedStronglyConnectedComponentsAlgo {

    public IndexedStronglyConnectedComponentsAlgo() {

    }

    public @NonNull List<IntArrayList> findStronglyConnectedComponents(
            final @NonNull IndexedDirectedGraph graph) {
        return findStronglyConnectedComponents(graph.getVertexCount(), graph::nextVerticesSpliterator);
    }


    /**
     * Returns all strongly connected components in the specified graph.
     *
     * @param vertexCount      the vertices of the graph
     * @param nextNodeFunction returns the next nodes of a given node
     * @return set of strongly connected components (sets of vertices).
     */
    public @NonNull List<IntArrayList> findStronglyConnectedComponents(int vertexCount, Function<Integer, IntEnumerator> nextNodeFunction) {
        // The following non-recursive implementation "Tarjan's strongly connected components"
        // algorithm has been taken from
        // https://stackoverflow.com/questions/46511682/non-recursive-version-of-tarjans-algorithm

        final List<IntArrayList> sccs = new ArrayList<>(vertexCount);
        final int[] lows = new int[vertexCount];
        Arrays.fill(lows, -1);

        int pre = 0;
        IntArrayDeque stack = new IntArrayDeque();

        IntArrayDeque minStack = new IntArrayDeque();
        Deque<IntEnumerator> enumeratorStack = new ArrayDeque<>();
        IntEnumerator enumerator = new IntRangeEnumerator(vertexCount);

        STRONGCONNECT:
        while (true) {
            if (enumerator.moveNext()) {
                int v = enumerator.currentAsInt();
                int low = lows[v];
                if (low == -1) {
                    lows[v] = low = pre++;
                    stack.push(v);
                    // Level down:
                    minStack.push(low);
                    enumeratorStack.push(enumerator);
                    enumerator = nextNodeFunction.apply(v);
                } else {
                    if (!minStack.isEmpty()) {
                        minStack.push(min(low, minStack.pop()));
                    }
                }
            } else {
                // Level up:
                if (enumeratorStack.isEmpty()) {
                    break STRONGCONNECT;
                }

                enumerator = enumeratorStack.pop();
                int v = enumerator.currentAsInt();
                int min = minStack.pop();
                int low = lows[v];
                if (min < low) {
                    lows[v] = low = min;
                } else {
                    IntArrayList component = new IntArrayList();
                    int w;
                    do {
                        w = stack.pop();
                        component.add(w);
                        lows[w] = vertexCount;
                    } while (w != v);
                    sccs.add(component);
                }

                if (!minStack.isEmpty()) {
                    minStack.push(min(low, minStack.pop()));
                }
            }
        }
        return sccs;
    }
}
