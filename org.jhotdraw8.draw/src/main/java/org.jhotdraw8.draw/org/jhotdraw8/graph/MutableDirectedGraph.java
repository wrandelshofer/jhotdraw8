/*
 * @(#)MutableDirectedGraph.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.graph;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

public interface MutableDirectedGraph<V, A> extends DirectedGraph<V, A> {
    /**
     * Adds a vertex to the graph.
     *
     * @param v vertex data
     * @throws IllegalStateException if the vertex is already in the graph
     */
    void addVertex(@NonNull V v);

    /**
     * Removes a vertex from the graph.
     *
     * @param v vertex data
     * @throws IllegalStateException if the vertex is not in the graph
     */
    void removeVertex(@NonNull V v);

    /**
     * Adds an arrow from vertex v to vertex u.
     * <p>
     * This method adds additional an arrow if the arrow is already in the graph.
     *
     * @param v vertex data v
     * @param u vertex data u
     * @param a arrow data
     * @throws IllegalStateException if v or u is not in the graph
     */
    void addArrow(@NonNull V v, @NonNull V u, @Nullable A a);

    /**
     * Removes the first arrow from vertex v to vertex u that has
     * the same data value.
     *
     * @param v    vertex data v
     * @param u    vertex data u
     * @param a arrow data
     * @throws IllegalStateException if the arrow is not in the graph
     */
    void removeArrow(@NonNull V v, @NonNull V u, @Nullable A a);

    /**
     * Removes the first arrow from vertex v to vertex u.
     *
     * @param v vertex data v
     * @param u vertex data u
     * @throws IllegalStateException if the arrow is not in the graph
     */
    void removeArrow(@NonNull V v, @NonNull V u);

    /**
     * Removes the k-th next arrow from vertex v.
     *
     * @param v vertex data v
     * @param k index of arrow to be removed
     * @throws IllegalStateException     if the arrow is not in the graph
     * @throws IndexOutOfBoundsException if the index is out of bounds
     */
    void removeNext(@NonNull V v, int k);
}
