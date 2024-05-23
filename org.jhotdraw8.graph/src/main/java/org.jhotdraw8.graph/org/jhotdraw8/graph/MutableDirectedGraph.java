/*
 * @(#)MutableDirectedGraph.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph;

import org.jspecify.annotations.Nullable;

/**
 * Interface for a mutable directed graph.
 *
 * @param <V> the vertex type
 * @param <A> the arrow data type
 */
public interface MutableDirectedGraph<V, A> extends DirectedGraph<V, A> {
    /**
     * Adds a vertex to the graph if it is not already in the graph.
     *
     * @param v vertex data
     */
    void addVertex(V v);

    /**
     * Removes a vertex from the graph if it is in the graph.
     *
     * @param v vertex data
     */
    void removeVertex(V v);

    /**
     * Adds an arrow from vertex v to vertex u.
     * <p>
     * This method adds additional an arrow if the arrow is already in the graph.
     *
     * @param v vertex data v
     * @param u vertex data u
     * @param a arrow data
     */
    void addArrow(V v, V u, @Nullable A a);

    /**
     * Removes the first arrow from vertex v to vertex u that has
     * the same data value.
     *
     * @param v vertex data v
     * @param u vertex data u
     * @param a arrow data
     */
    void removeArrow(V v, V u, @Nullable A a);

    /**
     * Removes the first arrow from vertex v to vertex u.
     *
     * @param v vertex data v
     * @param u vertex data u
     */
    void removeArrow(V v, V u);

    /**
     * Removes the k-th next arrow from vertex v.
     *
     * @param v vertex data v
     * @param k index of arrow to be removed
     */
    void removeNext(V v, int k);
}
