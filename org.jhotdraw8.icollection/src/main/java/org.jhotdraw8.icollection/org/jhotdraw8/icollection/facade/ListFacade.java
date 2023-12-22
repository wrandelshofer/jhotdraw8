/*
 * @(#)ListFacade.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.icollection.facade;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.icollection.readonly.ReadOnlyList;

import java.util.AbstractList;
import java.util.List;
import java.util.SequencedCollection;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.stream.Stream;

/**
 * Provides a {@link List} facade to a set of {@code List} functions.
 *
 * @param <E> the element type
 * @author Werner Randelshofer
 */
public class ListFacade<E> extends AbstractList<E>
        implements SequencedCollection<E> {
    private final @NonNull IntSupplier sizeFunction;
    private final @NonNull IntFunction<E> getFunction;
    private final @NonNull BiConsumer<Integer, E> addFunction;
    private final @NonNull IntFunction<E> removeFunction;
    private final @NonNull Runnable clearFunction;

    public ListFacade(@NonNull ReadOnlyList<E> backingList) {
        this(backingList::size, backingList::get, null, null, null);
    }

    public ListFacade(@NonNull List<E> backingList) {
        this(backingList::size, backingList::get, backingList::clear,
                backingList::add, backingList::remove);
    }

    public ListFacade(@NonNull IntSupplier sizeFunction,
                      @NonNull IntFunction<E> getFunction
    ) {
        this(sizeFunction, getFunction, null, null, null);
    }

    public ListFacade(@NonNull IntSupplier sizeFunction,
                      @NonNull IntFunction<E> getFunction,
                      @Nullable Runnable clearFunction,
                      @Nullable BiConsumer<Integer, E> addFunction,
                      @Nullable IntFunction<E> removeFunction
    ) {
        this.sizeFunction = sizeFunction;
        this.getFunction = getFunction;
        this.addFunction = addFunction == null ? (i, e) -> {
            throw new UnsupportedOperationException();
        } : addFunction;
        this.clearFunction = clearFunction == null ? () -> {
            throw new UnsupportedOperationException();
        } : clearFunction;
        this.removeFunction = removeFunction == null ? (i) -> {
            throw new UnsupportedOperationException();
        } : removeFunction;
    }

    @Override
    public E get(int index) {
        return getFunction.apply(index);
    }


    @Override
    public Spliterator<E> spliterator() {
        return super.spliterator();
    }

    @Override
    public E remove(int index) {
        return removeFunction.apply(index);
    }

    @Override
    public int size() {
        return sizeFunction.getAsInt();
    }

    @Override
    public void addFirst(E e) {
        addFunction.accept(0, e);
    }

    @Override
    public void addLast(E e) {
        addFunction.accept(size(), e);
    }

    @Override
    public Stream<E> stream() {
        return super.stream();
    }

    @Override
    public @NonNull List<E> reversed() {
        return new ListFacade<>(
                sizeFunction,
                i -> get(size() - i - 1),
                clearFunction,
                (i, e) -> add(size() - i, e),
                (i) -> remove(size() - i - i)
        );
    }
}
