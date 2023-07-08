/*
 * @(#)ReversedListSpliterator.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.pcollection.impl.iteration;

import org.jhotdraw8.annotation.NonNull;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.function.Consumer;

public class ReverseListSpliterator<E> extends AbstractListIteratorSpliterator<E> {
    private int index;
    private final int fromInclusive;
    private final int toExclusive;
    private E current;
    private final @NonNull List<E> list;

    public ReverseListSpliterator(@NonNull List<E> list, int fromInclusive, int toExclusive) {
        this.index = toExclusive - 1;
        this.fromInclusive = fromInclusive;
        this.toExclusive = toExclusive;
        this.list = list;
    }


    @Override
    public boolean hasNext() {
        return index >= fromInclusive;
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
        return current = list.get(index--);
    }

    @Override
    public boolean hasPrevious() {
        return index < toExclusive;
    }

    @Override
    public E previous() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        return current = list.get(index++);
    }

    @Override
    public int nextIndex() {
        return index;
    }

    @Override
    public int previousIndex() {
        return index + 1;
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
    public Spliterator<E> trySplit() {
        int hi = index, mid = ((hi - fromInclusive) >>> 1) + fromInclusive;
        return (hi <= mid)
                ? null
                : new ReverseListSpliterator<>(list, index = mid, toExclusive);
    }

    @Override
    public long estimateSize() {
        return index - fromInclusive + 1;
    }
}
