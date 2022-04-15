/*
 * @(#)AbstractIterator.java
 */
package org.jhotdraw8.collection;


import java.util.Iterator;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * AbstractIterator.
 *
 * @author Adrien Grzechowiak
 * {@link Enumerator}, {@link ListIterator},
 * {@link Iterator} for a {@link ReadOnlyList}.
 * <p>
 * Does not perform modification checks of the list.
 *
 * @param <E> the element type of the list
 */

public abstract class AbstractIterator<E>  implements Iterator<E>, ListIterator<E>, Enumerator<E>, Consumer<E> {


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