/*
 * @(#)EmptySpliterator.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.enumerator;

import org.jspecify.annotations.Nullable;

import java.util.Spliterator;

/**
 * An enumerator over an empty sequence.
 *
 * @param <E> the element type
 */
public class EmptyEnumerator<E> implements Enumerator<E> {
    private static final EmptyEnumerator<Object> singleton = new EmptyEnumerator<>();

    @SuppressWarnings("unchecked")
    public static <T> Enumerator<T> emptyEnumerator() {
        return (Enumerator<T>) singleton;
    }

    private EmptyEnumerator() {

    }

    @Override
    public boolean moveNext() {
        return false;
    }

    @Override
    public @Nullable E current() {
        return null;
    }

    @Override
    public @Nullable Spliterator<E> trySplit() {
        return null;
    }

    @Override
    public long estimateSize() {
        return 0;
    }

    @Override
    public int characteristics() {
        return 0;
    }
}
