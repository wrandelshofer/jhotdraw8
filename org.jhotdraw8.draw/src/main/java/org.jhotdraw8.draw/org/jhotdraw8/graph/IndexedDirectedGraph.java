/*
 * @(#)IntDirectedGraph.java
 * Copyright © 2021 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.AbstractIntEnumeratorSpliterator;
import org.jhotdraw8.collection.IntEnumeratorSpliterator;

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
     * Returns the k-th next vertex of v.
     *
     * @param v     a vertex index
     * @param index the index of the desired next vertex, {@code k ∈ {0, ..., getNextCount(v) -1 }}.
     * @return the index of the k-th next vertex of v.
     */
    int getNextAsInt(int v, int index);

    int getNextArrowAsInt(int v, int index);


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
    default boolean isNext(int v, int u) {
        return findIndexOfNextAsInt(v, u) >= 0;
    }

    /**
     * Returns the direct successor vertices of the specified vertex.
     *
     * @param v a vertex index
     * @return a collection view on the direct successor vertices of vertex
     */
    default @NonNull IntEnumeratorSpliterator nextVerticesSpliterator(int v) {
        class MySpliterator extends AbstractIntEnumeratorSpliterator {
            private int index;
            private final int limit;
            private final int vidx;

            public MySpliterator(int vidx, int lo, int hi) {
                super(hi - lo, ORDERED | NONNULL | SIZED | SUBSIZED);
                limit = hi;
                index = lo;
                this.vidx = vidx;
            }

            @Override
            public boolean moveNext() {
                if (index < limit) {
                    current = getNextAsInt(vidx, index++);
                    return true;
                }
                return false;
            }

            public @Nullable MySpliterator trySplit() {
                int hi = limit, lo = index, mid = (lo + hi) >>> 1;
                return (lo >= mid) ? null : // divide range in half unless too small
                        new MySpliterator(vidx, lo, index = mid);
            }

        }
        return new MySpliterator(v, 0, getNextCount(v));
    }
}
