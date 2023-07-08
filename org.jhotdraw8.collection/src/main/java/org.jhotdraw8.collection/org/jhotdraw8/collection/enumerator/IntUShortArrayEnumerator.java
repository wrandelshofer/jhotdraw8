/*
 * @(#)IntUShortArraySpliterator.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.enumerator;

import org.jhotdraw8.annotation.Nullable;

/**
 * An {@link Enumerator.OfInt} over an unsigned short array.
 */
public class IntUShortArrayEnumerator extends AbstractIntEnumerator {
    private final int limit;
    private final short[] arrows;
    private int index;

    public IntUShortArrayEnumerator(int lo, int hi, short[] arrows) {
        super(hi - lo, ORDERED | NONNULL | SIZED | SUBSIZED);
        limit = hi;
        index = lo;
        this.arrows = arrows;
    }

    @Override
    public boolean moveNext() {
        if (index < limit) {
            current = arrows[index++] & 0xffff;
            return true;
        }
        return false;
    }

    @Override
    public @Nullable IntUShortArrayEnumerator trySplit() {
        int lo = index, mid = (lo + limit) >>> 1;
        return (lo >= mid) ? null : // divide range in half unless too small
                new IntUShortArrayEnumerator(lo, index = mid, arrows);
    }

}
