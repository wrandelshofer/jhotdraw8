/*
 * @(#)VectorSpliterator.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.champ;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.enumerator.AbstractEnumeratorSpliterator;
import org.jhotdraw8.collection.vector.VectorList;

import java.util.function.Function;

class VectorSpliterator<K> extends AbstractEnumeratorSpliterator<K> {
    private final @NonNull VectorList<Object> vector;
    private final @NonNull Function<Object, K> mapper;
    private int index;

    protected VectorSpliterator(@NonNull VectorList<Object> vector, @NonNull Function<Object, K> mapper, long est, int additionalCharacteristics) {
        super(est, additionalCharacteristics);
        this.vector = vector;
        this.mapper = mapper;
    }

    @Override
    public boolean moveNext() {
        if (index >= vector.size()) {
            return false;
        }
        Object o = vector.get(index++);
        if (o instanceof Tombstone t) {
            index += t.after();
            o = vector.get(index++);
        }
        current = mapper.apply(o);
        return true;
    }
}
