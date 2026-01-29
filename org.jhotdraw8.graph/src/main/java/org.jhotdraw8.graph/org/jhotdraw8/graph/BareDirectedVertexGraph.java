/*
 * @(#)BareDirectedVertexGraph.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph;


import java.util.Set;

/**
 * Provides minimalistic (bare) read-access to a directed graph.
 * <p>
 * A directed graph is a tuple {@code G = (V, A)}
 * <br>where {@code V} is a set of vertices {@code { v[0], ..., v[n-1] }}
 * <br>and {@code A} is a bag of ordered pairs {@code { (v[i], v[j]), ... } }.
 * <p>
 * This interface supports an arbitrary data type {@literal <V>} for the vertices of the graph.
 * <p>
 * Use the interface {@link BareDirectedGraph} if you also need support for
 * the arrow data type {@literal <A>} of a graph.
 *
 * @param <V> the vertex data type
 */
public interface BareDirectedVertexGraph<V> {
    /**
     * Returns the next vertex associated with
     * the specified vertex and outgoing arrow index.
     *
     * @param v     a vertex
     * @param index index of outgoing arrow
     * @return the next vertex
     * @see #getNextCount
     */
    V getNext(V v, int index);

    /**
     * Returns the number of next vertices at the specified vertex.
     * <p>
     * This number is the same as the number of outgoing arrows at the specified
     * vertex.
     *
     * @param v a vertex
     * @return the number of next vertices
     */
    int getNextCount(V v);

    /**
     * Returns all vertices.
     *
     * @return a set view on all vertices
     */
    Set<V> getVertices();

}
