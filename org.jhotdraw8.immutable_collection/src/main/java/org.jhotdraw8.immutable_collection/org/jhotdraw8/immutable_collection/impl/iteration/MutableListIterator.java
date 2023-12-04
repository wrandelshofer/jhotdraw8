package org.jhotdraw8.immutable_collection.impl.iteration;

import org.jhotdraw8.annotation.NonNull;

import java.util.*;
import java.util.function.IntSupplier;

public class MutableListIterator<E> implements ListIterator<E> {
    private final @NonNull List<E> src;
    /**
     * Index of next element to return.
     */
    int index;
    /**
     * Index of last element returned; negative if none.
     */
    int lastReturned = -1;
    private final @NonNull IntSupplier modCount;
    private int expectedCount;

    /**
     * Constructs a new instance.
     *
     * @param src   the underlying source list
     * @param index the next index of the iterator
     */
    public MutableListIterator(@NonNull List<E> src, int index, @NonNull IntSupplier modCount) {
        Objects.checkIndex(index, src.size() + 1);
        this.src = src;
        this.index = index;
        this.modCount = modCount;
        expectedCount = modCount.getAsInt();
    }

    @Override
    public boolean hasNext() {
        ensureUnmodified();
        return index < src.size();
    }

    @Override
    public E next() {
        ensureUnmodified();
        if (!hasNext()) throw new NoSuchElementException();
        return get(index++);
    }

    @Override
    public boolean hasPrevious() {
        ensureUnmodified();
        return index > 0;
    }

    @Override
    public E previous() {
        ensureUnmodified();
        if (!hasPrevious()) throw new NoSuchElementException();
        return get(--index);
    }

    @Override
    public int nextIndex() {
        ensureUnmodified();
        return index;
    }

    @Override
    public int previousIndex() {
        ensureUnmodified();
        return index - 1;
    }

    @Override
    public void remove() {
        ensureUnmodified();
        if (lastReturned < 0) {
            throw new IllegalStateException();
        }
        try {
            src.remove(lastReturned);
            lastReturned = -1;
            if (index > 0) index--;
            updateModified();
        } catch (IndexOutOfBoundsException ex) {
            throw new ConcurrentModificationException();
        }
    }

    private E get(int index) {
        ensureUnmodified();
        lastReturned = index;
        return src.get(index);
    }

    public void set(E e) {
        ensureUnmodified();
        if (lastReturned < 0) {
            throw new IllegalStateException();
        }
        try {
            src.set(lastReturned, e);
            updateModified();
        } catch (IndexOutOfBoundsException ex) {
            throw new ConcurrentModificationException();
        }
    }

    @Override
    public void add(E e) {
        ensureUnmodified();
        try {
            int i = index;
            src.add(index, e);
            index = i + 1;
            lastReturned = -1;
            updateModified();
        } catch (IndexOutOfBoundsException ex) {
            throw new ConcurrentModificationException();
        }
    }

    protected void ensureUnmodified() {
        if (expectedCount != modCount.getAsInt()) {
            throw new ConcurrentModificationException();
        }

    }

    protected void updateModified() {
        expectedCount = modCount.getAsInt();
    }
}
