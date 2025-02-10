/*
 * @(#)SequencedSetFacade.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.icollection.facade;

import org.jhotdraw8.icollection.readable.ReadableCollection;
import org.jhotdraw8.icollection.readable.ReadableSequencedCollection;
import org.jhotdraw8.icollection.sequenced.ReversedSequencedSetView;
import org.jspecify.annotations.Nullable;

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
 */
public class SequencedSetFacade<E> extends SetFacade<E> implements SequencedSet<E> {

    private final Supplier<E> getFirstFunction;
    private final Supplier<E> getLastFunction;
    private final Consumer<E> addFirstFunction;
    private final Consumer<E> addLastFunction;
    private final Supplier<Iterator<E>> reverseIteratorFunction;
    private final Supplier<Spliterator<E>> reverseSpliteratorFunction;
    private final Predicate<E> reversedAddFunction;

    public SequencedSetFacade(ReadableCollection<E> backingSet,
                              Supplier<Iterator<E>> reverseIteratorFunction) {
        this(backingSet::iterator, backingSet::spliterator,
                reverseIteratorFunction,
                () -> Spliterators.spliterator(reverseIteratorFunction.get(), backingSet.size(), Spliterator.DISTINCT),
                backingSet::size,
                backingSet::contains, null, null, null, null, null, null, null, null);
    }

    public SequencedSetFacade(ReadableSequencedCollection<E> backingSet) {
        this(backingSet::iterator, backingSet::spliterator, () -> backingSet.readOnlyReversed().iterator(),
                () -> backingSet.readOnlyReversed().spliterator(), backingSet::size,
                backingSet::contains, null, null,
                backingSet::getFirst,
                backingSet::getLast,
                null, null, null, null);
    }

    public SequencedSetFacade(Set<E> backingSet,
                              Supplier<Iterator<E>> reverseIteratorFunction) {
        this(backingSet::iterator, backingSet::spliterator,
                reverseIteratorFunction, () -> Spliterators.spliterator(reverseIteratorFunction.get(), backingSet.size(), Spliterator.DISTINCT), backingSet::size,
                backingSet::contains, backingSet::clear, backingSet::remove, null, null, null, null, null, null);
    }

    public SequencedSetFacade(Supplier<Iterator<E>> iteratorFunction,
                              Supplier<Iterator<E>> reverseIteratorFunction,
                              IntSupplier sizeFunction,
                              Predicate<Object> containsFunction) {
        this(iteratorFunction,
                () -> Spliterators.spliterator(iteratorFunction.get(), sizeFunction.getAsInt(), Spliterator.DISTINCT), reverseIteratorFunction,
                () -> Spliterators.spliterator(reverseIteratorFunction.get(), sizeFunction.getAsInt(), Spliterator.DISTINCT),
                sizeFunction, containsFunction, null, null, null, null, null, null, null, null);
    }

    public SequencedSetFacade(Supplier<Iterator<E>> iteratorFunction,
                              Supplier<Spliterator<E>> spliteratorFunction,
                              Supplier<Iterator<E>> reverseIteratorFunction,
                              Supplier<Spliterator<E>> reverseSpliteratorFunction,
                              IntSupplier sizeFunction,
                              Predicate<Object> containsFunction,
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
    public SequencedSet<E> reversed() {
        return new ReversedSequencedSetView<>(this, reverseIteratorFunction, reverseSpliteratorFunction);
    }
}
