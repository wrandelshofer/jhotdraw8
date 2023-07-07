/*
 * @(#)EmptySpliterator.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.enumerator;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.util.Spliterator;

/**
 * An enumerator over an empty sequence.
 *
 * @param <E> the element type
 */
public class EmptyEnumeratorSpliterator<E> implements EnumeratorSpliterator<E> {
    private static final EmptyEnumeratorSpliterator<Object> singleton = new EmptyEnumeratorSpliterator<>();

    @SuppressWarnings("unchecked")
    public static <T> @NonNull EnumeratorSpliterator<T> emptyEnumerator() {
        return (EnumeratorSpliterator<T>) singleton;
    }

    private EmptyEnumeratorSpliterator() {

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
