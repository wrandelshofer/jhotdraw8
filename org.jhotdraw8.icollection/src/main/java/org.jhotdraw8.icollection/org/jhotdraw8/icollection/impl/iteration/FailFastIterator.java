/*
 * @(#)FailFastIterator.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.impl.iteration;

import org.jspecify.annotations.Nullable;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.IntSupplier;

/**
 * An iterator that fails when a provided modification counter does not have an
 * expected value.
 *
 * @param <E> the element type
 */
public class FailFastIterator<E> implements Iterator<E> {
    private final Iterator<? extends E> i;
    private int expectedModCount;
    private final IntSupplier modCountSupplier;
    private final Consumer<E> removeFunction;
    private @Nullable E current;
    private boolean canRemove;

    public FailFastIterator(Iterator<? extends E> i, IntSupplier modCountSupplier) {
        this(i, (e) -> i.remove(), modCountSupplier);
    }

    public FailFastIterator(Iterator<? extends E> i,
                            Consumer<E> removeFunction, IntSupplier modCountSupplier) {
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
        current = i.next();
        canRemove = true;
        return current;
    }

    protected void ensureUnmodified() {
        if (expectedModCount != modCountSupplier.getAsInt()) {
            throw new ConcurrentModificationException();
        }
    }

    @Override
    public void remove() {
        if (canRemove) {
            ensureUnmodified();
            removeFunction.accept(current);
            expectedModCount = modCountSupplier.getAsInt();
            canRemove = false;
        } else {
            throw new IllegalStateException();
        }
    }
}
