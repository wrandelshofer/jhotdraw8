/*
 * @(#)BareDirectedVertexGraph.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph;

import org.jhotdraw8.annotation.NonNull;

import java.util.Set;

/**
 * Provides a minimal read-only interface for a directed graph.
 * <p>
 * A directed graph is a tuple {@code G = (V, A)} where {@code V} is a set of
 * vertices and {@code A} is a set or bag of arrows.
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
    @NonNull V getNext(@NonNull V v, int index);

    /**
     * Returns the number of next vertices at the specified vertex.
     * <p>
     * This number is the same as the number of outgoing arrows at the specified
     * vertex.
     *
     * @param v a vertex
     * @return the number of next vertices
     */
    int getNextCount(@NonNull V v);

    /**
     * Returns all vertices.
     *
     * @return a set view on all vertices
     */
    @NonNull Set<V> getVertices();

}
