package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.Nullable;

/**
 * An integer enumerator/spliterator over an unsigned short array.
 */
public class IntUShortArrayEnumeratorSpliterator extends AbstractIntEnumeratorSpliterator {
    private final int limit;
    private final short[] arrows;
    private int index;

    public IntUShortArrayEnumeratorSpliterator(int lo, int hi, short[] arrows) {
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

    public @Nullable IntUShortArrayEnumeratorSpliterator trySplit() {
        int lo = index, mid = (lo + limit) >>> 1;
        return (lo >= mid) ? null : // divide range in half unless too small
                new IntUShortArrayEnumeratorSpliterator(lo, index = mid, arrows);
    }

}
