/*
 * @(#)VectorSpliterator.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.pcollection.impl.champ;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.pcollection.VectorList;
import org.jhotdraw8.pcollection.impl.vector.BitMappedTrie;

import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A spliterator for a {@code VectorMap} or {@code VectorSet} that skips
 * tombstones.
 *
 * @param <K> the key type
 */
public class TombSkippingVectorSpliterator<K> extends Spliterators.AbstractSpliterator<K> implements Consumer<Object> {
    private final BitMappedTrie.@NonNull BitMappedTrieSpliterator<Object> vector;
    private final @NonNull Function<Object, K> mapper;
    private @Nullable Object current;

    public TombSkippingVectorSpliterator(@NonNull VectorList<Object> vector, @NonNull Function<Object, K> mapper, int fromIndex, long est, int additionalCharacteristics) {
        super(est, additionalCharacteristics);
        this.vector = new BitMappedTrie.BitMappedTrieSpliterator<>(vector, fromIndex, vector.size(), 0);
        this.mapper = mapper;
    }

    @Override
    public boolean tryAdvance(Consumer<? super K> action) {
        boolean success = vector.tryAdvance(this);
        if (!success) return false;
        if (current instanceof VectorTombstone t) {
            vector.skip(t.after());
            vector.tryAdvance(this);
        }
        action.accept(mapper.apply(current));
        return true;
    }

    @Override
    public void accept(Object o) {
        current = o;
    }
}