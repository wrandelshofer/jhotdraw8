/*
 * @(#)LongArrayEnumeratorSpliterator.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.enumerator;

import org.jhotdraw8.annotation.Nullable;

/**
 * A {@link LongSpliterator} over a {@code long}-array.
 *
 * @author Werner Randelshofer
 */
public class LongArraySpliterator extends AbstractLongEnumeratorSpliterator {
    private final int limit;
    private final long[] a;
    private int index;

    public LongArraySpliterator(long[] a, int from, int to) {
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
    public @Nullable LongArraySpliterator trySplit() {
        int lo = index, mid = (lo + limit) >>> 1;
        return (lo >= mid) ? null : // divide range in half unless too small
                new LongArraySpliterator(a, lo, index = mid);
    }
}
