package org.jhotdraw8.graph;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

public interface MutableDirectedGraph<V, A> extends DirectedGraph<V, A> {
    /**
     * Adds a vertex to the graph.
     */
    void addVertex(@NonNull V v);

    /**
     * Removes a vertex from the graph.
     *
     * @param v vertex v
     */
    void removeVertex(@NonNull V v);

    /**
     * Adds an arrow from vertex v to vertex u.
     *
     * @param v    vertex v
     * @param u    vertex u
     * @param data arrow data
     */
    void addArrow(@NonNull V v, @NonNull V u, @Nullable A data);

    /**
     * Removes an arrow from vertex v to vertex u.
     *
     * @param v    vertex v
     * @param u    vertex u
     * @param data arrow data
     */
    void removeArrow(@NonNull V v, @NonNull V u, @Nullable A data);

    /**
     * Removes an arrow from vertex v to vertex u.
     *
     * @param v vertex v
     * @param u vertex u
     */
    void removeArrow(@NonNull V v, @NonNull V u);

    /**
     * Removes the k-th outgoing arrow from vertex v.
     *
     * @param v vertex v
     * @param k index of arrow to be removed
     */
    void removeArrowAt(@NonNull V v, int k);
}
