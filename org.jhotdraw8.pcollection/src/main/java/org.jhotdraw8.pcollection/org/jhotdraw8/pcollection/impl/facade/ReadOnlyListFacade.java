/*
 * @(#)ReadOnlyListFacade.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.pcollection.impl.facade;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.pcollection.readonly.AbstractReadOnlyList;
import org.jhotdraw8.pcollection.readonly.ReadOnlyList;
import org.jhotdraw8.pcollection.readonly.ReadOnlySequencedCollection;

import java.util.List;
import java.util.Objects;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * Wraps {@code List} functions in the {@link ReadOnlyList} interface.
 *
 * @param <E> the element type
 * @author Werner Randelshofer
 */
public class ReadOnlyListFacade<E> extends AbstractReadOnlyList<E> {
    private final @NonNull IntSupplier sizeFunction;
    private final @NonNull IntFunction<E> getFunction;
    private final @NonNull Supplier<ReadOnlySequencedCollection<E>> readOnlyReversedFunction;

    public ReadOnlyListFacade(@NonNull List<E> backingList) {
        this.sizeFunction = backingList::size;
        this.getFunction = backingList::get;
        this.readOnlyReversedFunction = () -> new ReadOnlyListFacade<>(
                sizeFunction,
                index -> getFunction.apply(sizeFunction.getAsInt() - index),
                () -> this);
    }

    public ReadOnlyListFacade(@NonNull IntSupplier sizeFunction, @NonNull IntFunction<E> getFunction, @NonNull Supplier<ReadOnlySequencedCollection<E>> readOnlyReversedFunction) {
        this.sizeFunction = sizeFunction;
        this.getFunction = getFunction;
        this.readOnlyReversedFunction = readOnlyReversedFunction;
    }

    @Override
    public E get(int index) {
        return getFunction.apply(index);
    }

    @Override
    public @NonNull ReadOnlySequencedCollection<E> readOnlyReversed() {
        return readOnlyReversedFunction.get();
    }

    @Override
    public @NonNull ReadOnlyList<E> readOnlySubList(int fromIndex, int toIndex) {
        int length = size();
        Objects.checkFromToIndex(fromIndex, toIndex, length);
        return new ReadOnlyListFacade<>(
                () -> toIndex - fromIndex,
                i -> getFunction.apply(i - fromIndex),
                readOnlyReversedFunction);
    }

    @Override
    public int size() {
        return sizeFunction.getAsInt();
    }


    @Override
    public boolean equals(Object o) {
        return ReadOnlyList.listEquals(this, o);
    }

    @Override
    public int hashCode() {
        return ReadOnlyList.iteratorToHashCode(iterator());
    }
}
