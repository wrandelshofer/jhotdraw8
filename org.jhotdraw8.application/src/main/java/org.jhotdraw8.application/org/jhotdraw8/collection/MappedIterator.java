/*
 * @(#)MappedIterator.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

import java.util.Iterator;
import java.util.function.Function;

public class MappedIterator<E, F> implements Iterator<E> {
    private final @NonNull Iterator<F> i;

    private final @NonNull Function<F, E> mappingFunction;

    public MappedIterator(@NonNull Iterator<F> i, @NonNull Function<F, E> mappingFunction) {
        this.i = i;
        this.mappingFunction = mappingFunction;
    }

    @Override
    public boolean hasNext() {
        return i.hasNext();
    }

    @Override
    public E next() {
        return mappingFunction.apply(i.next());
    }

    public void remove() {
        i.remove();
    }
}
