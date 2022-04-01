package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.Nullable;

/**
 * An integer enumerator/spliterator over an integer array.
 */
public class IntArrayEnumerator extends AbstractIntEnumerator {
    private final int limit;
    private final int[] arrows;
    private int index;

    public IntArrayEnumerator(int lo, int hi, int[] arrows) {
        super(hi - lo, ORDERED | NONNULL | SIZED | SUBSIZED);
        limit = hi;
        index = lo;
        this.arrows = arrows;
    }

    @Override
    public boolean moveNext() {
        if (index < limit) {
            current = arrows[index++];
            return true;
        }
        return false;
    }

    @Override
    public long estimateSize() {
        return limit - index;
    }

    public @Nullable IntArrayEnumerator trySplit() {
        int lo = index, mid = (lo + limit) >>> 1;
        return (lo >= mid) ? null : // divide range in half unless too small
                new IntArrayEnumerator(lo, index = mid, arrows);
    }

}
