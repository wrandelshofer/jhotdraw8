/*
 * @(#)SequencedCollectionFacade.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection.facade;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.readonly.ReadOnlyCollection;
import org.jhotdraw8.collection.readonly.ReadOnlySequencedCollection;
import org.jhotdraw8.collection.sequenced.SequencedCollection;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Wraps {@code Collection} functions into the {@link Collection} interface.
 *
 * @param <E> the element type
 * @author Werner Randelshofer
 */
public class SequencedCollectionFacade<E> extends CollectionFacade<E> implements SequencedCollection<E> {
    private final @NonNull Supplier<E> getFirstFunction;
    private final @NonNull Supplier<E> getLastFunction;

    private final @NonNull Consumer<E> addFirstFunction;
    private final @NonNull Consumer<E> addLastFunction;
    private final @NonNull Supplier<Iterator<E>> reversedIteratorFunction;

    public SequencedCollectionFacade(@NonNull ReadOnlySequencedCollection<E> backingCollection) {
        this(backingCollection::iterator, () -> backingCollection.readOnlyReversed().iterator(), backingCollection::size,
                backingCollection::contains, null, null, null, null, null, null);
    }

    public SequencedCollectionFacade(@NonNull ReadOnlyCollection<E> backingCollection,
                                     @NonNull Supplier<Iterator<E>> reversedIteratorFunction) {
        this(backingCollection::iterator, reversedIteratorFunction, backingCollection::size,
                backingCollection::contains, null, null, null, null, null, null);
    }

    public SequencedCollectionFacade(@NonNull Collection<E> backingCollection,
                                     @NonNull Supplier<Iterator<E>> reversedIteratorFunction) {
        this(backingCollection::iterator, reversedIteratorFunction, backingCollection::size,
                backingCollection::contains, backingCollection::clear, backingCollection::remove,
                null, null, null, null);
    }

    public SequencedCollectionFacade(@NonNull Supplier<Iterator<E>> iteratorFunction,
                                     @NonNull Supplier<Iterator<E>> reversedIteratorFunction,
                                     @NonNull IntSupplier sizeFunction,
                                     @NonNull Predicate<Object> containsFunction) {
        this(iteratorFunction, reversedIteratorFunction, sizeFunction, containsFunction, null, null, null, null, null, null);
    }

    public SequencedCollectionFacade(@NonNull Supplier<Iterator<E>> iteratorFunction,
                                     @NonNull Supplier<Iterator<E>> reversedIteratorFunction,
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
        this.reversedIteratorFunction = reversedIteratorFunction;
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
    public @NonNull SequencedCollection<E> reversed() {
        return new SequencedCollectionFacade<>(
                reversedIteratorFunction,
                iteratorFunction,
                sizeFunction,
                containsFunction,
                clearFunction,
                removeFunction,
                getLastFunction,
                getFirstFunction,
                addLastFunction,
                addFirstFunction);
    }
}
