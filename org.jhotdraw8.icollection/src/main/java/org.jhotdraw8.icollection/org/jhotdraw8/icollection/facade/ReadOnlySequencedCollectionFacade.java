/*
 * @(#)ReadOnlySequencedCollectionFacade.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.icollection.facade;


import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.icollection.readonly.ReadOnlySequencedCollection;
import org.jhotdraw8.icollection.readonly.ReadOnlySequencedSet;

import java.util.Comparator;
import java.util.Iterator;
import java.util.SequencedCollection;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Provides a {@link ReadOnlySequencedSet} facade to a set of {@code Set} functions.
 *
 * @param <E> the element type
 * @author Werner Randelshofer
 */
public class ReadOnlySequencedCollectionFacade<E> extends ReadOnlyCollectionFacade<E>
        implements ReadOnlySequencedCollection<E> {

    final @NonNull Supplier<E> getFirstFunction;
    final @NonNull Supplier<E> getLastFunction;
    final @NonNull Supplier<Iterator<E>> reverseIteratorFunction;
    private final @Nullable Comparator<E> comparator;

    public ReadOnlySequencedCollectionFacade(@NonNull SequencedCollection<E> c) {
        this(c::iterator, () -> c.reversed().iterator(), c::size,
                c::contains, c::getFirst, c::getLast, Spliterator.SIZED, null);
    }

    public ReadOnlySequencedCollectionFacade(@NonNull Supplier<Iterator<E>> iteratorFunction,
                                             @NonNull Supplier<Iterator<E>> reverseIteratorFunction,
                                             @NonNull IntSupplier sizeFunction,
                                             @NonNull Predicate<Object> containsFunction,
                                             @NonNull Supplier<E> getFirstFunction,
                                             @NonNull Supplier<E> getLastFunction, int spliteratorCharacteristics,
                                             @Nullable Comparator<E> comparator) {
        super(iteratorFunction, sizeFunction, containsFunction, spliteratorCharacteristics);
        this.getFirstFunction = getFirstFunction;
        this.getLastFunction = getLastFunction;
        this.reverseIteratorFunction = reverseIteratorFunction;
        this.comparator = comparator;
    }

    @Override
    public Spliterator<E> spliterator() {
        return Spliterators.spliterator(iterator(), size(), characteristics);
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
    public @NonNull ReadOnlySequencedCollection<E> readOnlyReversed() {
        return new ReadOnlySequencedCollectionFacade<>(
                reverseIteratorFunction,
                iteratorFunction,
                sizeFunction,
                containsFunction,
                getLastFunction,
                getFirstFunction,
                0, null);
    }
}
