/*
 * @(#)SequencedSetFacade.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection.facade;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.readonly.ReadOnlyCollection;
import org.jhotdraw8.collection.readonly.ReadOnlySequencedCollection;
import org.jhotdraw8.collection.sequenced.SequencedSet;

import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Wraps {@code Set} functions into the {@link SequencedSet} interface.
 *
 * @param <E> the element type
 * @author Werner Randelshofer
 */
public class SequencedSetFacade<E> extends SetFacade<E> implements SequencedSet<E> {

    private final @NonNull Supplier<E> getFirstFunction;
    private final @NonNull Supplier<E> getLastFunction;
    private final @NonNull Consumer<E> addFirstFunction;
    private final @NonNull Consumer<E> addLastFunction;
    private final @NonNull Supplier<Iterator<E>> reverseIteratorFunction;
    private final @NonNull Supplier<Spliterator<E>> reverseSpliteratorFunction;
    private final @NonNull Predicate<E> reversedAddFunction;

    public SequencedSetFacade(@NonNull ReadOnlyCollection<E> backingSet,
                              @NonNull Supplier<Iterator<E>> reverseIteratorFunction) {
        this(backingSet::iterator, backingSet::spliterator,
                reverseIteratorFunction,
                () -> Spliterators.spliterator(reverseIteratorFunction.get(), backingSet.size(), Spliterator.DISTINCT),
                backingSet::size,
                backingSet::contains, null, null, null, null, null, null, null, null);
    }

    public SequencedSetFacade(@NonNull ReadOnlySequencedCollection<E> backingSet) {
        this(backingSet::iterator, backingSet::spliterator, () -> backingSet.readOnlyReversed().iterator(),
                () -> backingSet.readOnlyReversed().spliterator(), backingSet::size,
                backingSet::contains, null, null, null, null, null, null, null, null);
    }

    public SequencedSetFacade(@NonNull Set<E> backingSet,
                              @NonNull Supplier<Iterator<E>> reverseIteratorFunction) {
        this(backingSet::iterator, backingSet::spliterator,
                reverseIteratorFunction, () -> Spliterators.spliterator(reverseIteratorFunction.get(), backingSet.size(), Spliterator.DISTINCT), backingSet::size,
                backingSet::contains, backingSet::clear, backingSet::remove, null, null, null, null, null, null);
    }

    public SequencedSetFacade(@NonNull Supplier<Iterator<E>> iteratorFunction,
                              @NonNull Supplier<Iterator<E>> reverseIteratorFunction,
                              @NonNull IntSupplier sizeFunction,
                              @NonNull Predicate<Object> containsFunction) {
        this(iteratorFunction,
                () -> Spliterators.spliterator(iteratorFunction.get(), sizeFunction.getAsInt(), Spliterator.DISTINCT), reverseIteratorFunction,
                () -> Spliterators.spliterator(reverseIteratorFunction.get(), sizeFunction.getAsInt(), Spliterator.DISTINCT),
                sizeFunction, containsFunction, null, null, null, null, null, null, null, null);
    }

    public SequencedSetFacade(@NonNull Supplier<Iterator<E>> iteratorFunction,
                              @NonNull Supplier<Spliterator<E>> spliteratorFunction,
                              @NonNull Supplier<Iterator<E>> reverseIteratorFunction,
                              @NonNull Supplier<Spliterator<E>> reverseSpliteratorFunction,
                              @NonNull IntSupplier sizeFunction,
                              @NonNull Predicate<Object> containsFunction,
                              @Nullable Runnable clearFunction,
                              @Nullable Predicate<Object> removeFunction,
                              @Nullable Supplier<E> getFirstFunction,
                              @Nullable Supplier<E> getLastFunction,
                              @Nullable Predicate<E> addFunction,
                              @Nullable Predicate<E> reversedAddFunction,
                              @Nullable Consumer<E> addFirstFunction,
                              @Nullable Consumer<E> addLastFunction) {
        super(iteratorFunction, spliteratorFunction, sizeFunction, containsFunction, clearFunction, addFunction, removeFunction);
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
        this.reversedAddFunction = reversedAddFunction == null ? o -> {
            throw new UnsupportedOperationException();
        } : reversedAddFunction;
        this.reverseIteratorFunction = reverseIteratorFunction;
        this.reverseSpliteratorFunction = reverseSpliteratorFunction;
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

    @Override
    public @NonNull SequencedSet<E> _reversed() {
        return new SequencedSetFacade<>(
                reverseIteratorFunction,
                spliteratorFunction, iteratorFunction,
                reverseSpliteratorFunction, sizeFunction,
                containsFunction,
                clearFunction,
                removeFunction,
                getLastFunction,
                getFirstFunction,
                reversedAddFunction,
                addFunction,
                addLastFunction,
                addFirstFunction);
    }
}
