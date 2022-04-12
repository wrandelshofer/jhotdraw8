/*
 * @(#)MinimumSpanningTreeAlgo.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph.algo;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.Pair;
import org.jhotdraw8.collection.UnorderedPair;
import org.jhotdraw8.graph.Arc;
import org.jhotdraw8.graph.DirectedGraph;
import org.jhotdraw8.graph.SimpleMutableDirectedGraph;
import org.jhotdraw8.util.TriFunction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class MinimumSpanningTreeAlgo {
    public MinimumSpanningTreeAlgo() {

    }

    public static @NonNull <VV> Map<VV, List<VV>> createForest(@NonNull Collection<VV> vertices) {
        // Create initial forest.
        Map<VV, List<VV>> forest = new LinkedHashMap<>(vertices.size());
        for (VV v : vertices) {
            List<VV> initialSet = new ArrayList<>(1);
            initialSet.add(v);
            forest.put(v, initialSet);
        }
        return forest;
    }

    public static <VV> void union(@NonNull List<VV> uset, @NonNull List<VV> vset, @NonNull Map<VV, List<VV>> forest) {
        if (uset != vset) {
            if (uset.size() < vset.size()) {
                for (VV uu : uset) {
                    forest.put(uu, vset);
                }
                vset.addAll(uset);
            } else {
                for (VV vv : vset) {
                    forest.put(vv, uset);
                }
                uset.addAll(vset);
            }
        }
    }

    /**
     * Given a set of vertices and a list of arrows ordered by cost, returns
     * the minimum spanning tree.
     * <p>
     * Uses Kruskal's algorithm.
     *
     * @param <P>           the pair data type
     * @param vertices      a directed graph
     * @param orderedEdges  list of edges sorted by cost in ascending order
     *                      (lowest cost first, highest cost last).
     * @param rejectedEdges optional, all excluded edges are added to this
     *                      list, if it is provided.
     * @return the arrows that are part of the minimum spanning tree.
     */
    public @NonNull <V, A, C extends Number & Comparable<C>, P extends Pair<V, V>> List<P> findMinimumSpanningTree(@NonNull Collection<V> vertices, @NonNull List<P> orderedEdges, @Nullable List<P> rejectedEdges) {
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
     * @param graph the graph. This must be an undirected graph
     *              represented as a directed graph with two identical arrows for each edge.
     * @param costf the cost function
     * @return the graph builder
     */
    public @NonNull <V, A, C extends Number & Comparable<C>> SimpleMutableDirectedGraph<V, A> findMinimumSpanningTreeGraph(@NonNull DirectedGraph<V, A> graph, @NonNull Function<A, C> costf) {
        return findMinimumSpanningTreeGraph(graph, (u, v, a) -> costf.apply(a));

    }

    /**
     * Given an undirected graph and a cost function, returns a builder
     * with the minimum spanning tree.
     * <p>
     *
     * @param graph the graph. This must be an undirected graph
     *              represented as a directed graph with two identical arrows for each edge.
     * @param costf the cost function
     * @return the graph builder
     */
    public @NonNull <V, A, C extends Number & Comparable<C>> SimpleMutableDirectedGraph<V, A> findMinimumSpanningTreeGraph(@NonNull DirectedGraph<V, A> graph, @NonNull TriFunction<V, V, A, C> costf) {
        Collection<V> vertices = graph.getVertices();
        Set<V> done = new HashSet<>();
        List<ArrowWithCost<V, A, C>> arrowWithCosts = new ArrayList<>();
        for (V start : vertices) {
            done.add(start);
            for (Arc<V, A> entry : graph.getNextArcs(start)) {
                V end = entry.getEnd();
                A arrow = entry.getArrow();
                if (!done.contains(end)) {
                    arrowWithCosts.add(new ArrowWithCost<>(start, end, arrow, costf.apply(start, end, arrow)));
                }
            }
        }
        arrowWithCosts.sort(Comparator.comparing(a -> a.cost));
        List<ArrowWithCost<V, A, C>> mst = findMinimumSpanningTree(vertices, arrowWithCosts, null);
        SimpleMutableDirectedGraph<V, A> builder = new SimpleMutableDirectedGraph<>(vertices.size(), mst.size() * 2);
        for (V v : vertices) {
            builder.addVertex(v);
        }
        for (ArrowWithCost<V, A, C> e : mst) {
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
     * @param <P>            the pair data type
     * @param vertices       the list of vertices
     * @param orderedArrows  list of arrows sorted by cost in ascending order
     *                       (lowest cost first, highest cost last)
     * @param includedArrows optional, all included arrows are added to this
     *                       list, if it is provided.
     * @param rejectedArrows optional, all excluded arrows are added to this
     *                       list, if it is provided.
     * @return the graph builder
     */
    public @NonNull <V, A, C extends Number & Comparable<C>, P extends Pair<V, V>> SimpleMutableDirectedGraph<V, P> findMinimumSpanningTreeGraph(@NonNull Collection<V> vertices, @NonNull List<P> orderedArrows, @Nullable List<P> includedArrows, List<P> rejectedArrows) {
        List<P> includedArrowList = findMinimumSpanningTree(vertices, orderedArrows, rejectedArrows);
        if (includedArrows != null) {
            includedArrows.addAll(includedArrowList);
        }
        SimpleMutableDirectedGraph<V, P> builder = new SimpleMutableDirectedGraph<>();
        for (V v : vertices) {
            builder.addVertex(v);
        }
        for (P e : includedArrowList) {
            builder.addArrow(e.first(), e.second(), e);
            builder.addArrow(e.second(), e.first(), e);
        }
        return builder;
    }

    private static class ArrowWithCost<VV, AA, CC extends Number & Comparable<CC>> extends UnorderedPair<VV> {
        private final AA arrow;
        private final CC cost;

        public ArrowWithCost(VV a, VV b, AA arrow, CC cost) {
            super(a, b);
            this.arrow = arrow;
            this.cost = cost;
        }
    }
}
