/*
 * @(#)LongArrayEnumerator.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.Nullable;

/**
 * A {@link LongEnumerator} over a {@code long}-array.
 *
 * @author Werner Randelshofer
 */
public class LongArrayEnumerator extends AbstractLongEnumerator {
    private final int limit;
    private final long[] a;
    private int index;

    public LongArrayEnumerator(long[] a, int from, int to) {
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
    public @Nullable LongArrayEnumerator trySplit() {
        int lo = index, mid = (lo + limit) >>> 1;
        return (lo >= mid) ? null : // divide range in half unless too small
                new LongArrayEnumerator(a, lo, index = mid);
    }
}
