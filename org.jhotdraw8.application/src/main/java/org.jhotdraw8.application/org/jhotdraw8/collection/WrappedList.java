/*
 * @(#)ListWrapper.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

import java.util.AbstractList;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;

/**
 * Wraps list functions in the {@link List} API.
 *
 * @param <E> the element type
 * @author Werner Randelshofer
 */
public class WrappedList<E> extends AbstractList<E> {
    private final @NonNull IntSupplier sizeFunction;
    private final @NonNull IntFunction<E> getFunction;

    public WrappedList(@NonNull ReadOnlyList<E> backingList) {
        this.sizeFunction = backingList::size;
        this.getFunction = backingList::get;
    }

    public WrappedList(@NonNull List<E> backingList) {
        this.sizeFunction = backingList::size;
        this.getFunction = backingList::get;
    }

    public WrappedList(@NonNull IntSupplier sizeFunction, @NonNull IntFunction<E> getFunction) {
        this.sizeFunction = sizeFunction;
        this.getFunction = getFunction;
    }

    @Override
    public E get(int index) {
        return getFunction.apply(index);
    }

    @Override
    public int size() {
        return sizeFunction.getAsInt();
    }
}
