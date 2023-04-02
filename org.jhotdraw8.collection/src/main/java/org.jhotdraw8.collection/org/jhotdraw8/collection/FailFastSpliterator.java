package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

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
public class FailFastSpliterator<E> implements Spliterator<E> {
    private final @NonNull Spliterator<? extends E> s;
    private final int expectedModCount;
    private final @NonNull IntSupplier modCountSupplier;

    public FailFastSpliterator(@NonNull Spliterator<? extends E> s, @NonNull IntSupplier modCountSupplier) {
        this.s = s;
        this.modCountSupplier = modCountSupplier;
        this.expectedModCount = modCountSupplier.getAsInt();
    }

    @Override
    public boolean tryAdvance(Consumer<? super E> action) {
        ensureUnmodified();
        return s.tryAdvance(action);
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
