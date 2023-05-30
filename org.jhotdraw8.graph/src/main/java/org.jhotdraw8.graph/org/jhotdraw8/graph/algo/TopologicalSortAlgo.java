/*
 * @(#)TopologicalSortAlgo.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph.algo;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.SimpleOrderedPair;
import org.jhotdraw8.collection.enumerator.IntSpliterator;
import org.jhotdraw8.collection.primitive.IntArrayList;
import org.jhotdraw8.graph.AttributedIndexedDirectedGraph;
import org.jhotdraw8.graph.DirectedGraph;
import org.jhotdraw8.graph.IndexedDirectedGraph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;

/**
 * Provides topological sort algorithms for directed graphs.
 *
 * @author Werner Randelshofer
 */
public class TopologicalSortAlgo {
    public TopologicalSortAlgo() {
    }

    /**
     * Sorts the specified directed graph topologically.
     *
     * @param <V> the vertex data type
     * @param <A> the arrow data type
     * @param m   the graph
     * @return the sorted list of vertices
     */
    @SuppressWarnings("unchecked")
    public @NonNull <V, A> List<V> sortTopologically(DirectedGraph<V, A> m) {
        final AttributedIndexedDirectedGraph<V, A> im;
        if (!(m instanceof AttributedIndexedDirectedGraph)) {
            return sortTopologicallyObject(m);
        } else {
            im = (AttributedIndexedDirectedGraph<V, A>) m;
        }
        int[] a = sortTopologicallyInt(im);
        List<V> result = new ArrayList<>(a.length);
        for (int i = 0; i < a.length; i++) {
            result.add(im.getVertex(a[i]));
        }
        return result;
    }


    /**
     * Sorts the specified directed graph topologically.
     *
     * @param model the graph
     * @return the sorted list of vertices
     */
    public int @NonNull [] sortTopologicallyInt(@NonNull IndexedDirectedGraph model) {
        final int n = model.getVertexCount();

        // Step 1: compute number of incoming arrows for each vertex
        final int[] deg = new int[n]; // deg is the number of unprocessed incoming arrows on vertex
        for (int i = 0; i < n; i++) {
            for (IntSpliterator iter = model.nextVerticesEnumerator(i); iter.moveNext(); ) {
                int v = iter.currentAsInt();
                deg[v]++;
            }
        }

        // Step 2: put all vertices with degree zero into deque
        final int[] queue = new int[n];
        int first = 0, last = 0; // first and last indices in deque
        for (int i = 0; i < n; i++) {
            if (deg[i] == 0) {
                queue[last++] = i;
            }
        }

        // Step 3: Repeat until all vertices have been processed or a loop has been detected
        final int[] result = new int[n];// result array
        int done = 0;
        while (done < n) {
            for (; done < n; done++) {
                if (first == last) {
                    // => the graph has a loop!
                    break;
                }
                int v = queue[first++];
                for (IntSpliterator iter = model.nextVerticesEnumerator(v); iter.moveNext(); ) {
                    int u = iter.currentAsInt();
                    if (--deg[u] == 0) {
                        queue[last++] = u;
                    }
                }
                result[done] = v;
            }

            if (done < n) {
                // Break loop in graph by removing all arrows on a node.
                int i = 0;
                while (i < n - 1 && deg[i] <= 0) {
                    i++;
                }
                if (deg[i] == 0) {
                    throw new AssertionError("bug in loop-breaking algorithm i: " + i);
                }
                deg[i] = 0;// this can actually remove more than one arrow
                queue[last++] = i;
            }
        }

        return result;
    }

    /**
     * Sorts the specified directed graph topologically.
     * Returns a list of batches that do not depend topologically on
     * each other.
     *
     * @param model the graph
     * @return the sorted list of vertices and the list of batches,
     * batches will be empty if the graph has cycles
     */
    public @NonNull SimpleOrderedPair<int[], IntArrayList> sortTopologicallyIntBatches(@NonNull IndexedDirectedGraph model) {
        final int n = model.getVertexCount();
        IntArrayList batches = new IntArrayList();
        boolean hasLoop = false;

        // Step 1: compute number of incoming arrows for each vertex
        final int[] deg = new int[n]; // deg is the number of unprocessed incoming arrows on vertex
        for (int i = 0; i < n; i++) {
            for (IntSpliterator iter = model.nextVerticesEnumerator(i); iter.moveNext(); ) {
                int v = iter.currentAsInt();
                deg[v]++;
            }
        }

        // Step 2: put all vertices with degree zero into deque
        final int[] queue = new int[n];
        int first = 0, last = 0; // first and last indices in deque
        for (int i = 0; i < n; i++) {
            if (deg[i] == 0) {
                queue[last++] = i;
            }
        }
        int lastBatch = last;
        batches.addAsInt(last);

        // Step 3: Repeat until all vertices have been processed or a loop has been detected
        final int[] result = new int[n];// result array
        int done = 0;
        while (done < n) {
            for (; done < n; done++) {
                if (first == last) {
                    hasLoop = true;
                    break;
                }
                int v = queue[first++];
                queue[first - 1] = 0;//for debugging
                for (IntSpliterator iter = model.nextVerticesEnumerator(v); iter.moveNext(); ) {
                    int u = iter.currentAsInt();
                    if (--deg[u] == 0) {
                        queue[last++] = u;
                    }

                }
                result[done] = v;

                if (first == lastBatch && done < n - 1) {
                    lastBatch = last;
                    batches.addAsInt(last);
                }
            }

            if (done < n) {
                // Break loop in graph by removing all arrows on a node.
                int i = 0;
                while (i < n - 1 && deg[i] <= 0) {
                    i++;
                }
                if (deg[i] == 0) {
                    throw new AssertionError("bug in loop-breaking algorithm i: " + i);
                }
                deg[i] = 0;// this can actually remove more than one arrow
                queue[last++] = i;
            }
        }

        if (hasLoop) {
            batches.clear();
        }

        return new SimpleOrderedPair<>(result, batches);
    }

    /**
     * Sorts the specified directed graph topologically.
     *
     * @param <V>   the vertex data type
     * @param <A>   the arrow data type
     * @param model the graph
     * @return the sorted list of vertices
     */
    public @NonNull <V, A> List<V> sortTopologicallyObject(@NonNull DirectedGraph<V, A> model) {
        return sortTopologically(model.getVertices(), model::getNextVertices);
    }

    /**
     * Sorts the specified directed graph topologically.
     * <p>
     * If the graph contains cycles, then this method splits the graph
     * inside a cycle.
     *
     * @param <V>          the vertex data type
     * @param vertices     the vertices of the graph
     * @param nextVertices a function that delivers the next vertices for a given vertex
     * @return the sorted list of vertices
     */
    public @NonNull <V> List<V> sortTopologically(@NonNull Collection<V> vertices,
                                                  @NonNull Function<V, Iterable<? extends V>> nextVertices) {
        final int n = vertices.size();
        Set<V> verticesInLoops = null;

        // Step 1: compute number of incoming arrows for each vertex
        final Map<V, Integer> deg = new LinkedHashMap<>(n); // deg is the number of unprocessed incoming arrows on vertex
        for (V v : vertices) {
            deg.putIfAbsent(v, 0);
            for (V u : nextVertices.apply(v)) {
                deg.merge(u, 1, Integer::sum);
            }
        }

        // Step 2: put all vertices with degree zero into queue
        final Queue<V> queue = new ArrayDeque<>(n);
        for (Map.Entry<V, Integer> entry : deg.entrySet()) {
            if (entry.getValue() == 0) {
                queue.add(entry.getKey());
            }
        }

        // Step 3: Repeat until all vertices have been processed or a loop has been detected
        final List<V> result = new ArrayList<>(n);// result array
        int done = 0;
        while (done < n) {
            for (; done < n; done++) {
                if (queue.isEmpty()) {
                    // => the graph has a loop!
                    break;
                }
                V v = queue.remove();
                for (V u : nextVertices.apply(v)) {
                    if (deg.merge(u, -1, Integer::sum) == 0) {
                        queue.add(u);
                    }
                }
                result.add(v);
            }

            if (done < n) {
                // FIXME only search in remaining subgraph
                if (verticesInLoops == null) {
                    List<List<V>> stronglyConnectedComponents = new StronglyConnectedComponentsAlgo().findStronglyConnectedComponents(vertices, nextVertices);
                    verticesInLoops = new LinkedHashSet<>();
                    for (List<V> stronglyConnectedComponent : stronglyConnectedComponents) {
                        if (stronglyConnectedComponent.size() > 1) {
                            verticesInLoops.addAll(stronglyConnectedComponent);
                        }
                    }
                }

                // Break loop in graph by removing an arbitrary arrow.
                boolean didBreakLoop = false;
                for (V v : verticesInLoops) {
                    if (deg.get(v) > 0) {
                        deg.put(v, 0);// this can actually remove more than one arrow
                        queue.add(v);
                        didBreakLoop = true;
                        break;
                    }
                }
                if (!didBreakLoop) {
                    throw new AssertionError("Programming error in loop breaking code.");
                }
            }
        }

        return result;
    }


}
