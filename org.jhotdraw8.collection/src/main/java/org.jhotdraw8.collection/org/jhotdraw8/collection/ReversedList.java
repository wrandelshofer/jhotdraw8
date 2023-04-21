/*
 * @(#)ReversedList.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.readonly.ReadOnlyList;

import java.util.AbstractList;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;

/**
 * A ReversedList provides an unmodifiable view on a List in reverse order.
 *
 * @param <E> the element type
 * @author Werner Randelshofer
 */
public class ReversedList<E> extends AbstractList<E> {
    private final @NonNull IntSupplier sizeFunction;
    private final @NonNull IntFunction<E> getFunction;

    public ReversedList(@NonNull ReadOnlyList<E> backingList) {
        this.sizeFunction = backingList::size;
        this.getFunction = backingList::get;
    }

    public ReversedList(@NonNull List<E> backingList) {
        this.sizeFunction = backingList::size;
        this.getFunction = backingList::get;
    }

    public ReversedList(@NonNull IntSupplier sizeFunction, @NonNull IntFunction<E> getFunction) {
        this.sizeFunction = sizeFunction;
        this.getFunction = getFunction;
    }

    @Override
    public E get(int index) {
        return getFunction.apply(sizeFunction.getAsInt() - 1 - index);
    }

    @Override
    public int size() {
        return sizeFunction.getAsInt();
    }
}
