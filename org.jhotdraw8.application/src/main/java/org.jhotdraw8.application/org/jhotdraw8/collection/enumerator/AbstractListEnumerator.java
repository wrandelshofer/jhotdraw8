/*
 * @(#)AbstractListEnumerator.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.enumerator;


import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * Abstract base classes for {@link Enumerator}s that also implement
 * the {@link ListIterator} interface.
 *
 * @param <E> the element type
 * @author Adrien Grzechowiak
 */
public abstract class AbstractListEnumerator<E> implements ListIterator<E>, Enumerator<E> {
    public AbstractListEnumerator() {
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void forEachRemaining(Consumer<? super E> action) {
        Enumerator.super.forEachRemaining(action);
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