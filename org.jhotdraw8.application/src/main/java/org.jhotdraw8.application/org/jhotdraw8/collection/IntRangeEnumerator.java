/*
 * @(#)IntRangeEnumerator.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;

/**
 * Enumerates integers in a given range.
 */
public class IntRangeEnumerator extends AbstractIntEnumeratorSpliterator {
    private int next;
    private final int toExclusive;

    /**
     * Enumerates from 0 to {@code toExclusive}.
     *
     * @param toExclusive the end of the range + 1
     */
    public IntRangeEnumerator(int toExclusive) {
        this(0, toExclusive);
    }

    /**
     * Enumerates from {@code fromInclusive}. to {@code toExclusive}.
     *
     * @param fromInclusive the start of the range
     * @param toExclusive   the end of the range + 1
     */
    public IntRangeEnumerator(int fromInclusive, int toExclusive) {
        super(toExclusive - fromInclusive, ORDERED | SIZED | SUBSIZED);
        this.next = fromInclusive;
        this.toExclusive = toExclusive;
    }

    @Override
    public boolean moveNext() {
        if (next < toExclusive) {
            current = next;
            next++;
            return true;
        }
        return false;
    }

    @Override
    public OfInt trySplit() {
        int lo = next, mid = (lo + toExclusive) >>> 1;
        return (lo >= mid) ? null : // divide range in half unless too small
                new IntRangeEnumerator(lo, next = mid);
    }
}
