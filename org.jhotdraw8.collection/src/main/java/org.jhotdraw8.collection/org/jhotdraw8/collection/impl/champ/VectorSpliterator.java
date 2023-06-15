/*
 * @(#)VectorSpliterator.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.impl.champ;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.VectorList;
import org.jhotdraw8.collection.enumerator.AbstractEnumeratorSpliterator;
import org.jhotdraw8.collection.impl.vector.BitMappedTrie;

import java.util.function.Function;

/**
 * A spliterator for a {@code VectorMap} or {@code VectorSet}.
 *
 * @param <K> the key type
 */
public class VectorSpliterator<K> extends AbstractEnumeratorSpliterator<K> {
    private final BitMappedTrie.@NonNull BitMappedTrieSpliterator<Object> vector;
    private final @NonNull Function<Object, K> mapper;

    public VectorSpliterator(@NonNull VectorList<Object> vector, @NonNull Function<Object, K> mapper, int fromIndex, long est, int additionalCharacteristics) {
        super(est, additionalCharacteristics);
        this.vector = new BitMappedTrie.BitMappedTrieSpliterator<>(vector, fromIndex, 0);
        this.mapper = mapper;
    }

    @Override
    public boolean moveNext() {
        boolean success = vector.moveNext();
        if (!success) return false;
        if (vector.current() instanceof VectorTombstone t) {
            vector.skip(t.after());
            vector.moveNext();
        }
        current = mapper.apply(vector.current());
        return true;
    }
}
