/*
 * @(#)IntArraySpliterator.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.enumerator;

import org.jspecify.annotations.Nullable;

/**
 * An integer enumerator/spliterator over an integer array.
 */
public class IntArrayEnumerator extends AbstractIntEnumerator {
    private final int limit;
    private final int[] a;
    private int index;

    /**
     * Creates a new instance that iterates over the specified
     * array in the specified range.
     *
     * @param a    the array
     * @param from the start index of the range (inclusive)
     * @param to   the end index of the range (exclusive)
     */
    public IntArrayEnumerator(int[] a, int from, int to) {
        super(to - from, ORDERED | NONNULL | SIZED | SUBSIZED);
        limit = to;
        index = from;
        this.a = a;
    }

    @Override
    public boolean moveNext() {
        if (index < limit) {
            current = a[index++];
            return true;
        }
        return false;
    }

    @Override
    public long estimateSize() {
        return limit - index;
    }

    @Override
    public @Nullable IntArrayEnumerator trySplit() {
        // divide range in half unless too small
        int lo = index, mid = (lo + limit) >>> 1;
        return (lo >= mid) ? null
                : new IntArrayEnumerator(a, lo, index = mid);
    }

}
