package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.Nullable;

/**
 * An integer enumerator/spliterator over an integer array.
 */
public class LongIntArrayEnumeratorSpliterator extends AbstractIntEnumeratorSpliterator {
    private final int limit;
    private final long[] arrows;
    private int index;
    private final int shift;
    private final long mask;

    public LongIntArrayEnumeratorSpliterator(int lo, int hi, long[] arrows, int shift, long mask) {
        super(hi - lo, ORDERED | NONNULL | SIZED | SUBSIZED);
        limit = hi;
        index = lo;
        this.arrows = arrows;
        this.shift = shift;
        this.mask = mask;
    }

    private int getArrowIndex(long struct) {
        return (int) ((struct >>> shift) & mask);
    }

    @Override
    public boolean moveNext() {
        if (index < limit) {
            current = getArrowIndex(arrows[index++]);
            return true;
        }
        return false;
    }

    public @Nullable LongIntArrayEnumeratorSpliterator trySplit() {
        int lo = index, mid = (lo + limit) >>> 1;
        return (lo >= mid) ? null : // divide range in half unless too small
                new LongIntArrayEnumeratorSpliterator(lo, index = mid, arrows, shift, mask);
    }

}
