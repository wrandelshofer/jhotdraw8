/*
 * @(#)IntBidiGraph.java
 * Copyright © 2021 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.IntEnumerator;
import org.jhotdraw8.collection.IntRangeEnumerator;

/**
 * This interface provides read access to a directed graph {@code G = (int, A) }.
 * <p>
 * This interface provides access to the following data in addition to the data
 * that interface {@link DirectedGraph} provides:
 * <ul>
 * <li>The previous count {@code prevCount_i} of the vertex {@code v_i}.</li>
 * <li>The {@code k}-th previous vertex of the vertex {@code v_i}, with
 * {@code k ∈ {0, ..., getPrevCount(i) - 1}}.</li>
 * </ul>
 *
 * @author Werner Randelshofer
 */
public interface IndexedBidiGraph extends IndexedDirectedGraph {

    /**
     * Returns the {@code i}-th previous vertex of {@code v}.
     *
     * @param v index of vertex v
     * @param i index of ingoing arrow
     * @return the vertex index of the ingoing arrow
     */
    int getPrevAsInt(int v, int i);

    /**
     * Returns the arrow data of the {@code i}-th ingoing arrow
     * to {@code v}.
     *
     * @param v index of vertex v
     * @param i index of ingoing arrow
     * @return the arrow data of the ingoing arrow
     */
    int getPrevArrowAsInt(int v, int i);

    /**
     * Returns the number of direct predecessor vertices of v.
     *
     * @param v index of vertex v
     * @return the number of next vertices of v.
     */
    int getPrevCount(int v);

    /**
     * Returns the direct successor vertices of the specified vertex.
     *
     * @param v index of vertex v
     * @return a collection view on the direct successor vertices of vertex
     */
    default @NonNull IntEnumerator prevVerticesEnumerator(int v) {
        return new IntRangeEnumerator(i -> getPrevAsInt(v, i), 0, getPrevCount(v));
    }


    /**
     * Returns the index of the arrow from {@code v} to {@code u}
     * in the list of next-vertices from {@code v}.
     *
     * @param v vertex {@code v}
     * @param u vertex {@code u}
     * @return index of vertex {@code u} in the list of next-vertices from
     * {@code v}. Returns a value {@literal < 0} if {@code u} is not in
     * the list.
     */
    default int findIndexOfPrevAsInt(int v, int u) {
        for (int i = 0, n = getNextCount(v); i < n; i++) {
            if (u == getNextAsInt(v, i)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns whether there is an arrow from vertex {@code u} to vertex {@code v}.
     *
     * @param v a vertex
     * @param u a vertex
     * @return true if there is an arrow from {@code u} to {@code v}
     */
    default boolean isPrevAsInt(int v, int u) {
        return findIndexOfNextAsInt(v, u) >= 0;
    }

}
