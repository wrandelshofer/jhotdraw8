/*
 * @(#)StronglyConnectedComponentsAlgo.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph.algo;

import org.jhotdraw8.collection.primitive.IntArrayDeque;
import org.jhotdraw8.collection.primitive.IntDeque;
import org.jhotdraw8.graph.DirectedGraph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
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
 *     <dt>Robert Tarjan (1972). Depth-first search and linear graph algorithms.
 *     </dt>
 *     <dd><a href="http://www.cs.ucsb.edu/~gilbert/cs240a/old/cs240aSpr2011/slides/TarjanDFS.pdf">cs.ucsb.edu</a></dd>
 *
 *     <dt>Wikipedia. Tarjan's strongly connected components algorithm</dt>
 *     <dd><a href="https://en.wikipedia.org/wiki/Tarjan%27s_strongly_connected_components_algorithm">wikipedia.org</a></dd>
 * </dl>
 */
public class StronglyConnectedComponentsAlgo {


    public StronglyConnectedComponentsAlgo() {

    }

    /**
     * Returns all strongly connected components in the specified graph.
     *
     * @param graph the graph
     * @param <V>   the vertex data type
     * @param <A>   the arrow data type
     * @return the strongly connected components of the graph
     * (this is actually a set of sets, but for performance reasons we return a list of lists).
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
     * @return lists of strongly connected components
     * (this is actually a set of sets, but for performance reasons we use a list of lists).
     */
    public <V> List<List<V>> findStronglyConnectedComponents(
            final Collection<? extends V> vertices, final Function<V, Iterable<? extends V>> nextNodeFunction
    ) {
        return new SCCAlgo<>(vertices, nextNodeFunction).findSCCs();

    }

    private record StackFrame<V>(V vv, int v, Iterator<? extends V> neighbors) {

    }

    /**
     * This object holds the algorithm and its current state.
     */
    private static class SCCAlgo<V> {

        private final Function<V, Iterable<? extends V>> getNeighbors;
        private final List<List<V>> SCCs;

        /**
         * This time value is used to indicate that a vertex has not yet been visited.
         */
        private static final int UNVISITED = 0;
        /**
         * Visit time of a vertex.
         * <p>
         * This array is initialized with {@link #UNVISITED}.
         * <p>
         * Note: We choose {@link #UNVISITED} = 0, so that we do not have to explicitly fill this array.
         */
        private final int[] visited;
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
        private final int[] earliest;
        /**
         * Tracks vertices that lie on the current path.
         */
        private final boolean[] onStack;
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
        private final Deque<StackFrame<V>> callStack = new ArrayDeque<>();

        private final Map<V, Integer> vertexToIndex;
        private final List<V> indexToVertex;

        public SCCAlgo(Collection<? extends V> vertices, Function<V, Iterable<? extends V>> nextNodeFunction) {
            this.getNeighbors = nextNodeFunction;
            int vertexCount = vertices.size();
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
                int vi = entry.getValue();
                if (visited[vi] == UNVISITED) {
                    dfs(entry.getKey());
                    assert callStack.isEmpty() : "dfsStack is not empty";
                    assert stack.isEmpty() : "stack is not empty";
                }
            }
            return SCCs;
        }

        /**
         * Non-recursive depth-first search using an explicit call stack.
         */
        private void dfs(V start) {
            callStack.push(new StackFrame<>(start, vertexToIndex.get(start), getNeighbors.apply(start).iterator()));


            Outer:
            while (!callStack.isEmpty()) {
                StackFrame<V> frame = callStack.getFirst();
                int v = frame.v;
                Iterator<? extends V> neighbors = frame.neighbors;

                // 1) Visit a node
                if (visited[v] == UNVISITED) {
                    // Record the visited time
                    visited[v] = earliest[v] = ++time;
                    stack.pushAsInt(v);
                    onStack[v] = true;
                }

                // 2) Process neighbors 'w' until we find one that has not yet been visited yet
                while (neighbors.hasNext()) {
                    V ww = neighbors.next();
                    int w = vertexToIndex.get(ww);
                    if (visited[w] == UNVISITED) {
                        // We have not visited neighbor 'w' before.
                        // Recurse into 'w' using our explicit call stack.
                        callStack.push(new StackFrame<>(ww, w, getNeighbors.apply(ww).iterator()));
                        continue Outer;
                        // Technically, after recursion completes, we continue with step 4) below.
                    } else if (onStack[w]) {
                        // We have visited neighbor 'w' before in our current stack.
                        // If this occurred at an earlier time than the earliest possible visit time of 'v',
                        // then 'v' belongs to the strongly connected component at that earlier time.
                        // If' w' is not on stack, then (v, w) is an edge pointing to a 'scc' already
                        // found and must be ignored.
                        earliest[v] = min(earliest[v], visited[w]);
                    }
                }

                // 3) Remove the recursion step from the call stack.
                callStack.pop();

                // 4) If it is possible to visit 'v' at an earlier time than its
                // parent on the call stack,
                // then its parent can also be visited at that earlier time.
                if (!callStack.isEmpty()) {
                    int parent = callStack.getFirst().v;
                    earliest[parent] = min(earliest[parent], earliest[v]);
                }

                // 5) If 'v' is the root of a 'scc', add the 'scc' to the result list.
                if (visited[v] == earliest[v]) {
                    List<V> scc = new ArrayList<>();
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
