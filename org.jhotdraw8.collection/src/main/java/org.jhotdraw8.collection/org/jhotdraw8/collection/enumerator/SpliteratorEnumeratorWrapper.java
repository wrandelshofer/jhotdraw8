/*
 * @(#)EnumeratorFacade.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection.enumerator;

import org.jhotdraw8.annotation.Nullable;

import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * Wraps a {@link Spliterator} into the Enumerator interface.
 *
 * @param <E> the element type
 */
public class SpliteratorEnumeratorWrapper<E> implements Enumerator<E>, Consumer<E> {
    private final Spliterator<E> spliterator;
    private E current;

    public SpliteratorEnumeratorWrapper(Spliterator<E> spliterator) {
        this.spliterator = spliterator;
    }

    @Override
    public void accept(E e) {
        current = e;
    }

    @Override
    public boolean moveNext() {
        return spliterator.tryAdvance(this);
    }

    @Override
    public E current() {
        return current;
    }

    @Override
    public @Nullable Spliterator<E> trySplit() {
        final Spliterator<E> splitted = spliterator.trySplit();
        return splitted == null ? null : new SpliteratorEnumeratorWrapper<>(splitted);
    }

    @Override
    public long estimateSize() {
        return spliterator.estimateSize();
    }

    @Override
    public int characteristics() {
        return spliterator.characteristics();
    }
}
