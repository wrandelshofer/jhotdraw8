/*
 * @(#)SequencedCollectionFacade.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.icollection.facade;

import org.jhotdraw8.icollection.readable.ReadableCollection;
import org.jhotdraw8.icollection.readable.ReadableSequencedCollection;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Iterator;
import java.util.SequencedCollection;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Provides a {@link SequencedCollection} facade to a set of {@code Collection} functions.
 *
 * @param <E> the element type
 */
public class SequencedCollectionFacade<E> extends CollectionFacade<E> implements SequencedCollection<E> {
    private final Supplier<E> getFirstFunction;
    private final Supplier<E> getLastFunction;

    private final Consumer<E> addFirstFunction;
    private final Consumer<E> addLastFunction;
    private final Supplier<Iterator<E>> reverseIteratorFunction;

    public SequencedCollectionFacade(ReadableSequencedCollection<E> backingCollection) {
        this(backingCollection::iterator, () -> backingCollection.readableReversed().iterator(), backingCollection::size,
                backingCollection::contains, null, null, null, null, null, null, null);
    }

    public SequencedCollectionFacade(ReadableCollection<E> backingCollection,
                                     Supplier<Iterator<E>> reverseIteratorFunction) {
        this(backingCollection::iterator, reverseIteratorFunction, backingCollection::size,
                backingCollection::contains, null, null, null, null, null, null, null);
    }

    public SequencedCollectionFacade(Collection<E> backingCollection,
                                     Supplier<Iterator<E>> reverseIteratorFunction) {
        this(backingCollection::iterator, reverseIteratorFunction, backingCollection::size,
                backingCollection::contains, backingCollection::clear, backingCollection::remove,
                null, null, null, null, null);
    }

    public SequencedCollectionFacade(Supplier<Iterator<E>> iteratorFunction,
                                     Supplier<Iterator<E>> reverseIteratorFunction,
                                     IntSupplier sizeFunction,
                                     Predicate<Object> containsFunction) {
        this(iteratorFunction, reverseIteratorFunction, sizeFunction, containsFunction, null, null, null, null, null, null, null);
    }

    public SequencedCollectionFacade(Supplier<Iterator<E>> iteratorFunction,
                                     Supplier<Iterator<E>> reverseIteratorFunction,
                                     IntSupplier sizeFunction,
                                     Predicate<Object> containsFunction,
                                     @Nullable Runnable clearFunction,
                                     @Nullable Predicate<Object> removeFunction,
                                     @Nullable Supplier<E> getFirstFunction,
                                     @Nullable Supplier<E> getLastFunction,
                                     @Nullable Consumer<E> addFirstFunction,
                                     @Nullable Consumer<E> addLastFunction,
                                     @Nullable Predicate<E> addFunction) {
        super(iteratorFunction, null, sizeFunction, containsFunction, clearFunction, addFunction, removeFunction);
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
        this.reverseIteratorFunction = reverseIteratorFunction;
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

    @Override
    public SequencedCollection<E> reversed() {
        return new SequencedCollectionFacade<>(
                reverseIteratorFunction,
                iteratorFunction,
                sizeFunction,
                containsFunction,
                clearFunction,
                removeFunction,
                getLastFunction,
                getFirstFunction,
                addLastFunction,
                addFirstFunction, addFunction);
    }

    @Override
    public Spliterator<E> spliterator() {
        return Spliterators.spliterator(this, Spliterator.ORDERED);
    }
}
