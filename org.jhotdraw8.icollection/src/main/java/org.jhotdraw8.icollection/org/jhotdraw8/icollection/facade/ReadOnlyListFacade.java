/*
 * @(#)ReadOnlyListFacade.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.icollection.facade;

import org.jhotdraw8.icollection.readonly.AbstractReadOnlyList;
import org.jhotdraw8.icollection.readonly.ReadOnlyList;
import org.jhotdraw8.icollection.readonly.ReadOnlySequencedCollection;

import java.util.List;
import java.util.Objects;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * Provides a {@link ReadOnlyList} facade to a set of {@code ReadOnlyList} functions.
 *
 * @param <E> the element type
 * @author Werner Randelshofer
 */
public class ReadOnlyListFacade<E> extends AbstractReadOnlyList<E> {
    private final IntSupplier sizeFunction;
    private final IntFunction<E> getFunction;
    private final Supplier<ReadOnlySequencedCollection<E>> readOnlyReversedFunction;

    public ReadOnlyListFacade(List<E> backingList) {
        this.sizeFunction = backingList::size;
        this.getFunction = backingList::get;
        this.readOnlyReversedFunction = () -> new ReadOnlyListFacade<>(
                sizeFunction,
                index -> getFunction.apply(sizeFunction.getAsInt() - index),
                () -> this);
    }

    public ReadOnlyListFacade(IntSupplier sizeFunction, IntFunction<E> getFunction) {
        this.sizeFunction = sizeFunction;
        this.getFunction = getFunction;
        this.readOnlyReversedFunction = () -> new ReadOnlyListFacade<>(
                sizeFunction,
                index -> getFunction.apply(sizeFunction.getAsInt() - index),
                () -> this);
    }

    public ReadOnlyListFacade(IntSupplier sizeFunction, IntFunction<E> getFunction, Supplier<ReadOnlySequencedCollection<E>> readOnlyReversedFunction) {
        this.sizeFunction = sizeFunction;
        this.getFunction = getFunction;
        this.readOnlyReversedFunction = readOnlyReversedFunction;
    }

    @Override
    public E get(int index) {
        return getFunction.apply(index);
    }

    @Override
    public ReadOnlySequencedCollection<E> readOnlyReversed() {
        return readOnlyReversedFunction.get();
    }

    @Override
    public ReadOnlyList<E> readOnlySubList(int fromIndex, int toIndex) {
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


}
