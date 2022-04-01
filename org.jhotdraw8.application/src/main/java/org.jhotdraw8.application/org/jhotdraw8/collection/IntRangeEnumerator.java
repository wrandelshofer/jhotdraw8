/*
 * @(#)IntRangeEnumerator.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;

import org.jhotdraw8.util.function.IntToIntFunction;

/**
 * Enumerates integers in a given range.
 */
public class IntRangeEnumerator extends AbstractIntEnumerator {
    private int next;
    private final int to;
    private final IntToIntFunction f;

    /**
     * Enumerates from 0 to {@code toExclusive}.
     *
     * @param to the end of the range + 1
     */
    public IntRangeEnumerator(int to) {
        this(i -> i, 0, to);
    }

    /**
     * Enumerates from {@code fromInclusive}. to {@code toExclusive}.
     *
     * @param from the start of the range
     * @param to   the end of the range + 1
     */
    public IntRangeEnumerator(int from, int to) {
        this(i -> i, from, to);
    }

    /**
     * Enumerates from {@code fromInclusive}. to {@code toExclusive}.
     *
     * @param from the start of the range
     * @param to   the end of the range + 1
     */
    public IntRangeEnumerator(IntToIntFunction f, int from, int to) {
        super(to - from, ORDERED | SIZED | SUBSIZED);
        this.f = f;
        this.next = from;
        this.to = to;
    }

    @Override
    public boolean moveNext() {
        if (next < to) {
            current = f.applyAsInt(next);
            next++;
            return true;
        }
        return false;
    }

    @Override
    public OfInt trySplit() {
        int lo = next, mid = (lo + to) >>> 1;
        return (lo >= mid) ? null : // divide range in half unless too small
                new IntRangeEnumerator(lo, next = mid);
    }
}
