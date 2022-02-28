/*
 * @(#)DirectedGraphBuilder.java
 * Copyright © 2021 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph;

import org.jhotdraw8.annotation.NonNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;


/**
 * DirectedGraphBuilder.
 *
 * @author Werner Randelshofer
 */
public class DirectedGraphBuilder {
    public DirectedGraphBuilder() {
    }

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

    /**
     * Adds the source graph to the target graph.
     *
     * @param source the source graph
     * @param target the target graph
     * @param <V>    the vertex type
     * @param <A>    the arrow type
     */
    public <V, A> void addAll(final @NonNull DirectedGraph<V, A> source, final @NonNull MutableDirectedGraph<V, A> target) {
        for (V v : source.getVertices()) {
            target.addVertex(v);
        }
        for (V v : source.getVertices()) {
            for (Arc<V, A> arc : source.getNextArcs(v)) {
                target.addArrow(arc.getStart(), arc.getEnd(), arc.getArrow());
            }
        }
    }

    /**
     * Adds the source graph to the target graph.
     *
     * @param source the source graph
     * @param target the target graph
     * @param <V>    the vertex type of the target graph
     * @param <A>    the arrow type of the target graph
     * @param <VV>   the vertex type of the source graph
     * @param <AA>   the arrow type of the target graph
     */
    public <V, A, VV, AA> void addAll(final DirectedGraph<VV, AA> source, final @NonNull MutableDirectedGraph<V, A> target,
                                      @NonNull final Function<VV, V> vertexMapper, @NonNull final Function<AA, A> arrowMapper) {
        LinkedHashMap<VV, V> vertexMap = new LinkedHashMap<>(2 * target.getVertexCount());
        for (final VV vv : source.getVertices()) {
            V v = vertexMapper.apply(vv);
            vertexMap.put(vv, v);
            target.addVertex(v);
        }
        for (Map.Entry<VV, V> entry : vertexMap.entrySet()) {
            VV vv = entry.getKey();
            V v = entry.getValue();
            for (int i = 0, n = source.getNextCount(vv); i < n; i++) {
                target.addArrow(v, vertexMapper.apply(source.getNext(vv, i)), arrowMapper.apply(source.getNextArrow(vv, i)));
            }
        }
    }

}
