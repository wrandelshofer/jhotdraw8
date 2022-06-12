/*
 * @(#)WrappedReadOnlyList.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.util.Preconditions;

import java.util.List;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;

/**
 * Wraps list functions in the {@link ReadOnlyList} interface.
 *
 * @author Werner Randelshofer
 */
public class WrappedReadOnlyList<E> extends AbstractReadOnlyList<E> {
    private final @NonNull IntSupplier sizeFunction;
    private final @NonNull IntFunction<E> getFunction;

    public WrappedReadOnlyList(@NonNull List<E> backingList) {
        this.sizeFunction = backingList::size;
        this.getFunction = backingList::get;
    }

    public WrappedReadOnlyList(@NonNull IntSupplier sizeFunction, @NonNull IntFunction<E> getFunction) {
        this.sizeFunction = sizeFunction;
        this.getFunction = getFunction;
    }

    @Override
    public E get(int index) {
        return getFunction.apply(index);
    }

    @Override
    public @NonNull ReadOnlySequencedCollection<E> readOnlyReversed() {
        return new WrappedReadOnlyList<>(
                sizeFunction,
                index -> getFunction.apply(sizeFunction.getAsInt() - index)
        );
    }

    @Override
    public @NonNull ReadOnlyList<E> readOnlySubList(int fromIndex, int toIndex) {
        Preconditions.checkFromToIndex(fromIndex, toIndex, size());
        return new WrappedReadOnlyList<>(
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
