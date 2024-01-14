package org.jhotdraw8.icollection.impl.iteration;

import java.util.Iterator;

public class IntRangeIterator implements Iterator<Integer> {
    private int from;
    private final int toInclusive;
    private final int step;

    public IntRangeIterator(int from, int toInclusive) {
        this(from, toInclusive, 1);
    }

    public IntRangeIterator(int from, int toInclusive, int step) {
        this.from = from;
        this.toInclusive = toInclusive;
        this.step = step;
    }

    @Override
    public boolean hasNext() {
        return from <= toInclusive;
    }

    @Override
    public Integer next() {
        int value = from;
        from += step;
        return value;
    }
}
