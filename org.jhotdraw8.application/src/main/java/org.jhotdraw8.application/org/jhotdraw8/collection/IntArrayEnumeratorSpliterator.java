package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.Nullable;

/**
 * An integer enumerator/spliterator over an integer array.
 */
public class IntArrayEnumeratorSpliterator extends AbstractIntEnumeratorSpliterator {
    private final int limit;
    private final int[] arrows;
    private int index;

    public IntArrayEnumeratorSpliterator(int lo, int hi, int[] arrows) {
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

    public @Nullable IntArrayEnumeratorSpliterator trySplit() {
        int lo = index, mid = (lo + limit) >>> 1;
        return (lo >= mid) ? null : // divide range in half unless too small
                new IntArrayEnumeratorSpliterator(lo, index = mid, arrows);
    }

}
