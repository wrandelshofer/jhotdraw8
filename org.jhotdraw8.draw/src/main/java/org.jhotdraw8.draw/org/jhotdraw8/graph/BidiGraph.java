/*
 * @(#)BidiGraph.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.Enumerator;
import org.jhotdraw8.collection.WrappedList;
import org.jhotdraw8.graph.iterator.VertexEnumerator;
import org.jhotdraw8.util.function.AddToSet;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Adds convenience methods to the API defined in {@link BareBidiGraph}.
 *
 * @param <V> the vertex data type
 * @param <A> the arrow data type
 * @author Werner Randelshofer
 */
public interface BidiGraph<V, A> extends DirectedGraph<V, A>, BareBidiGraph<V, A> {

    /**
     * Returns the list of previous (incoming) arrows of vertex {@code v}.
     *
     * @param v a vertex
     * @return a collection view on the previous arrows
     */
    default @NonNull Collection<A> getPrevArrows(@NonNull V v) {
        return new WrappedList<>(() -> this.getPrevCount(v), i -> getPrevArrow(v, i));
    }

    /**
     * Returns the list of next vertices of vertex {@code v}.
     *
     * @param v a vertex
     * @return a collection view on the direct successor vertices of vertex
     */
    default @NonNull Collection<V> getPrevVertices(@NonNull V v) {
        return new WrappedList<>(() -> this.getPrevCount(v), i -> getPrev(v, i));
    }

    /**
     * Returns the arc data for the {@code i}-th previous (incoming)
     * arrow from vertex {@code v}.
     *
     * @param v a vertex
     * @param i the index into the list of outgoing arrows
     * @return the arc data
     */
    default @NonNull Arc<V, A> getPrevArc(@NonNull V v, int i) {
        return new Arc<>(getPrev(v, i), v, getPrevArrow(v, i));
    }

    /**
     * Returns the list of previous arc data of vertex {@code v}.
     *
     * @param v a vertex
     * @return a collection view on the arc data
     */
    default @NonNull Collection<Arc<V, A>> getPrevArcs(@NonNull V v) {
        return new WrappedList<>(() -> this.getPrevCount(v), i -> getPrevArc(v, i));
    }

    /**
     * Returns the index of vertex {@code u} in the list of previous vertices
     * of {@code v} if an arrow from {@code u} to {@code v} exists.
     *
     * @param v a vertex
     * @param u a vertex
     * @return index of vertex {@code u} or a value {@literal < 0}
     */
    default int findIndexOfPrev(final @NonNull V v, final @NonNull V u) {
        for (int i = 0, n = getPrevCount(v); i < n; i++) {
            if (u.equals(getPrev(v, i))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns whether there is an arrow from vertex {@code u}
     * to vertex {@code v}.
     *
     * @param v a vertex
     * @param u a vertex
     * @return true if {@code u} is previous of {@code v}
     */
    default boolean isPrev(final @NonNull V v, final @NonNull V u) {
        return findIndexOfPrev(v, u) >= 0;
    }

    /**
     * Searches for vertices starting at the provided vertex.
     *
     * @param start the start vertex
     * @param dfs   whether to search depth-first instead of breadth-first
     * @return breadth first search
     */
    default @NonNull Enumerator<V> searchPrevVertices(final @NonNull V start, final boolean dfs) {
        final Set<V> visited = new HashSet<>();
        return searchPrevVertices(start, visited::add, dfs);
    }

    /**
     * Searches for vertices starting at the provided vertex.
     *
     * @param start   the start vertex
     * @param visited the add method of the visited set, see {@link Set#add}.
     * @param dfs     whether to search depth-first instead of breadth-first
     * @return breadth first search
     */
    default @NonNull Enumerator<V> searchPrevVertices(final @NonNull V start, final @NonNull AddToSet<V> visited, final boolean dfs) {
        return new VertexEnumerator<V>(this::getPrevVertices, start, visited, dfs);
    }

}
