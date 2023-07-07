/*
 * @(#)SingletonSpliterator.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.enumerator;

import java.util.Spliterators;

/**
 * An enumerator over a single element.
 *
 * @param <E> the element type
 */
public class SingletonSpliterator<E> extends Spliterators.AbstractSpliterator<E> implements EnumeratorSpliterator<E> {
    private final E current;
    private boolean canMove = true;

    public SingletonSpliterator(E singleton) {
        super(1L, 0);
        current = singleton;
    }

    @Override
    public boolean moveNext() {
        boolean hasMoved = canMove;
        canMove = false;
        return hasMoved;
    }

    @Override
    public E current() {
        return current;
    }

    @Override
    public long estimateSize() {
        return canMove ? 1L : 0L;
    }
}
