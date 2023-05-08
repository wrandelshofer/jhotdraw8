/*
 * @(#)EmptyEnumeratorSpliterator.java
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
public class EmptySpliterator<E> implements EnumeratorSpliterator<E> {
    private static final EmptySpliterator<Object> singleton = new EmptySpliterator<>();

    @SuppressWarnings("unchecked")
    public static <T> @NonNull EnumeratorSpliterator<T> emptyEnumerator() {
        return (EnumeratorSpliterator<T>) singleton;
    }

    private EmptySpliterator() {

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
