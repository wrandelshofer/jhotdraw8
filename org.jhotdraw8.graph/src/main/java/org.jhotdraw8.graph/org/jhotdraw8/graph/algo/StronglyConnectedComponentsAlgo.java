/*
 * @(#)StronglyConnectedComponentsAlgo.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph.algo;

import org.jhotdraw8.collection.primitive.IntArrayDeque;
import org.jhotdraw8.collection.primitive.IntDeque;
import org.jhotdraw8.graph.DirectedGraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
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
public class StronglyConnectedComponentsAlgo {
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

    public StronglyConnectedComponentsAlgo() {

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
        return new SCCAlgo<V>(vertices, nextNodeFunction).findSCCs();

    }

    /**
     * This object holds the algorithm and its current state.
     */
    private static class SCCAlgo<V> {
        final Function<V, Iterable<? extends V>> adj;
        final List<List<V>> SCCs;
        final int vertexCount;

        /**
         * This time value is used to indicate that a vertex has not yet been visited.
         */
        final static int UNVISITED = 0;
        /**
         * Visit time of a vertex.
         * <p>
         * This array is initialized with {@link #UNVISITED}.
         * <p>
         * Note: We choose {@link #UNVISITED} = 0, so that we do not have to explicitly fill this array.
         */
        final int[] visited;
        /**
         * Visit time counter.
         */
        private int time = UNVISITED;
        /**
         * Earliest time a vertex could have been visited.
         * <p<
         * This is the earliest visit time of a vertex if we had performed the depth-first search
         * using a different permutation of the 'neighbor' vertices.
         */
        final int[] earliest;
        /**
         * Tracks vertices that lie on the current path.
         */
        final boolean[] onStack;
        /**
         * The current stack.
         * <p>
         * The topmost elements of the stack contain the current
         * strongly connected component ('scc').
         * <p>
         * The 'scc' extends from the top of the stack down to vertex 'v',
         * if 'v' is the root of the 'scc'.
         * <p>
         * 'v' is the root if the 'scc', if its {@link #visited} time is equal to
         * the earliest possible time that we have determined in {@link #earliest}.
         * <p>
         * If we find a neighbor 'w' that is on the current stack,
         * and if it its {@link #visited} time is earlier than {@link #earliest} of 'v',
         * then this means that 'w' and 'v' are on the same strongly connected component.
         * Thus, we can update the {@link #earliest} of 'v' with that earlier time.
         */
        private final IntDeque stack;
        /**
         * The explicit call stack of the non-recursive depth first search.
         */
        private IntDeque callStack = new IntArrayDeque();

        private final Map<V, Integer> vertexToIndex;
        private final List<V> indexToVertex;

        public SCCAlgo(Collection<? extends V> vertices, Function<V, Iterable<? extends V>> nextNodeFunction) {
            this.adj = nextNodeFunction;
            this.vertexCount = vertices.size();
            SCCs = new ArrayList<>(vertexCount);
            this.visited = new int[vertexCount];
            this.earliest = new int[vertexCount];
            this.onStack = new boolean[vertexCount];
            stack = new IntArrayDeque();
            this.vertexToIndex = new LinkedHashMap<>();
            this.indexToVertex = new ArrayList<>();
            for (V v : vertices) {
                this.vertexToIndex.put(v, vertexToIndex.size());
                this.indexToVertex.add(v);
            }
        }

        public List<List<V>> findSCCs() {
            for (Map.Entry<V, Integer> entry : vertexToIndex.entrySet()) {
                int v = entry.getValue();
                if (visited[v] == UNVISITED) {
                    dfs(v);
                    if (!callStack.isEmpty()) throw new IllegalStateException("dfsStack is not empty");
                    if (!stack.isEmpty()) throw new IllegalStateException("stack is not empty");
                }
            }
            return SCCs;
        }

        /**
         * Non-recursive depth-first search using an explicit call stack.
         */
        private void dfs(int start) {
            callStack.pushAsInt(start);

            Outer:
            while (!callStack.isEmpty()) {
                int v = callStack.getFirstAsInt();

                if (visited[v] == UNVISITED) {
                    // Record the visited time
                    visited[v] = earliest[v] = ++time;
                    stack.pushAsInt(v);
                    onStack[v] = true;
                }

                // Process neighbors 'w' until we find one that has not yet been visited yet
                for (V W : adj.apply(indexToVertex.get(v))) {
                    int w = vertexToIndex.get(W);
                    if (visited[w] == UNVISITED) {
                        // We have not visited neighbor 'w' before.
                        // Recurse into 'w' using our explicit call stack.
                        callStack.pushAsInt(w);
                        continue Outer;
                    } else if (onStack[w]) {
                        // We have visited neighbor 'w' before in our current stack.
                        // If this occurred at an earlier time than the earliest visit time of 'v',
                        // then 'v' belongs to the strongly connected component at that earlier time.
                        // If' w' is not on stack, then (v, w) is an edge pointing to a 'scc' already
                        // found and must be ignored.
                        earliest[v] = min(earliest[v], visited[w]);
                    }
                }

                // We pop the vertex 'v' if it has no unvisited neighbors
                callStack.popAsInt();


                // If it is possible to visit 'v' at an earlier time than its parent,
                // then its parent can also be visited at that earlier time.
                if (!callStack.isEmpty()) {
                    int parent = callStack.getFirstAsInt();
                    earliest[parent] = min(earliest[parent], earliest[v]);
                }

                // If 'v' is the root of a 'scc', add the 'scc' to the result list.
                if (visited[v] == earliest[v]) {
                    List<V> scc = new ArrayList<V>();
                    int w;
                    do {
                        w = stack.popAsInt();
                        onStack[w] = false;
                        scc.add(indexToVertex.get(w));
                    } while (w != v);
                    SCCs.add(scc);
                }
            }
        }
    }
}
