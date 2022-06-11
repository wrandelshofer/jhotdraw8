/*
 * @(#)WrappedSequencedCollection.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Wraps {@code Collection} functions into the {@link Collection} interface.
 *
 * @author Werner Randelshofer
 */
public class WrappedSequencedCollection<E> extends WrappedCollection<E> implements SequencedCollection<E> {

    final @NonNull Supplier<E> getFirstFunction;
    final @NonNull Supplier<E> getLastFunction;

    final @NonNull Consumer<E> addFirstFunction;
    final @NonNull Consumer<E> addLastFunction;

    public WrappedSequencedCollection(@NonNull ReadOnlyCollection<E> backingCollection) {
        this((Supplier<Iterator<E>>) backingCollection::iterator, backingCollection::size,
                backingCollection::contains, null, null, null, null, null, null);
    }

    public WrappedSequencedCollection(@NonNull Collection<E> backingCollection) {
        this(backingCollection::iterator, backingCollection::size,
                backingCollection::contains, backingCollection::clear, backingCollection::remove,
                null, null, null, null);
    }

    public WrappedSequencedCollection(@NonNull Supplier<Iterator<E>> iteratorFunction,
                                      @NonNull IntSupplier sizeFunction,
                                      @NonNull Predicate<Object> containsFunction) {
        this(iteratorFunction, sizeFunction, containsFunction, null, null, null, null, null, null);
    }

    public WrappedSequencedCollection(@NonNull Supplier<Iterator<E>> iteratorFunction,
                                      @NonNull IntSupplier sizeFunction,
                                      @NonNull Predicate<Object> containsFunction,
                                      @Nullable Runnable clearFunction,
                                      @Nullable Predicate<Object> removeFunction,
                                      @Nullable Supplier<E> getFirstFunction,
                                      @Nullable Supplier<E> getLastFunction,
                                      @Nullable Consumer<E> addFirstFunction,
                                      @Nullable Consumer<E> addLastFunction) {
        super(iteratorFunction, sizeFunction, containsFunction, clearFunction, removeFunction);
        this.getFirstFunction = getFirstFunction == null ? () -> {
            throw new UnsupportedOperationException();
        } : getFirstFunction;
        this.getLastFunction = getLastFunction == null ? () -> {
            throw new UnsupportedOperationException();
        } : getLastFunction;
        this.addFirstFunction = addFirstFunction == null ? e -> {
            throw new UnsupportedOperationException();
        } : addFirstFunction;
        this.addLastFunction = addLastFunction == null ? e -> {
            throw new UnsupportedOperationException();
        } : addLastFunction;
    }

    @Override
    public void addFirst(E e) {
        addFirstFunction.accept(e);
    }

    @Override
    public void addLast(E e) {
        addLastFunction.accept(e);
    }

    @Override
    public E getFirst() {
        return getFirstFunction.get();
    }

    @Override
    public E getLast() {
        return getLastFunction.get();
    }
}
