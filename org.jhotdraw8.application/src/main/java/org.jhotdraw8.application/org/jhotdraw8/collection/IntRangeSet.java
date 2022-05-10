/*
 * @(#)IntRangeSet.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.util.Preconditions;

import java.util.Iterator;
import java.util.stream.IntStream;

/**
 * Represents a set of integers in a given range.
 */
public class IntRangeSet extends AbstractReadOnlySet<Integer> {
    private final int from;
    private final int to;

    /**
     * Creates a new instance.
     *
     * @param from from inclusive
     * @param to   exclusive
     */
    public IntRangeSet(int from, int to) {
        Preconditions.checkIndex(from, to);
        this.from = from;
        this.to = to;
    }

    @Override
    public @NonNull Iterator<Integer> iterator() {
        return IntStream.range(from, to).iterator();
    }

    @Override
    public int size() {
        return to - from;
    }

    @Override
    public boolean contains(@Nullable Object o) {
        if (o instanceof Integer) {
            int e = ((int) o);
            return from <= e && e < to;
        }
        return false;
    }
}
