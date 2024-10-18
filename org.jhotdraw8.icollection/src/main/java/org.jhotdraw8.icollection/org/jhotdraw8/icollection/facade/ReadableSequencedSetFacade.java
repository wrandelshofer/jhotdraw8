/*
 * @(#)ReadableSequencedSetFacade.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.icollection.facade;

import org.jhotdraw8.icollection.readable.ReadableSequencedSet;

import java.util.Iterator;
import java.util.SequencedSet;
import java.util.Spliterator;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Provides a {@link ReadableSequencedSet} facade to a set of {@code SequencedSet} functions.
 *
 * @param <E> the element type
 * @author Werner Randelshofer
 */
public class ReadableSequencedSetFacade<E> extends ReadableSetFacade<E>
        implements ReadableSequencedSet<E> {

    final Supplier<E> getFirstFunction;
    final Supplier<E> getLastFunction;
    final Supplier<Iterator<E>> reverseIteratorFunction;

    public ReadableSequencedSetFacade(ReadableSequencedSet<E> backingSet) {
        this(backingSet::iterator, () -> backingSet.readOnlyReversed().iterator(), backingSet::size,
                backingSet::contains, backingSet::getFirst, backingSet::getLast, Spliterator.SIZED | Spliterator.DISTINCT);
    }

    public ReadableSequencedSetFacade(SequencedSet<E> backingSet) {
        this(backingSet::iterator, () -> backingSet.reversed().iterator(), backingSet::size,
                backingSet::contains, backingSet::getFirst, backingSet::getLast, Spliterator.SIZED | Spliterator.DISTINCT);
    }

    public ReadableSequencedSetFacade(Supplier<Iterator<E>> iteratorFunction,
                                      Supplier<Iterator<E>> reverseIteratorFunction,
                                      IntSupplier sizeFunction,
                                      Predicate<Object> containsFunction,
                                      Supplier<E> getFirstFunction,
                                      Supplier<E> getLastFunction, int characteristics) {
        super(iteratorFunction, sizeFunction, containsFunction, characteristics);
        this.getFirstFunction = getFirstFunction;
        this.getLastFunction = getLastFunction;
        this.reverseIteratorFunction = reverseIteratorFunction;
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
    public ReadableSequencedSet<E> readOnlyReversed() {
        return new ReadableSequencedSetFacade<>(
                reverseIteratorFunction,
                iteratorFunction,
                sizeFunction,
                containsFunction,
                getLastFunction,
                getFirstFunction,
                super.characteristics);
    }
}
