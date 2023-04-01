/*
 * @(#)WrappedReadOnlyList.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection.facade;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.readonly.AbstractReadOnlyList;
import org.jhotdraw8.collection.readonly.ReadOnlyList;
import org.jhotdraw8.collection.readonly.ReadOnlySequencedCollection;

import java.util.List;
import java.util.Objects;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;

/**
 * Wraps {@code List} functions in the {@link ReadOnlyList} interface.
 *
 * @param <E> the element type
 * @author Werner Randelshofer
 */
public class ReadOnlyListFacade<E> extends AbstractReadOnlyList<E> {
    private final @NonNull IntSupplier sizeFunction;
    private final @NonNull IntFunction<E> getFunction;

    public ReadOnlyListFacade(@NonNull List<E> backingList) {
        this.sizeFunction = backingList::size;
        this.getFunction = backingList::get;
    }

    public ReadOnlyListFacade(@NonNull IntSupplier sizeFunction, @NonNull IntFunction<E> getFunction) {
        this.sizeFunction = sizeFunction;
        this.getFunction = getFunction;
    }

    @Override
    public E get(int index) {
        return getFunction.apply(index);
    }

    @Override
    public @NonNull ReadOnlySequencedCollection<E> readOnlyReversed() {
        return new ReadOnlyListFacade<>(
                sizeFunction,
                index -> getFunction.apply(sizeFunction.getAsInt() - index)
        );
    }

    @Override
    public @NonNull ReadOnlyList<E> readOnlySubList(int fromIndex, int toIndex) {
        int length = size();
        Objects.checkFromToIndex(fromIndex, toIndex, length);
        return new ReadOnlyListFacade<>(
                () -> toIndex - fromIndex,
                i -> getFunction.apply(i - fromIndex)
        );
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
