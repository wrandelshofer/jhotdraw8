/*
 * @(#)AbstractIterator.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

/*
 * @(#)AbstractIterator.java
 */
package org.jhotdraw8.collection;


import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * Abstract base classes for {@link ListIterator}s that also implement
 * the {@link Enumerator} interface.
 *
 * @param <E> the element type
 * @author Adrien Grzechowiak
 */
public abstract class AbstractIterator<E> implements ListIterator<E>, Enumerator<E>, Consumer<E> {


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