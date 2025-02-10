/*
 * @(#)MappedSpliterator.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.impl.iteration;

import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Maps a {@link Spliterator} to a different element type.
 * <p>
 * The underlying iterator is referenced - not copied.
 *
 * @param <E> the mapped element type
 * @param <F> the original element type
 */
public class MappedSpliterator<E, F> implements Spliterator<E> {
    private final Spliterator<? extends F> s;
    private final Function<F, E> mappingFunction;
    private final int characteristics;
    private final @Nullable Comparator<? super E> comparator;

    public MappedSpliterator(Spliterator<? extends F> s, Function<F, E> mappingFunction, int characteristics, @Nullable Comparator<? super E> comparator) {
        this.s = s;
        this.mappingFunction = mappingFunction;
        this.characteristics = characteristics;
        this.comparator = comparator;
    }


    @Override
    public boolean tryAdvance(Consumer<? super E> action) {
        return s.tryAdvance(k -> action.accept(mappingFunction.apply(k)));
    }

    @Override
    public @Nullable Spliterator<E> trySplit() {
        Spliterator<? extends F> split = s.trySplit();
        return split == null ? null : new MappedSpliterator<>(split, mappingFunction, characteristics, comparator);
    }

    @Override
    public long estimateSize() {
        return s.estimateSize();
    }

    @Override
    public int characteristics() {
        return characteristics;
    }

    @Override
    public @Nullable Comparator<? super E> getComparator() {
        return comparator;
    }
}
