/*
 * @(#)WrappedSequencedSet.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.util.Iterator;
import java.util.Set;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Wraps {@code Set} functions into the {@link Set} interface.
 *
 * @author Werner Randelshofer
 */
public class WrappedSequencedSet<E> extends WrappedSet<E> implements SequencedSet<E> {

    final @NonNull Supplier<E> getFirstFunction;
    final @NonNull Supplier<E> getLastFunction;

    public WrappedSequencedSet(@NonNull ReadOnlySet<E> backingSet) {
        this(backingSet::iterator, backingSet::size,
                backingSet::contains, null, null, null, null);
    }

    public WrappedSequencedSet(@NonNull Set<E> backingSet) {
        this(backingSet::iterator, backingSet::size,
                backingSet::contains, backingSet::clear, backingSet::remove, null, null);
    }

    public WrappedSequencedSet(@NonNull Supplier<Iterator<E>> iteratorFunction,
                               @NonNull IntSupplier sizeFunction,
                               @NonNull Predicate<Object> containsFunction) {
        this(iteratorFunction, sizeFunction, containsFunction, null, null, null, null);
    }

    public WrappedSequencedSet(@NonNull Supplier<Iterator<E>> iteratorFunction,
                               @NonNull IntSupplier sizeFunction,
                               @NonNull Predicate<Object> containsFunction,
                               @Nullable Runnable clearFunction,
                               @Nullable Predicate<Object> removeFunction,
                               @Nullable Supplier<E> getFirstFunction,
                               @Nullable Supplier<E> getLastFunction) {
        super(iteratorFunction, sizeFunction, containsFunction, clearFunction, removeFunction);
        this.getFirstFunction = getFirstFunction == null ? () -> {
            throw new UnsupportedOperationException();
        } : getFirstFunction;
        this.getLastFunction = getLastFunction == null ? () -> {
            throw new UnsupportedOperationException();
        } : getLastFunction;
    }

    @Override
    public boolean addFirst(E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addLast(E e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public E removeLast() {
        E e = getLastFunction.get();
        removeFunction.test(e);
        return e;
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
