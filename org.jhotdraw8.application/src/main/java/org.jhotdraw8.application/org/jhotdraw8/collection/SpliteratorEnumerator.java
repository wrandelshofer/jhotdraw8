/*
 * @(#)SpliteratorEnumerator.java
 * Copyright © 2021 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;

import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * Wraps a Spliterator into the Enumerator interface.
 *
 * @param <E> the eleemnt type
 */
public class SpliteratorEnumerator<E> implements Enumerator<E>, Consumer<E> {
    private final Spliterator<E> spliterator;
    private E current;

    public SpliteratorEnumerator(Spliterator<E> spliterator) {
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
    public Spliterator<E> trySplit() {
        final Spliterator<E> splitted = spliterator.trySplit();
        return splitted == null ? null : new SpliteratorEnumerator<>(splitted);
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
