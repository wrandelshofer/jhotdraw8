/*
 * @(#)IndexedDirectedGraph.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.enumerator.IntEnumerator;
import org.jhotdraw8.collection.enumerator.IntRangeEnumerator;

/**
 * Provides indexed read access to a directed graph {@code G = (V, A) }.
 * <ul>
 * <li>{@code G} is a tuple {@code (V, A) }.</li>
 * <li>{@code V} is the set of vertices with elements {@code v_i ∈ V. i ∈ {0, ..., vertexCount - 1} }.</li>
 * <li>{@code A} is the set of ordered pairs with elements {@code  (v_i, v_j)_k ∈ A. i,j ∈ {0, ..., vertexCount - 1}. k ∈ {0, ..., arrowCount - 1} }.</li>
 * </ul>
 * <p>
 * The API of this class provides access to the following data:
 * <ul>
 * <li>The vertex count {@code vertexCount}.</li>
 * <li>The arrow count {@code arrowCount}.</li>
 * <li>The index {@code i} of each vertex {@code v_i ∈ V}.</li>
 * <li>The index {@code k} of each arrow {@code a_k ∈ A}.</li>
 * <li>The next count {@code nextCount_i} of the vertex with index {@code i}.</li>
 * <li>The index of the {@code k}-th next vertex of the vertex with index {@code i}, and with {@code k ∈ {0, ..., nextCount_i - 1}}.</li>
 * </ul>
 *
 * @author Werner Randelshofer
 */
public interface IndexedDirectedGraph {

    /**
     * Returns the number of arrows.
     *
     * @return arrow count
     */
    int getArrowCount();

    /**
     * Returns the {@code i}-th next vertex of {@code v}.
     *
     * @param v a vertex index
     * @param i the index of the desired next vertex, {@code i ∈ {0, ..., getNextCount(v) -1 }}.
     * @return the vertex index of the i-th next vertex of v.
     */
    int getNextAsInt(int v, int i);

    /**
     * Returns the {@code i}-th next arrow of {@code v}.
     *
     * @param v a vertex index
     * @param i the index of the desired arrow, {@code i ∈ {0, ..., getNextCount(v) -1 }}.
     * @return the arrow data of the i-th next vertex of v.
     */
    int getNextArrowAsInt(int v, int i);


    /**
     * Returns the number of next vertices of v.
     *
     * @param v a vertex
     * @return the number of next vertices of v.
     */
    int getNextCount(int v);

    /**
     * Returns the number of vertices {@code V}.
     *
     * @return vertex count
     */
    int getVertexCount();

    /**
     * Returns the index of vertex b.
     *
     * @param v a vertex
     * @param u another vertex
     * @return index of vertex b. Returns a value {@literal < 0}
     * if b is not a next vertex of a.
     */
    default int findIndexOfNextAsInt(int v, int u) {
        for (int i = 0, n = getNextCount(v); i < n; i++) {
            if (u == getNextAsInt(v, i)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns whether there is an arrow from vertex {@code v} to vertex {@code u}.
     *
     * @param v a vertex
     * @param u another vertex
     * @return true if there is an arrow from {@code u} to {@code v}
     */
    default boolean isNextAsInt(int v, int u) {
        return findIndexOfNextAsInt(v, u) >= 0;
    }

    /**
     * Returns the direct successor vertices of the specified vertex.
     *
     * @param v a vertex index
     * @return a collection view on the direct successor vertices of vertex
     */
    default @NonNull IntEnumerator nextVerticesEnumerator(int v) {
        return new IntRangeEnumerator(i -> getNextAsInt(v, i), 0, getNextCount(v));
    }
}
