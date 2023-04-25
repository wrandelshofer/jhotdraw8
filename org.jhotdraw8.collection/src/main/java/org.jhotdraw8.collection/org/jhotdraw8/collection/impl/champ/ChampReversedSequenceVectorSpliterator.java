/*
 * @(#)VectorSpliterator.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.impl.champ;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.VectorList;
import org.jhotdraw8.collection.enumerator.AbstractEnumeratorSpliterator;

import java.util.function.Function;

public class ChampReversedSequenceVectorSpliterator<K> extends AbstractEnumeratorSpliterator<K> {
    private final @NonNull VectorList<Object> vector;
    private final @NonNull Function<Object, K> mapper;
    private int index;

    public ChampReversedSequenceVectorSpliterator(@NonNull VectorList<Object> vector, @NonNull Function<Object, K> mapper, int additionalCharacteristics, long est) {
        super(est, additionalCharacteristics);
        this.vector = vector;
        this.mapper = mapper;
        index = vector.size() - 1;
    }

    @Override
    public boolean moveNext() {
        if (index < 0) {
            return false;
        }
        Object o = vector.get(index--);
        if (o instanceof ChampTombstone t) {
            index -= t.before();
            o = vector.get(index--);
        }
        current = mapper.apply(o);
        return true;
    }
}
