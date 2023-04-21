/*
 * @(#)ReadOnlyListEnumeratorSpliterator.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection.enumerator;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.readonly.ReadOnlyList;

import java.util.ListIterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * A {@link ListIterator}, {@link EnumeratorSpliterator}, and {@link Spliterator} for a
 * {@link ReadOnlyList}.
 * <p>
 * Does not perform modification checks.
 *
 * @param <E> the element type
 */
public class ReadOnlyListEnumeratorSpliterator<E> extends AbstractListEnumeratorSpliterator<E> {
    private final @NonNull ReadOnlyList<E> list;
    private int index;
    private final int size;
    private E current;

    public ReadOnlyListEnumeratorSpliterator(@NonNull ReadOnlyList<E> list) {
        this(list, 0, list.size());
    }

    public ReadOnlyListEnumeratorSpliterator(@NonNull ReadOnlyList<E> list, int index, int size) {
        this.list = list;
        this.size = size;
        this.index = index;
    }

    @Override
    public boolean hasNext() {
        return index < getSize();
    }

    private int getSize() {
        return size;
    }

    @Override
    public E next() {
        return current = list.get(index++);
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public E previous() {
        return current = list.get(--index);
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
    public boolean tryAdvance(@Nullable Consumer<? super E> action) {
        Objects.requireNonNull(action, "action");
        if (index >= 0 && index < getSize()) {
            action.accept(current = list.get(index++));
            return true;
        }
        return false;
    }

    @Override
    public @Nullable Spliterator<E> trySplit() {
        int lo = index, mid = (lo + getSize()) >>> 1;
        return (lo >= mid)
                ? null
                : new ReadOnlyListEnumeratorSpliterator<>(list, lo, index = mid);
    }

    @Override
    public long estimateSize() {
        return getSize() - index;
    }


    @Override
    public boolean moveNext() {
        return tryAdvance(e -> current = e);
    }

    @Override
    public E current() {
        return current;
    }
}