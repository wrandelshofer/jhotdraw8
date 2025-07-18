
/*
 * @(#)StronglyConnectedComponentsAlgo.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph.algo;

import org.jhotdraw8.collection.enumerator.Enumerator;
import org.jhotdraw8.collection.enumerator.IteratorEnumeratorFacade;
import org.jhotdraw8.collection.primitive.IntArrayDeque;
import org.jhotdraw8.graph.DirectedGraph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.lang.Math.min;

/**
 * Computes the sets of strongly connected components in a directed graph.
 * <p>
 * References:
 * <dl>
 *     <dt>Stackoverflow. Non-recursive version of Tarjan's algorithm.
 *     Copyright Ivan Stoev. CC BY-SA 4.0 license.</dt>
 *     <dd><a href="https://stackoverflow.com/questions/46511682/non-recursive-version-of-tarjans-algorithm">stackoverflow.com</a></dd>
 *     <dt>Wikipedia. Tarjan's strongly connected components algorithm</dt>
 *     <dd><a href="https://en.wikipedia.org/wiki/Tarjan%27s_strongly_connected_components_algorithm">wikipedia.org</a></dd>
 *
 * </dl>
 */
public class StackoverflowStronglyConnectedComponentsAlgo {
    /**
     * Holds bookkeeping data for a node v from the graph.
     */
    private static class NodeData {

        /**
         * Low represents the smallest index of any node known to be reachable from v through v's DFS subtree,
         * including v itself.
         * <p>
         * Therefore, v must be left on the stack if v.low < v.index, whereas v must be removed as the root of a
         * strongly connected component if v.low == v.index.
         * <p>
         * The value v.low is computed during the depth-first search from v, as this finds the nodes that are reachable from v.
         */
        private int low;

    }

    public StackoverflowStronglyConnectedComponentsAlgo() {

    }

    /**
     * Returns all strongly connected components in the specified graph.
     *
     * @param graph the graph
     * @param <V>   the vertex data type
     * @param <A>   the arrow data type
     * @return set of strongly connected components (sets of vertices).
     */
    public <V, A> List<List<V>> findStronglyConnectedComponents(
            final DirectedGraph<V, A> graph) {
        return findStronglyConnectedComponents(graph.getVertices(), graph::getNextVertices);
    }

    /**
     * Returns all strongly connected components in the specified graph.
     *
     * @param <V>              the vertex data type
     * @param vertices         the vertices of the graph
     * @param nextNodeFunction returns the next nodes of a given node
     * @return set of strongly connected components (sets of vertices).
     */
    public <V> List<List<V>> findStronglyConnectedComponents(
            final Collection<? extends V> vertices, final Function<V, Iterable<? extends V>> nextNodeFunction
    ) {

        final List<List<V>> sccs = new ArrayList<>();
        final Map<V, NodeData> nodeMap = new HashMap<>();

        int pre = 0;
        Deque<V> stack = new ArrayDeque<>();

        IntArrayDeque minStack = new IntArrayDeque();
        Deque<Enumerator<V>> enumeratorStack = new ArrayDeque<>();
        Enumerator<V> enumerator = new IteratorEnumeratorFacade<>(vertices.iterator());

        while (true) {
            if (enumerator.moveNext()) {
                V v = enumerator.current();
                NodeData vdata = nodeMap.get(v);
                if (vdata == null) {
                    vdata = new NodeData();
                    nodeMap.put(v, vdata);
                    vdata.low = pre++;
                    stack.push(v);
                    // Level down:
                    minStack.pushAsInt(vdata.low);
                    enumeratorStack.push(enumerator);
                    enumerator = new IteratorEnumeratorFacade<>(nextNodeFunction.apply(v).iterator());
                } else {
                    if (!minStack.isEmpty()) {
                        minStack.pushAsInt(min(vdata.low, minStack.popAsInt()));
                    }
                }
            } else {
                // Level up:
                if (enumeratorStack.isEmpty()) {
                    break;
                }

                enumerator = enumeratorStack.pop();
                V v = enumerator.current();
                int min = minStack.popAsInt();
                NodeData vdata = nodeMap.get(v);
                if (min < vdata.low) {
                    vdata.low = min;
                } else {
                    List<V> component = new ArrayList<>();
                    V w;
                    do {
                        w = stack.pop();
                        component.add(w);
                        NodeData wdata = nodeMap.get(w);
                        wdata.low = vertices.size();
                    } while (!w.equals(v));
                    sccs.add(component);
                }

                if (!minStack.isEmpty()) {
                    minStack.pushAsInt(min(vdata.low, minStack.popAsInt()));
                }
            }
        }
        return sccs;
    }
}
