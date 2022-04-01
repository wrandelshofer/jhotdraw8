/*
 * @(#)ReadOnlyListWrapper.java
 * Copyright © 2021 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.util.Preconditions;

import java.util.List;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;

/**
 * Wraps a list in the {@link ReadOnlyList} API.
 * <p>
 * The underlying List is referenced - not copied.
 *
 * @author Werner Randelshofer
 */
public final class ReadOnlyListWrapper<E> extends AbstractReadOnlyList<E> {
    private final @NonNull IntSupplier sizeFunction;
    private final @NonNull IntFunction<E> getFunction;

    public ReadOnlyListWrapper(@NonNull List<E> backingList) {
        this.sizeFunction = backingList::size;
        this.getFunction = backingList::get;
    }

    public ReadOnlyListWrapper(@NonNull IntSupplier sizeFunction, @NonNull IntFunction<E> getFunction) {
        this.sizeFunction = sizeFunction;
        this.getFunction = getFunction;
    }

    @Override
    public E get(int index) {
        return getFunction.apply(index);
    }

    @Override
    public @NonNull ReadOnlyList<E> readOnlySubList(int fromIndex, int toIndex) {
        Preconditions.checkFromToIndex(fromIndex, toIndex, size());
        return new ReadOnlyListWrapper<>(
                () -> toIndex - fromIndex,
                i -> getFunction.apply(i - fromIndex)
        );
    }

    @Override
    public int size() {
        return sizeFunction.getAsInt();
    }
}
