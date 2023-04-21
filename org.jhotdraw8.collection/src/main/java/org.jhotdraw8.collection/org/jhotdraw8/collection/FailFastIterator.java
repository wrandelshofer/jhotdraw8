/*
 * @(#)FailFastIterator.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.function.IntSupplier;

/**
 * An iterator that fails when a provided modification counter does not have an
 * expected value.
 *
 * @param <E> the element type
 */
public class FailFastIterator<E> implements Iterator<E> {
    private final @NonNull Iterator<? extends E> i;
    private int expectedModCount;
    private final @NonNull IntSupplier modCountSupplier;
    private final @NonNull Runnable removeFunction;

    public FailFastIterator(@NonNull Iterator<? extends E> i, @NonNull IntSupplier modCountSupplier) {
        this(i, modCountSupplier, i::remove);
    }

    public FailFastIterator(@NonNull Iterator<? extends E> i,
                            @NonNull IntSupplier modCountSupplier,
                            @NonNull Runnable removeFunction) {
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
}
