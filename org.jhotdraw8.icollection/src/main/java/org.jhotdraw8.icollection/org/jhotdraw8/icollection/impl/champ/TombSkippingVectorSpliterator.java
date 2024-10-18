/*
 * @(#)VectorSpliterator.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.impl.champ;

import org.jhotdraw8.icollection.impl.vector.BitMappedTrie;
import org.jspecify.annotations.Nullable;

import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A spliterator for a {@code SimplePersistentSequencedMap} or {@code SimplePersistentSequencedSet} that skips
 * tombstones.
 *
 * @param <K> the key type
 */
public class TombSkippingVectorSpliterator<K> extends Spliterators.AbstractSpliterator<K> implements Consumer<Object> {
    private final BitMappedTrie.BitMappedTrieSpliterator<Object> vector;
    private final Function<Object, K> mapper;
    private @Nullable Object current;

    public TombSkippingVectorSpliterator(BitMappedTrie<Object> vector, Function<Object, K> mapper, int fromIndex,
                                         int size, int sizeWithTombstones,
                                         int additionalCharacteristics) {
        super(size, additionalCharacteristics);
        this.vector = new BitMappedTrie.BitMappedTrieSpliterator<>(vector, fromIndex, sizeWithTombstones, 0);
        this.mapper = mapper;
    }

    @Override
    public boolean tryAdvance(Consumer<? super K> action) {
        boolean success = vector.tryAdvance(this);
        if (!success) {
            return false;
        }
        if (current instanceof Tombstone t) {
            vector.skip(t.skip());
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
