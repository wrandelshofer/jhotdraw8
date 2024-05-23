/*
 * @(#)VectorSpliterator.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.impl.champ;

import org.jhotdraw8.icollection.VectorList;

import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A spliterator for a {@code SimpleImmutableSequencedMap} or {@code SimpleImmutableSequencedSet} that skips
 * tombstones.
 *
 * @param <E> the element type
 */
public class ReverseTombSkippingVectorSpliterator<E> extends Spliterators.AbstractSpliterator<E> {
    private final VectorList<Object> vector;
    private final Function<Object, E> mapper;
    private int index;

    public ReverseTombSkippingVectorSpliterator(VectorList<Object> vector, Function<Object, E> mapper, long est, int additionalCharacteristics) {
        super(est, additionalCharacteristics);
        this.vector = vector;
        this.mapper = mapper;
        index = vector.size() - 1;
    }

    @Override
    public boolean tryAdvance(Consumer<? super E> action) {
        if (index < 0) {
            return false;
        }
        Object o = vector.get(index--);
        if (o instanceof Tombstone t) {
            index -= t.skip();
            o = vector.get(index--);
        }
        action.accept(mapper.apply(o));
        return true;
    }

}
