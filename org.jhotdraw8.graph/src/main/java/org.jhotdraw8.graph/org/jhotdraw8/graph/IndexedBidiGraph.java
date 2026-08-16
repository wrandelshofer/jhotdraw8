/*
 * @(#)IndexedBidiGraph.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph;

import org.jhotdraw8.collection.enumerator.Enumerator;
import org.jhotdraw8.collection.enumerator.IntRangeEnumerator;

/// This interface provides read access to a directed graph `G = (int, A)`.
///
/// This interface provides access to the following data in addition to the data
/// that interface [DirectedGraph] provides:
///
///   - The previous count `prevCount_i` of the vertex `v_i`.
///   - The `k`-th previous vertex of the vertex `v_i`, with
///     `k ∈{0, ..., getPrevCount(i) - 1}`.
///
public interface IndexedBidiGraph extends IndexedDirectedGraph {

    /// Returns the `i`-th previous vertex of `v`.
    ///
    /// @param v index of vertex v
    /// @param i index of ingoing arrow
    /// @return the vertex index of the ingoing arrow
    int getPrevAsInt(int v, int i);

    /// Returns the arrow data of the `i`-th ingoing arrow
    /// to `v`.
    ///
    /// @param v index of vertex v
    /// @param i index of ingoing arrow
    /// @return the arrow data of the ingoing arrow
    int getPrevArrowAsInt(int v, int i);

    /// Returns the number of direct predecessor vertices of v.
    ///
    /// @param v index of vertex v
    /// @return the number of next vertices of v.
    int getPrevCount(int v);

    /// Returns the direct successor vertices of the specified vertex.
    ///
    /// @param v index of vertex v
    /// @return a collection view on the direct successor vertices of vertex
    default Enumerator.OfInt prevVerticesEnumerator(int v) {
        return new IntRangeEnumerator(i -> getPrevAsInt(v, i), 0, getPrevCount(v));
    }


    /// Returns the index of the arrow from `v` to `u`
    /// in the list of next-vertices from `v`.
    ///
    /// @param v vertex `v`
    /// @param u vertex `u`
    /// @return index of vertex `u` in the list of next-vertices from
    /// `v`. Returns a value {@literal < 0} if `u` is not in
    /// the list.
    default int findIndexOfPrevAsInt(int v, int u) {
        for (int i = 0, n = getNextCount(v); i < n; i++) {
            if (u == getNextAsInt(v, i)) {
                return i;
            }
        }
        return -1;
    }

    /// Returns whether there is an arrow from vertex `u` to vertex `v`.
    ///
    /// @param v a vertex
    /// @param u a vertex
    /// @return true if there is an arrow from `u` to `v`
    default boolean isPrevAsInt(int v, int u) {
        return findIndexOfNextAsInt(v, u) >= 0;
    }

}
