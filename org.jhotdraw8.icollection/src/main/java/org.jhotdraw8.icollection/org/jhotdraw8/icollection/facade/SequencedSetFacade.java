/*
 * @(#)SequencedSetFacade.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.icollection.facade;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.icollection.readonly.ReadOnlyCollection;
import org.jhotdraw8.icollection.readonly.ReadOnlySequencedCollection;
import org.jhotdraw8.icollection.sequenced.ReversedSequencedSetView;

import java.util.Iterator;
import java.util.SequencedSet;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Provides a {@link SequencedSet} facade to a set of {@code Set} functions.
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
                backingSet::contains, null, null,
                backingSet::getFirst,
                backingSet::getLast,
                null, null, null, null);
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
        Supplier<E> throwingSupplier = () -> {
            throw new UnsupportedOperationException();
        };
        this.getFirstFunction = getFirstFunction == null ? throwingSupplier : getFirstFunction;
        this.getLastFunction = getLastFunction == null ? throwingSupplier : getLastFunction;
        Consumer<E> throwingConsumer = e -> {
            throw new UnsupportedOperationException();
        };
        Predicate<E> throwingPredicate = e -> {
            throw new UnsupportedOperationException();
        };
        this.addFirstFunction = addFirstFunction == null ? throwingConsumer : addFirstFunction;
        this.addLastFunction = addLastFunction == null ? throwingConsumer : addLastFunction;
        this.reversedAddFunction = reversedAddFunction == null ? throwingPredicate : reversedAddFunction;
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
    public @NonNull SequencedSet<E> reversed() {
        return new ReversedSequencedSetView<>(this, reverseIteratorFunction, reverseSpliteratorFunction);
    }
}
