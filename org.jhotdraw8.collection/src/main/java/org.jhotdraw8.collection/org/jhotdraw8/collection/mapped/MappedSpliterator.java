/*
 * @(#)MappedSpliterator.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.mapped;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

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
 * @author Werner Randelshofer
 */
public class MappedSpliterator<E, F> implements Spliterator<E> {
    private final @NonNull Spliterator<? extends F> s;
    private final @NonNull Function<F, E> mappingFunction;
    private final int characteristics;

    public MappedSpliterator(@NonNull Spliterator<? extends F> s, @NonNull Function<F, E> mappingFunction, int characteristics) {
        this.s = s;
        this.mappingFunction = mappingFunction;
        this.characteristics = characteristics;
    }

    public MappedSpliterator(@NonNull Spliterator<? extends F> s, @NonNull Function<F, E> mappingFunction) {
        this(s, mappingFunction, s.characteristics());
    }

    @Override
    public boolean tryAdvance(@NonNull Consumer<? super E> action) {
        return s.tryAdvance(k -> action.accept(mappingFunction.apply(k)));
    }

    @Override
    public @Nullable Spliterator<E> trySplit() {
        Spliterator<? extends F> split = s.trySplit();
        return split == null ? null : new MappedSpliterator<>(split, mappingFunction, characteristics);
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
