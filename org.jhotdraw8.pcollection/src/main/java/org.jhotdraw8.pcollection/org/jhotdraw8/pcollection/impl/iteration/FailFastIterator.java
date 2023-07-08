/*
 * @(#)FailFastIterator.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.pcollection.impl.iteration;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

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
    private final @NonNull Iterator<? extends E> i;
    private int expectedModCount;
    private final @NonNull IntSupplier modCountSupplier;
    private final @NonNull Consumer<E> removeFunction;
    private @Nullable E current;
    private boolean canRemove;

    public FailFastIterator(@NonNull Iterator<? extends E> i, @NonNull IntSupplier modCountSupplier) {
        this(i, (e) -> i.remove(), modCountSupplier);
    }

    public FailFastIterator(@NonNull Iterator<? extends E> i,
                            @NonNull Consumer<E> removeFunction, @NonNull IntSupplier modCountSupplier) {
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
