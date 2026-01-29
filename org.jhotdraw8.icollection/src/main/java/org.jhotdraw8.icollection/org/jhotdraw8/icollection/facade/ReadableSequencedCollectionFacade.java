/*
 * @(#)ReadableSequencedCollectionFacade.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.icollection.facade;

import org.jhotdraw8.icollection.readable.ReadableSequencedCollection;
import org.jhotdraw8.icollection.readable.ReadableSequencedSet;

import java.util.Iterator;
import java.util.SequencedCollection;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Provides a {@link ReadableSequencedSet} facade to a set of {@code Set} functions.
 *
 * @param <E> the element type
 */
public class ReadableSequencedCollectionFacade<E> extends ReadableCollectionFacade<E>
        implements ReadableSequencedCollection<E> {

    final Supplier<E> getFirstFunction;
    final Supplier<E> getLastFunction;
    final Supplier<Iterator<E>> reverseIteratorFunction;

    public ReadableSequencedCollectionFacade(SequencedCollection<E> c) {
        this(c::iterator, () -> c.reversed().iterator(), c::size,
                c::contains, c::getFirst, c::getLast, Spliterator.SIZED);
    }

    public ReadableSequencedCollectionFacade(Supplier<Iterator<E>> iteratorFunction,
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
    public ReadableSequencedCollection<E> readableReversed() {
        return new ReadableSequencedCollectionFacade<>(
                reverseIteratorFunction,
                iteratorFunction,
                sizeFunction,
                containsFunction,
                getLastFunction,
                getFirstFunction,
                0);
    }
}
