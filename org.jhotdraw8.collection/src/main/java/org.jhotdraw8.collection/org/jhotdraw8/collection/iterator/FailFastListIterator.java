/*
 * @(#)FailFastListIterator.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.iterator;


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
    private final ListIterator<E> i;
    private int expectedModCount;
    private final IntSupplier modCountSupplier;
    private final Runnable removeFunction;

    public FailFastListIterator(ListIterator<E> i, IntSupplier modCountSupplier) {
        this(i, modCountSupplier, i::remove);
    }

    public FailFastListIterator(ListIterator<E> i,
                                IntSupplier modCountSupplier,
                                Runnable removeFunction) {
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
        expectedModCount = modCountSupplier.getAsInt();
    }

    @Override
    public void set(E e) {
        ensureUnmodified();
        i.set(e);
        expectedModCount = modCountSupplier.getAsInt();
    }

    @Override
    public void add(E e) {
        ensureUnmodified();
        i.add(e);
        expectedModCount = modCountSupplier.getAsInt();
    }
}
