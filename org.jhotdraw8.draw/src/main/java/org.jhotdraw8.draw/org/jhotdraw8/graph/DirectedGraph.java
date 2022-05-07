/*
 * @(#)DirectedGraph.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.Enumerator;
import org.jhotdraw8.collection.WrappedList;
import org.jhotdraw8.graph.iterator.VertexEnumerator;
import org.jhotdraw8.util.function.AddToSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Adds convenience methods to the interface defined in {@link BareDirectedGraph}.
 *
 * @param <V> the vertex data type
 * @param <A> the arrow data type
 * @author Werner Randelshofer
 */
public interface DirectedGraph<V, A> extends BareDirectedGraph<V, A> {

    /**
     * Returns the arrow data for the arrow going from {@code u} to {@code v}
     * if the arrow exists.
     *
     * @param u a vertex
     * @param v a vertex
     * @return the arrow data or null
     */
    default @Nullable A findArrow(@NonNull V u, @NonNull V v) {
        int index = findIndexOfNext(u, v);
        return index < 0 ? null : getNextArrow(u, index);
    }

    /**
     * Returns the index of vertex {@code u} in the list of next vertices
     * of {@code v} if an arrow from {@code v} to {@code u} exists.
     *
     * @param v a vertex
     * @param u a vertex
     * @return index of vertex {@code u} or a value {@literal < 0}
     */
    default int findIndexOfNext(@NonNull V v, @NonNull V u) {
        for (int i = 0, n = getNextCount(v); i < n; i++) {
            if (u.equals(getNext(v, i))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns the list of next vertices of vertex {@code v}.
     *
     * @param v a vertex
     * @return a collection view on the direct successor vertices of vertex
     */
    default @NonNull Collection<V> getNextVertices(@NonNull V v) {
        return new WrappedList<>(() -> this.getNextCount(v), i -> getNext(v, i));
    }

    /**
     * Returns the arc data for the {@code i}-th next (outgoing)
     * arrow from vertex {@code v}.
     *
     * @param v a vertex
     * @param i the index into the list of outgoing arrows
     * @return the arc data
     */
    default @NonNull Arc<V, A> getNextArc(@NonNull V v, int i) {
        return new Arc<>(v, getNext(v, i), getNextArrow(v, i));
    }

    /**
     * Returns the list of next arrow data of vertex {@code v}.
     *
     * @param v a vertex
     * @return a collection view on the arrow data
     */
    default @NonNull Collection<A> getNextArrows(@NonNull V v) {
        return new WrappedList<>(() -> this.getNextCount(v), i -> getNextArrow(v, i));
    }

    /**
     * Returns the list of next arc data of vertex {@code v}.
     *
     * @param v a vertex
     * @return a collection view on the arc data
     */
    default @NonNull Collection<Arc<V, A>> getNextArcs(@NonNull V v) {
        return new WrappedList<>(() -> this.getNextCount(v), i -> getNextArc(v, i));
    }

    /**
     * Returns the number of vertices.
     *
     * @return vertex count
     */
    default int getVertexCount() {
        return getVertices().size();
    }

    /**
     * Returns the number of arrows.
     *
     * @return arrow count
     */
    int getArrowCount();

    /**
     * Returns all arrows between two vertices.
     *
     * @param v1 vertex 1
     * @param v2 vertex 2
     * @return a collection of all arrows
     */
    default @NonNull Collection<A> getArrows(@NonNull V v1, V v2) {
        int n = getNextCount(v1);
        List<A> arrows = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            if (getNext(v1, i).equals(v2)) {
                arrows.add(getNextArrow(v1, i));
            }
        }
        return Collections.unmodifiableList(arrows);
    }

    /**
     * Returns all arrows.
     *
     * @return a collection of all arrows
     */
    default @NonNull Collection<A> getArrows() {
        ArrayList<A> arrows = new ArrayList<>(getArrowCount());
        for (V v1 : getVertices()) {
            int n = getNextCount(v1);
            for (int i = 0; i < n; i++) {
                arrows.add(getNextArrow(v1, i));
            }
        }
        return Collections.unmodifiableList(arrows);
    }

    /**
     * Returns true if there is an arrow from vertex {@code v} to
     * vertex {@code u}.
     *
     * @param v a vertex
     * @param u a vertex
     * @return true if {@code u} is next of {@code v}
     */
    default boolean isNext(@NonNull V v, @NonNull V u) {
        return findIndexOfNext(v, u) != -1;
    }

    /**
     * Gets the vertex data at the specified index.
     *
     * @param index an index
     * @return vertex data
     * @throws IndexOutOfBoundsException if the index is out of bounds
     */
    @NonNull V getVertex(int index);

    /**
     * Searches for vertices starting at the provided vertex.
     *
     * @param start the start vertex
     * @param dfs   whether to search depth-first instead of breadth-first
     * @return breadth first search
     */
    default @NonNull Enumerator<V> searchNextVertices(final @NonNull V start, final boolean dfs) {
        final Set<V> visited = new HashSet<>();
        return searchNextVertices(start, visited::add, dfs);
    }

    /**
     * Searches for vertices starting at the provided vertex.
     *
     * @param start   the start vertex
     * @param visited the add method of the visited set, see {@link Set#add}.
     * @param dfs     whether to search depth-first instead of breadth-first
     * @return breadth first search
     */
    default @NonNull Enumerator<V> searchNextVertices(final @NonNull V start, final @NonNull AddToSet<V> visited, final boolean dfs) {
        return new VertexEnumerator<V>(this::getNextVertices, start, visited, dfs);
    }
}
