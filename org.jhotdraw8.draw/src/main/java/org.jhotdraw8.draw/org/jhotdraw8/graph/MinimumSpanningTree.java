package org.jhotdraw8.graph;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.Pair;
import org.jhotdraw8.collection.UnorderedPair;
import org.jhotdraw8.util.ToDoubleTriFunction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.ToDoubleFunction;

public class MinimumSpanningTree {
    private static class Edge<VV, AA> extends UnorderedPair<VV> {
        private final AA arrow;
        private final double cost;

        public Edge(VV a, VV b, AA arrow, double cost) {
            super(a, b);
            this.arrow = arrow;
            this.cost = cost;
        }
    }

    /**
     * Don't let anyone instantiate this class.
     */
    private MinimumSpanningTree() {

    }

    static @NonNull <V> Map<V, List<V>> createForest(@NonNull Collection<V> vertices) {
        // Create initial forest.
        Map<V, List<V>> forest = new LinkedHashMap<>(vertices.size());
        for (V v : vertices) {
            List<V> initialSet = new ArrayList<>(1);
            initialSet.add(v);
            forest.put(v, initialSet);
        }
        return forest;
    }

    /**
     * Given a set of vertices and a list of arrows ordered by cost, returns
     * the minimum spanning tree.
     * <p>
     * Uses Kruskal's algorithm.
     *
     * @param <V>           the vertex type
     * @param <P>           the arrow type
     * @param vertices      a directed graph
     * @param orderedEdges  list of edges sorted by cost in ascending order
     *                      (lowest cost first, highest cost last).
     * @param rejectedEdges optional, all excluded edges are added to this
     *                      list, if it is provided.
     * @return the arrows that are part of the minimum spanning tree.
     */
    public static @NonNull <V, P extends Pair<V, V>> List<P> findMinimumSpanningTree(@NonNull Collection<V> vertices, @NonNull List<P> orderedEdges, @Nullable List<P> rejectedEdges) {
        List<P> minimumSpanningTree = new ArrayList<>(orderedEdges.size());
        if (rejectedEdges == null) {
            rejectedEdges = new ArrayList<>(orderedEdges.size());
        }

        // Create initial forest
        Map<V, List<V>> forest = createForest(vertices);

        // Process arrows from lowest cost to highest cost
        for (P arrow : orderedEdges) {
            List<V> uset = forest.get(arrow.first());
            List<V> vset = forest.get(arrow.second());
            if (uset != vset) {
                union(uset, vset, forest);
                minimumSpanningTree.add(arrow);
            } else {
                rejectedEdges.add(arrow);
            }
        }

        return minimumSpanningTree;
    }

    /**
     * Given an undirected graph and a cost function, returns a builder
     * with the minimum spanning tree.
     * <p>
     *
     * @param <V>   the vertex type
     * @param <A>   the arrow type
     * @param graph the graph. This must be an undirected graph
     *              represented as a directed graph with two identical arrows for each edge.
     * @param costf the cost function
     * @return the graph builder
     */
    public static @NonNull <V, A> DirectedGraphBuilder<V, A> findMinimumSpanningTreeGraph(@NonNull DirectedGraph<V, A> graph, @NonNull ToDoubleFunction<A> costf) {
        return findMinimumSpanningTreeGraph(graph, (u, v, a) -> costf.applyAsDouble(a));

    }

    /**
     * Given an undirected graph and a cost function, returns a builder
     * with the minimum spanning tree.
     * <p>
     *
     * @param <V>   the vertex type
     * @param <A>   the arrow type
     * @param graph the graph. This must be an undirected graph
     *              represented as a directed graph with two identical arrows for each edge.
     * @param costf the cost function
     * @return the graph builder
     */
    public static @NonNull <V, A> DirectedGraphBuilder<V, A> findMinimumSpanningTreeGraph(@NonNull DirectedGraph<V, A> graph, @NonNull ToDoubleTriFunction<V, V, A> costf) {
        Collection<V> vertices = graph.getVertices();
        Set<V> done = new HashSet<>();
        List<Edge<V, A>> edges = new ArrayList<>();
        for (V start : vertices) {
            done.add(start);
            for (Arc<V, A> entry : graph.getNextArcs(start)) {
                V end = entry.getEnd();
                A arrow = entry.getData();
                if (!done.contains(end)) {
                    edges.add(new Edge<>(start, end, arrow, costf.applyAsDouble(start, end, arrow)));
                }
            }
        }
        edges.sort(Comparator.comparingDouble(a -> a.cost));
        List<Edge<V, A>> mst = findMinimumSpanningTree(vertices, edges, null);
        DirectedGraphBuilder<V, A> builder = new DirectedGraphBuilder<>(vertices.size(), mst.size() * 2);
        for (V v : vertices) {
            builder.addVertex(v);
        }
        for (Edge<V, A> e : mst) {
            builder.addArrow(e.first(), e.second(), e.arrow);
            builder.addArrow(e.second(), e.first(), e.arrow);
        }
        return builder;
    }

    /**
     * Given a set of vertices and a list of arrows ordered by cost, returns a
     * builder with the minimum spanning tree. This is an undirected graph with
     * an arrow in each direction.
     * <p>
     *
     * @param <V>            the vertex type
     * @param <A>            the arrow type
     * @param vertices       the list of vertices
     * @param orderedArrows  list of arrows sorted by cost in ascending order
     *                       (lowest cost first, highest cost last)
     * @param includedArrows optional, all included arrows are added to this
     *                       list, if it is provided.
     * @param rejectedArrows optional, all excluded arrows are added to this
     *                       list, if it is provided.
     * @return the graph builder
     */
    public static @NonNull <V, A extends Pair<V, V>> DirectedGraphBuilder<V, A> findMinimumSpanningTreeGraph(@NonNull Collection<V> vertices, @NonNull List<A> orderedArrows, @Nullable List<A> includedArrows, List<A> rejectedArrows) {
        List<A> includedArrowList = findMinimumSpanningTree(vertices, orderedArrows, rejectedArrows);
        if (includedArrows != null) {
            includedArrows.addAll(includedArrowList);
        }
        DirectedGraphBuilder<V, A> builder = new DirectedGraphBuilder<>();
        for (V v : vertices) {
            builder.addVertex(v);
        }
        for (A e : includedArrowList) {
            builder.addArrow(e.first(), e.second(), e);
            builder.addArrow(e.second(), e.first(), e);
        }
        return builder;
    }

    static <V> void union(@NonNull List<V> uset, @NonNull List<V> vset, @NonNull Map<V, List<V>> forest) {
        if (uset != vset) {
            if (uset.size() < vset.size()) {
                for (V uu : uset) {
                    forest.put(uu, vset);
                }
                vset.addAll(uset);
            } else {
                for (V vv : vset) {
                    forest.put(vv, uset);
                }
                uset.addAll(vset);
            }
        }
    }
}
