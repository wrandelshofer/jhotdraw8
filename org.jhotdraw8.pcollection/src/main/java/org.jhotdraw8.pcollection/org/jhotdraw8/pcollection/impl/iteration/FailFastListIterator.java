/*
 * @(#)FailFastListIterator.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.pcollection.impl.iteration;

import org.jhotdraw8.annotation.NonNull;

import java.util.ConcurrentModificationException;
import java.util.ListIterator;
import java.util.function.IntSupplier;

/**
 * An iterator that fails when a provided modification counter does not have an
 * expected value.
 *
 * @param <E> the element type
 */
public class FailFastListIterator<E> implements ListIterator<E> {
    private final @NonNull ListIterator<E> i;
    private int expectedModCount;
    private final @NonNull IntSupplier modCountSupplier;
    private final @NonNull Runnable removeFunction;

    public FailFastListIterator(@NonNull ListIterator<E> i, @NonNull IntSupplier modCountSupplier) {
        this(i, i::remove, modCountSupplier);
    }

    public FailFastListIterator(@NonNull ListIterator<E> i,
                                @NonNull Runnable removeFunction, @NonNull IntSupplier modCountSupplier) {
        this.i = i;
        this.modCountSupplier = modCountSupplier;
        this.expectedModCount = modCountSupplier.getAsInt();
        this.removeFunction = removeFunction;
    }

    @Override
    public boolean hasNext() {
        ensureUnmodified();
        return i.hasNext();
    }

    @Override
    public E next() {
        ensureUnmodified();
        return i.next();
    }

    @Override
    public boolean hasPrevious() {
        ensureUnmodified();
        return i.hasPrevious();
    }

    @Override
    public E previous() {
        ensureUnmodified();
        return i.previous();
    }

    @Override
    public int nextIndex() {
        ensureUnmodified();
        return i.nextIndex();
    }

    @Override
    public int previousIndex() {
        ensureUnmodified();
        return i.previousIndex();
    }

    protected void ensureUnmodified() {
        if (expectedModCount != modCountSupplier.getAsInt()) {
            throw new ConcurrentModificationException();
        }
    }

    @Override
    public void remove() {
        ensureUnmodified();
        removeFunction.run();
        updateModified();
    }

    @Override
    public void set(E e) {
        ensureUnmodified();
        i.set(e);
        updateModified();
    }

    @Override
    public void add(E e) {
        ensureUnmodified();
        i.add(e);
        updateModified();
    }

    private void updateModified() {
        expectedModCount = modCountSupplier.getAsInt();
    }
}
