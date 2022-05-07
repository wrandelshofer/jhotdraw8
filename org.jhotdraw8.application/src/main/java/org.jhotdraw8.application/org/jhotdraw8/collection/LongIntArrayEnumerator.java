/*
 * @(#)LongIntArrayEnumerator.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.Nullable;

/**
 * A {@link LongEnumerator} over a {@code int}-array.
 * Supports shifting and masking of the {@code int}-values.
 *
 * @author Werner Randelshofer
 */
public class LongIntArrayEnumerator extends AbstractIntEnumerator {
    private final int limit;
    private final long[] a;
    private int index;
    private final int shift;
    private final long mask;

    public LongIntArrayEnumerator(long[] a, int from, int to, int shift, long mask) {
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

    public @Nullable LongIntArrayEnumerator trySplit() {
        int lo = index, mid = (lo + limit) >>> 1;
        return (lo >= mid) ? null : // divide range in half unless too small
                new LongIntArrayEnumerator(a, lo, index = mid, shift, mask);
    }

}
