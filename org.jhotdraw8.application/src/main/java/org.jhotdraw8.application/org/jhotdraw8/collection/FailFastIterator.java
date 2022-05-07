/*
 * @(#)FailFastIterator.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.function.IntSupplier;

/**
 * An iterator that fails fast if a modification counter does not have an
 * expected value.
 *
 * @param <E> the element type
 */
public class FailFastIterator<E> implements Iterator<E> {
    private final @NonNull Iterator<? extends E> i;
    private int expectedModCount;
    private final @NonNull IntSupplier modCountSupplier;

    public FailFastIterator(@NonNull Iterator<? extends E> i, @NonNull IntSupplier modCountSupplier) {
        this.i = i;
        this.modCountSupplier = modCountSupplier;
        this.expectedModCount = modCountSupplier.getAsInt();
    }

    @Override
    public boolean hasNext() {
        if (expectedModCount != modCountSupplier.getAsInt()) {
            throw new ConcurrentModificationException();
        }
        return i.hasNext();
    }

    @Override
    public E next() {
        if (expectedModCount != modCountSupplier.getAsInt()) {
            throw new ConcurrentModificationException();
        }
        return i.next();
    }

    @Override
    public void remove() {
        if (expectedModCount != modCountSupplier.getAsInt()) {
            throw new ConcurrentModificationException();
        }
        i.remove();
        expectedModCount = modCountSupplier.getAsInt();
    }
}
