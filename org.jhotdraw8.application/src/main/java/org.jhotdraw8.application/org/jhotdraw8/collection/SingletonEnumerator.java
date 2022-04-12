/*
 * @(#)SingletonEnumerator.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import java.util.Spliterators;

public class SingletonEnumerator<T> extends Spliterators.AbstractSpliterator<T> implements Enumerator<T> {
    private final T current;
    private boolean canMove = true;

    public SingletonEnumerator(T singleton) {
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
    public T current() {
        return current;
    }

    @Override
    public long estimateSize() {
        return canMove ? 1L : 0L;
    }
}
