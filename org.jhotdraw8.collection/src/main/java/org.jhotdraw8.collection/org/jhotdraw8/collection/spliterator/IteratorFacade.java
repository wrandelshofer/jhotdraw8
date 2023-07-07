/*
 * @(#)IteratorFacade.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.spliterator;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * Wraps an {@link Spliterator} into an {@link Iterator} interface.
 *
 * @param <E> the element type
 */
public class IteratorFacade<E> implements Iterator<E>, Consumer<E> {
    private final @NonNull Spliterator<E> e;
    private final @Nullable Consumer<E> removeFunction;
    private boolean valueReady;
    private boolean canRemove;
    private E current;

    public IteratorFacade(@NonNull Spliterator<E> e, @Nullable Consumer<E> removeFunction) {
        this.e = e;
        this.removeFunction = removeFunction;
    }

    @Override
    public boolean hasNext() {
        if (!valueReady) {
            // e.moveNext() changes e.current().
            // But the contract of hasNext() does not allow, that we change
            // the current value of the iterator.
            // This is why, we need a 'current' field in this facade.
            valueReady = e.tryAdvance(this);
        }
        return valueReady;
    }

    @Override
    public E next() {
        if (!valueReady && !hasNext()) {
            throw new NoSuchElementException();
        } else {
            valueReady = false;
            canRemove = true;
            return current;
        }
    }

    @Override
    public void remove() {
        if (!canRemove) throw new IllegalStateException();
        if (removeFunction != null) {
            removeFunction.accept(current);
            canRemove = false;
        } else {
            Iterator.super.remove();
        }
    }

    @Override
    public void accept(E e) {
        current = e;
    }
}
