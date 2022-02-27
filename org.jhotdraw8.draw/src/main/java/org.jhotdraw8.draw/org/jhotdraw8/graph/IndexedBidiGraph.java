/*
 * @(#)IntBidiGraph.java
 * Copyright © 2021 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.graph;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.AbstractIntEnumeratorSpliterator;
import org.jhotdraw8.collection.IntEnumeratorSpliterator;

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
     * Returns the i-th direct predecessor vertex of v.
     *
     * @param vidx index of vertex v
     * @param i    index of next vertex
     * @return the i-th next vertex of v
     */
    int getPrevAsInt(int vidx, int i);

    /**
     * Returns the number of direct predecessor vertices of v.
     *
     * @param vidx index of vertex v
     * @return the number of next vertices of v.
     */
    int getPrevCount(int vidx);

    /**
     * Returns the direct successor vertices of the specified vertex.
     *
     * @param vidx index of vertex v
     * @return a collection view on the direct successor vertices of vertex
     */
    default @NonNull IntEnumeratorSpliterator prevVerticesSpliterator(int vidx) {
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
                    current = getPrevAsInt(vidx, index++);
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
        return new MySpliterator(vidx, 0, getPrevCount(vidx));
    }


    /**
     * Returns the index of vertex b.
     *
     * @param vidx index of vertex a
     * @param uidx index of vertex b
     * @return index of vertex b. Returns a value {@literal < 01 if b is not a
     * previous vertex of a.
     */
    default int findIndexOfPrevAsInt(int vidx, int uidx) {
        for (int i = 0, n = getNextCount(vidx); i < n; i++) {
            if (uidx == getNextAsInt(vidx, i)) {
                return i;
            }
        }
        return -1;
    }
}
