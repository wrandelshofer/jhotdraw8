/*
 * @(#)ReadOnlySequencedSetFacade.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.icollection.facade;

import org.jhotdraw8.icollection.readonly.ReadOnlySequencedSet;

import java.util.Iterator;
import java.util.SequencedSet;
import java.util.Spliterator;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Provides a {@link ReadOnlySequencedSet} facade to a set of {@code SequencedSet} functions.
 *
 * @param <E> the element type
 * @author Werner Randelshofer
 */
public class ReadOnlySequencedSetFacade<E> extends ReadOnlySetFacade<E>
        implements ReadOnlySequencedSet<E> {

    final Supplier<E> getFirstFunction;
    final Supplier<E> getLastFunction;
    final Supplier<Iterator<E>> reverseIteratorFunction;

    public ReadOnlySequencedSetFacade(ReadOnlySequencedSet<E> backingSet) {
        this(backingSet::iterator, () -> backingSet.readOnlyReversed().iterator(), backingSet::size,
                backingSet::contains, backingSet::getFirst, backingSet::getLast, Spliterator.SIZED | Spliterator.DISTINCT);
    }

    public ReadOnlySequencedSetFacade(SequencedSet<E> backingSet) {
        this(backingSet::iterator, () -> backingSet.reversed().iterator(), backingSet::size,
                backingSet::contains, backingSet::getFirst, backingSet::getLast, Spliterator.SIZED | Spliterator.DISTINCT);
    }

    public ReadOnlySequencedSetFacade(Supplier<Iterator<E>> iteratorFunction,
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
    public ReadOnlySequencedSet<E> readOnlyReversed() {
        return new ReadOnlySequencedSetFacade<>(
                reverseIteratorFunction,
                iteratorFunction,
                sizeFunction,
                containsFunction,
                getLastFunction,
                getFirstFunction,
                super.characteristics);
    }
}
