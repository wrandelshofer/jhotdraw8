package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.Nullable;

import java.util.Iterator;
import java.util.Objects;
import java.util.stream.IntStream;

public class IntRangeReadOnlySet extends AbstractReadOnlySet<Integer> {
    private final int from;
    private final int to;

    /**
     * Creates a new instance.
     *
     * @param from from inclusive
     * @param to   exclusive
     */
    public IntRangeReadOnlySet(int from, int to) {
        Objects.checkIndex(from, to);
        this.from = from;
        this.to = to;
    }

    @Override
    public Iterator<Integer> iterator() {
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
