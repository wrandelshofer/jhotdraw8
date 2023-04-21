/*
 * @(#)ArrayListEnumeratorSpliterator.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection.enumerator;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * A {@link ListIterator} over an unmodifiable object array.
 * <p>
 * Does not perform modification checks.
 *
 * @param <E> the element type
 * @author Adrien Grzechowiak
 */
public class ArrayListEnumeratorSpliterator<E> extends AbstractListEnumeratorSpliterator<E> {
    private final Object[] list;
    private int index;
    final int size;

    private E current;

    public ArrayListEnumeratorSpliterator(@NonNull Object @NonNull [] list) {
        this(list, 0, list.length);
    }

    public ArrayListEnumeratorSpliterator(@NonNull Object[] list, int index, int size) {
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

    @SuppressWarnings("unchecked")
    @Override
    public @NonNull E next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return current = (E) list[index++];
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NonNull E previous() {
        return current = (E) list[--index];
    }

    @Override
    public int nextIndex() {
        return index;
    }

    @Override
    public int previousIndex() {
        return index - 1;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean tryAdvance(@Nullable Consumer<? super E> action) {
        Objects.requireNonNull(action, "action");
        if (index >= 0 && index < getSize()) {
            action.accept(current = (E) list[index++]);
            return true;
        }
        return false;
    }

    @Override
    public @Nullable Spliterator<E> trySplit() {
        int lo = index, mid = (lo + getSize()) >>> 1;
        return (lo >= mid)
                ? null
                : new ArrayListEnumeratorSpliterator<>(list, lo, index = mid);
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