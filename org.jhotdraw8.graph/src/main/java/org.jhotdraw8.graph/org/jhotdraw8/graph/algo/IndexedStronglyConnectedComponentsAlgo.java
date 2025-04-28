
/*
 * @(#)IndexedStronglyConnectedComponentsAlgo.öava
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph.algo;

import org.jhotdraw8.collection.enumerator.Enumerator;
import org.jhotdraw8.collection.primitive.IntArrayDeque;
import org.jhotdraw8.collection.primitive.IntArrayList;
import org.jhotdraw8.collection.primitive.IntDeque;
import org.jhotdraw8.collection.primitive.IntList;
import org.jhotdraw8.graph.IndexedDirectedGraph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Function;

import static java.lang.Math.min;

/**
 * Computes the sets of strongly connected components in an indexed directed graph.
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
        return new SCCAlgo(vertexCount, nextNodeFunction).findSCCs();
    }

    private record StackFrame(int v, Enumerator.OfInt neighbors) {

    }

    /**
     * This object holds the algorithm and its current state.
     */
    private static class SCCAlgo {
        final Function<Integer, Enumerator.OfInt> getNeighbors;
        final List<IntList> SCCs;
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
        private final Deque<StackFrame> callStack = new ArrayDeque<>();


        public SCCAlgo(int vertexCount, Function<Integer, Enumerator.OfInt> nextNodesFunction) {
            this.getNeighbors = nextNodesFunction;
            SCCs = new ArrayList<>(vertexCount);
            this.vertexCount = vertexCount;
            this.visited = new int[vertexCount];
            this.earliest = new int[vertexCount];
            this.onStack = new boolean[vertexCount];
            stack = new IntArrayDeque();
        }

        public List<IntList> findSCCs() {
            for (int v = 0; v < vertexCount; v++) {
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
            callStack.push(new StackFrame(start, getNeighbors.apply(start)));

            Outer:
            while (!callStack.isEmpty()) {
                StackFrame frame = callStack.getFirst();
                int v = frame.v;
                Enumerator.OfInt neighbors = frame.neighbors;

                // 1) Visit a node
                if (visited[v] == UNVISITED) {
                    // Record the visited time
                    visited[v] = earliest[v] = ++time;
                    stack.pushAsInt(v);
                    onStack[v] = true;
                }

                // 2) Process neighbors 'w' until we find one that has not yet been visited yet
                while (neighbors.moveNext()) {
                    int w = neighbors.currentAsInt();
                    if (visited[w] == UNVISITED) {
                        // We have not visited neighbor 'w' before.
                        // Recurse into 'w' using our explicit call stack.
                        callStack.push(new StackFrame(w, getNeighbors.apply(w)));
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
                    IntList scc = new IntArrayList();
                    int w;
                    do {
                        w = stack.popAsInt();
                        onStack[w] = false;
                        scc.add(w);
                    } while (w != v);
                    SCCs.add(scc);
                }
            }
        }
    }
}
