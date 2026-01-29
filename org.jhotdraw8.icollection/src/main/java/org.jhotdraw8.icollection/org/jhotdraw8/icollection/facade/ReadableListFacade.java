/*
 * @(#)ReadableListFacade.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.icollection.facade;

import org.jhotdraw8.icollection.readable.AbstractReadableList;
import org.jhotdraw8.icollection.readable.ReadableList;
import org.jhotdraw8.icollection.readable.ReadableSequencedCollection;

import java.util.List;
import java.util.Objects;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * Provides a {@link ReadableList} facade to a set of {@code ReadableList} functions.
 *
 * @param <E> the element type
 */
public class ReadableListFacade<E> extends AbstractReadableList<E> {
    private final IntSupplier sizeFunction;
    private final IntFunction<E> getFunction;
    private final Supplier<ReadableSequencedCollection<E>> readableReversedFunction;

    public ReadableListFacade(List<E> backingList) {
        this.sizeFunction = backingList::size;
        this.getFunction = backingList::get;
        this.readableReversedFunction = () -> new ReadableListFacade<>(
                sizeFunction,
                index -> getFunction.apply(sizeFunction.getAsInt() - index),
                () -> this);
    }

    public ReadableListFacade(IntSupplier sizeFunction, IntFunction<E> getFunction) {
        this.sizeFunction = sizeFunction;
        this.getFunction = getFunction;
        this.readableReversedFunction = () -> new ReadableListFacade<>(
                sizeFunction,
                index -> getFunction.apply(sizeFunction.getAsInt() - index),
                () -> this);
    }

    public ReadableListFacade(IntSupplier sizeFunction, IntFunction<E> getFunction, Supplier<ReadableSequencedCollection<E>> readableReversedFunction) {
        this.sizeFunction = sizeFunction;
        this.getFunction = getFunction;
        this.readableReversedFunction = readableReversedFunction;
    }

    @Override
    public E get(int index) {
        return getFunction.apply(index);
    }

    @Override
    public ReadableSequencedCollection<E> readableReversed() {
        return readableReversedFunction.get();
    }

    @Override
    public ReadableList<E> readableSubList(int fromIndex, int toIndex) {
        int length = size();
        Objects.checkFromToIndex(fromIndex, toIndex, length);
        return new ReadableListFacade<>(
                () -> toIndex - fromIndex,
                i -> getFunction.apply(i - fromIndex),
                readableReversedFunction);
    }

    @Override
    public int size() {
        return sizeFunction.getAsInt();
    }


}
