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
 * Wraps a list in the {@link List} API.
 * <p>
 * The underlying list is referenced - not copied.
 *
 * @author Werner Randelshofer
 */
public class ListWrapper<E> extends AbstractList<E> {
    private final @NonNull IntSupplier sizeFunction;
    private final @NonNull IntFunction<E> getFunction;

    public ListWrapper(@NonNull ReadOnlyList<E> backingList) {
        this.sizeFunction = backingList::size;
        this.getFunction = backingList::get;
    }

    public ListWrapper(@NonNull IntSupplier sizeFunction, @NonNull IntFunction<E> getFunction) {
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
