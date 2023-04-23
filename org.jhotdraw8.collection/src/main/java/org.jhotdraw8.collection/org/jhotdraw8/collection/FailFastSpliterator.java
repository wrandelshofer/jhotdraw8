/*
 * @(#)FailFastSpliterator.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.enumerator.AbstractEnumeratorSpliterator;

import java.util.ConcurrentModificationException;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.IntSupplier;

/**
 * A spliterator that fails when a provided modification counter does not have an
 * expected value.
 *
 * @param <E> the element type
 */
public class FailFastSpliterator<E> extends AbstractEnumeratorSpliterator<E> implements Consumer<E> {
    private final @NonNull Spliterator<? extends E> s;
    private final int expectedModCount;
    private final @NonNull IntSupplier modCountSupplier;

    public FailFastSpliterator(@NonNull Spliterator<? extends E> s, @NonNull IntSupplier modCountSupplier) {
        super(s.estimateSize(), s.characteristics());
        this.s = s;
        this.modCountSupplier = modCountSupplier;
        this.expectedModCount = modCountSupplier.getAsInt();
    }

    @Override
    public void accept(E e) {
        current = e;
    }

    @Override
    public boolean moveNext() {
        ensureUnmodified();
        return s.tryAdvance(this);
    }

    protected void ensureUnmodified() {
        if (expectedModCount != modCountSupplier.getAsInt()) {
            throw new ConcurrentModificationException();
        }
    }

    @Override
    public Spliterator<E> trySplit() {
        Spliterator<? extends E> split = s.trySplit();
        return split == null ? null : new FailFastSpliterator<>(split, modCountSupplier);
    }

    @Override
    public long estimateSize() {
        return s.estimateSize();
    }

    @Override
    public int characteristics() {
        return s.characteristics() & ~Spliterator.IMMUTABLE;
    }
}
