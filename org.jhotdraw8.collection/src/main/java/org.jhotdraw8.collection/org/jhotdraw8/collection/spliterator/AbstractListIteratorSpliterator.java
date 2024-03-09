/*
 * @(#)AbstractListSpliterator.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.spliterator;

import org.jhotdraw8.annotation.NonNull;

import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * Abstract base classes for {@link Spliterator}s that also implement
 * the {@link ListIterator} interface.
 *
 * @param <E> the element type
 * @author Adrien Grzechowiak
 */
public abstract class AbstractListIteratorSpliterator<E> implements ListIterator<E>, Spliterator<E> {
    /**
     * Constructs a new instance.
     */
    public AbstractListIteratorSpliterator() {
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void forEachRemaining(@NonNull Consumer<? super E> action) {
        ListIterator.super.forEachRemaining(action);
    }

    @Override
    public void set(E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int characteristics() {
        return Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.ORDERED;
    }

}