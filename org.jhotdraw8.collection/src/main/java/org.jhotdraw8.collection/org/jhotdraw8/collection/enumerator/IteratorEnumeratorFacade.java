/*
 * @(#)SpliteratorFacade.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection.enumerator;

import org.jspecify.annotations.Nullable;

import java.util.Iterator;
import java.util.Spliterator;

/**
 * Provides a {@link Enumerator} facade for an {@link Iterator}.
 *
 * @param <E> the element type
 * @author Werner Randelshofer
 */
public class IteratorEnumeratorFacade<E> implements Enumerator<E> {
    private final Iterator<? extends E> iterator;

    private E current;

    public IteratorEnumeratorFacade(final Iterator<? extends E> iterator) {
        this.iterator = iterator;
    }


    @Override
    public boolean moveNext() {
        if (iterator.hasNext()) {
            current = iterator.next();
            return true;
        }
        return false;
    }

    @Override
    public E current() {
        return current;
    }

    @Override
    public @Nullable Spliterator<E> trySplit() {
        return null;
    }

    @Override
    public long estimateSize() {
        return Long.MAX_VALUE;
    }

    @Override
    public int characteristics() {
        return 0;
    }
}
