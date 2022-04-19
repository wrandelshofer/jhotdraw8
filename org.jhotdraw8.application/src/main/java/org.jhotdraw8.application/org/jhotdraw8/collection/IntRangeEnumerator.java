/*
 * @(#)IntRangeEnumerator.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.util.function.IntToIntFunction;

/**
 * Enumerates integers in a given range.
 */
public class IntRangeEnumerator extends AbstractIntEnumerator {
    private int next;
    private final int to;
    private final @NonNull IntToIntFunction f;

    /**
     * Enumerates from 0 to {@code endExclusive}.
     *
     * @param endExclusive the end of the range + 1
     */
    public IntRangeEnumerator(int endExclusive) {
        this(i -> i, 0, endExclusive);
    }

    /**
     * Enumerates from {@code startInclusive} to {@code endExclusive}.
     *
     * @param startInclusive the start of the range
     * @param endExclusive   the end of the range + 1
     */
    public IntRangeEnumerator(int startInclusive, int endExclusive) {
        this(i -> i, startInclusive, endExclusive);
    }

    /**
     * Enumerates from {@code startInclusive} to {@code endExclusive}.
     *
     * @param startInclusive the start of the range
     * @param endExclusive   the end of the range + 1
     */
    public IntRangeEnumerator(@NonNull IntToIntFunction f, int startInclusive, int endExclusive) {
        super(endExclusive - startInclusive, NONNULL | DISTINCT | ORDERED | SIZED | SUBSIZED);
        this.f = f;
        this.next = startInclusive;
        this.to = endExclusive;
    }

    @Override
    public long estimateSize() {
        return to - next;
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
    public @Nullable IntRangeEnumerator trySplit() {
        int lo = next, mid = (lo + to) >>> 1;
        return (lo >= mid) ? null : // divide range in half unless too small
                new IntRangeEnumerator(f, lo, next = mid);
    }
}
