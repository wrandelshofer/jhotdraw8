/*
 * @(#)ReadOnlySequencedCollectionFacade.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.icollection.facade;

import org.jhotdraw8.icollection.readonly.ReadOnlySequencedCollection;
import org.jhotdraw8.icollection.readonly.ReadOnlySequencedSet;

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

    final Supplier<E> getFirstFunction;
    final Supplier<E> getLastFunction;
    final Supplier<Iterator<E>> reverseIteratorFunction;

    public ReadOnlySequencedCollectionFacade(SequencedCollection<E> c) {
        this(c::iterator, () -> c.reversed().iterator(), c::size,
                c::contains, c::getFirst, c::getLast, Spliterator.SIZED);
    }

    public ReadOnlySequencedCollectionFacade(Supplier<Iterator<E>> iteratorFunction,
                                             Supplier<Iterator<E>> reverseIteratorFunction,
                                             IntSupplier sizeFunction,
                                             Predicate<Object> containsFunction,
                                             Supplier<E> getFirstFunction,
                                             Supplier<E> getLastFunction, int spliteratorCharacteristics) {
        super(iteratorFunction, sizeFunction, containsFunction, spliteratorCharacteristics);
        this.getFirstFunction = getFirstFunction;
        this.getLastFunction = getLastFunction;
        this.reverseIteratorFunction = reverseIteratorFunction;
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
    public ReadOnlySequencedCollection<E> readOnlyReversed() {
        return new ReadOnlySequencedCollectionFacade<>(
                reverseIteratorFunction,
                iteratorFunction,
                sizeFunction,
                containsFunction,
                getLastFunction,
                getFirstFunction,
                0);
    }
}
