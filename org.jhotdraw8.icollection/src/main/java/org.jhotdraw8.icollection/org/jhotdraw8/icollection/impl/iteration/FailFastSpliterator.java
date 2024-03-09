/*
 * @(#)FailFastSpliterator.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.impl.iteration;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.IntSupplier;

/**
 * A spliterator that fails when a provided modification counter does not have an
 * expected value.
 *
 * @param <E> the element type
 */
public class FailFastSpliterator<E> extends Spliterators.AbstractSpliterator<E> {
    private final @NonNull Spliterator<? extends E> s;
    private final int expectedModCount;
    private final @NonNull IntSupplier modCountSupplier;
    private final @Nullable Comparator<E> comparator;

    public FailFastSpliterator(@NonNull Spliterator<? extends E> s, @NonNull IntSupplier modCountSupplier, @Nullable Comparator<E> comparator) {
        super(s.estimateSize(), s.characteristics());
        this.s = s;
        this.modCountSupplier = modCountSupplier;
        this.expectedModCount = modCountSupplier.getAsInt();
        this.comparator = comparator;
    }

    @Override
    public boolean tryAdvance(@NonNull Consumer<? super E> action) {
        ensureUnmodified();
        return s.tryAdvance(action);
    }

    protected void ensureUnmodified() {
        if (expectedModCount != modCountSupplier.getAsInt()) {
            throw new ConcurrentModificationException();
        }
    }


    @Override
    public @Nullable Spliterator<E> trySplit() {
        Spliterator<? extends E> split = s.trySplit();
        return split == null ? null : new FailFastSpliterator<>(split, modCountSupplier, null);
    }

    @Override
    public long estimateSize() {
        return s.estimateSize();
    }

    @Override
    public int characteristics() {
        return s.characteristics() & ~Spliterator.IMMUTABLE;
    }

    @Override
    public Comparator<? super E> getComparator() {
        if (s.hasCharacteristics(Spliterator.SORTED)) {
            return comparator;
        }
        throw new IllegalStateException();
    }
}
