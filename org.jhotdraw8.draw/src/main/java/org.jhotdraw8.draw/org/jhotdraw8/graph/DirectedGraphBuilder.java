/*
 * @(#)DirectedGraphBuilder.java
 * Copyright © 2021 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph;

import org.jhotdraw8.annotation.NonNull;

import java.util.function.Predicate;


/**
 * DirectedGraphBuilder.
 *
 * @param <V> the vertex data type
 * @param <A> the arrow data type
 * @author Werner Randelshofer
 */
public class DirectedGraphBuilder<V, A> {

    /**
     * Creates a builder which contains a copy of the specified graph with all arrows inverted.
     *
     * @param <V>   the vertex data type
     * @param <A>   the arrow data type
     * @param graph a graph
     * @return a new graph with inverted arrows
     */
    public @NonNull <V, A> MutableDirectedGraph<V, A> inverseOfDirectedGraph(@NonNull DirectedGraph<V, A> graph) {
        final int arrowCount = graph.getArrowCount();

        SimpleMutableDirectedGraph<V, A> b = new SimpleMutableDirectedGraph<>(graph.getVertexCount(), arrowCount);
        for (V v : graph.getVertices()) {
            b.addVertex(v);
        }
        for (V v : graph.getVertices()) {
            for (int j = 0, m = graph.getNextCount(v); j < m; j++) {
                b.addArrow(graph.getNext(v, j), v, graph.getNextArrow(v, j));
            }
        }
        return b;
    }


    /**
     * Creates a builder which contains the specified vertices, and only arrows
     * from the directed graph, for the specified vertices.
     *
     * @param <V>             the vertex data type
     * @param <A>             the arrow data type
     * @param graph           a graph
     * @param vertexPredicate a predicate for the vertices
     * @return a subset of the directed graph
     */
    public @NonNull <V, A> MutableDirectedGraph<V, A> subsetOfDirectedGraph(@NonNull DirectedGraph<V, A> graph, @NonNull Predicate<V> vertexPredicate) {
        SimpleMutableDirectedGraph<V, A> b = new SimpleMutableDirectedGraph<>();
        for (V v : graph.getVertices()) {
            if (vertexPredicate.test(v)) {
                b.addVertex(v);
            }
        }
        for (V v : graph.getVertices()) {
            for (Arc<V, A> arc : graph.getNextArcs(v)) {
                if (vertexPredicate.test(arc.getEnd())) {
                    b.addArrow(v, arc.getEnd(), arc.getArrow());
                }
            }
        }
        return b;
    }


}
