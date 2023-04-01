package org.jhotdraw8.collection.mapped;

import org.jhotdraw8.annotation.NonNull;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;

public class MappedSpliterator<E, F> implements Spliterator<E> {
    private final @NonNull Spliterator<? extends F> s;
    private final @NonNull Function<F, E> mappingFunction;
    private final int characteristics;

    public MappedSpliterator(@NonNull Spliterator<? extends F> s, @NonNull Function<F, E> mappingFunction, int characteristics) {
        this.s = s;
        this.mappingFunction = mappingFunction;
        this.characteristics = characteristics;
    }

    @Override
    public boolean tryAdvance(Consumer<? super E> action) {
        return s.tryAdvance(k -> action.accept(mappingFunction.apply(k)));
    }

    @Override
    public Spliterator<E> trySplit() {
        Spliterator<? extends F> split = s.trySplit();
        return split == null ? null : new MappedSpliterator<E, F>(split, mappingFunction, characteristics);
    }

    @Override
    public long estimateSize() {
        return s.estimateSize();
    }

    @Override
    public int characteristics() {
        return characteristics;
    }
}
