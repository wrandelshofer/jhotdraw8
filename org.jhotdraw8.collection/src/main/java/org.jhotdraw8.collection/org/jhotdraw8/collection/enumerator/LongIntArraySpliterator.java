/*
 * @(#)LongIntArrayEnumeratorSpliterator.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.enumerator;

import org.jhotdraw8.annotation.Nullable;

/**
 * A {@link LongSpliterator} over a {@code int}-array.
 * Supports shifting and masking of the {@code int}-values.
 *
 * @author Werner Randelshofer
 */
public class LongIntArraySpliterator extends AbstractIntEnumeratorSpliterator {
    private final int limit;
    private final long[] a;
    private int index;
    private final int shift;
    private final long mask;

    public LongIntArraySpliterator(long[] a, int from, int to, int shift, long mask) {
        super(to - from, ORDERED | NONNULL | SIZED | SUBSIZED);
        limit = to;
        index = from;
        this.a = a;
        this.shift = shift;
        this.mask = mask;
    }

    private int toIntValue(long longValue) {
        return (int) ((longValue >>> shift) & mask);
    }

    @Override
    public boolean moveNext() {
        if (index < limit) {
            current = toIntValue(a[index++]);
            return true;
        }
        return false;
    }

    @Override
    public @Nullable LongIntArraySpliterator trySplit() {
        int lo = index, mid = (lo + limit) >>> 1;
        return (lo >= mid) ? null : // divide range in half unless too small
                new LongIntArraySpliterator(a, lo, index = mid, shift, mask);
    }

}
