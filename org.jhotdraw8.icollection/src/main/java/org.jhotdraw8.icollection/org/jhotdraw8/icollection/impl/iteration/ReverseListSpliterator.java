/*
 * @(#)ReversedListSpliterator.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.impl.iteration;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.function.Consumer;

public class ReverseListSpliterator<E> extends AbstractListIteratorSpliterator<E> {
    private int index;
    private final int fromInclusive;
    private final int toExclusive;
    private E current;
    private final List<E> list;

    public ReverseListSpliterator(List<E> list, int fromInclusive, int toExclusive) {
        this.index = fromInclusive;
        this.fromInclusive = fromInclusive;
        this.toExclusive = toExclusive;
        this.list = list;
    }


    @Override
    public boolean hasNext() {
        return index < toExclusive;
    }

    public boolean moveNext() {
        return tryAdvance(e -> current = e);
    }

    public E current() {
        return current;
    }

    @Override
    public E next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return current = list.get(list.size() - 1 - index++);
    }

    @Override
    public boolean hasPrevious() {
        return index >= fromInclusive;
    }

    @Override
    public E previous() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        return current = list.get(index--);
    }

    @Override
    public int nextIndex() {
        return index;
    }

    @Override
    public int previousIndex() {
        return index - 1;
    }

    @Override
    public boolean tryAdvance(Consumer<? super E> action) {
        if (hasNext()) {
            action.accept(next());
            return true;
        }
        return false;
    }

    @Override
    public @Nullable Spliterator<E> trySplit() {
        int lo = index, hi = toExclusive, mid = (lo + hi) >>> 1;
        return (lo >= mid)
                ? null
                : new ReverseListSpliterator<>(list, lo, index = mid);
    }

    @Override
    public long estimateSize() {
        return toExclusive - index;
    }
}
