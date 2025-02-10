/*
 * @(#)ReadableNavigableSetFacade.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.icollection.facade;

import org.jhotdraw8.icollection.readable.ReadableNavigableSet;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.Spliterator;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Provides a {@link ReadableNavigableSet} facade to a set of {@code NavigableSet} functions.
 *
 * @param <E> the element type
 */
public class ReadableNavigableSetFacade<E> extends ReadableSetFacade<E>
        implements ReadableNavigableSet<E> {

    final Supplier<E> getFirstFunction;
    final Function<E, E> ceilingFunction;
    final Function<E, E> floorFunction;
    final Function<E, E> higherFunction;
    final Function<E, E> lowerFunction;
    final Supplier<E> getLastFunction;
    final Supplier<Comparator<? super E>> comparatorSupplier;
    final Supplier<Iterator<E>> reverseIteratorFunction;

    public ReadableNavigableSetFacade(NavigableSet<E> s) {
        this(s::iterator, () -> s.reversed().iterator(), s::size,
                s::contains, s::getFirst, s::getLast,
                s::ceiling, s::floor, s::higher, s::lower, s::comparator,
                Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.ORDERED);
    }

    public ReadableNavigableSetFacade(Supplier<Iterator<E>> iteratorFunction,
                                      Supplier<Iterator<E>> reverseIteratorFunction,
                                      IntSupplier sizeFunction,
                                      Predicate<Object> containsFunction,
                                      Supplier<E> getFirstFunction,
                                      Supplier<E> getLastFunction,
                                      final Function<E, E> ceilingFunction,
                                      final Function<E, E> floorFunction,
                                      final Function<E, E> higherFunction,
                                      final Function<E, E> lowerFunction,
                                      final Supplier<Comparator<? super E>> comparatorSupplier,
                                      int characteristics) {
        super(iteratorFunction, sizeFunction, containsFunction, characteristics);
        this.getFirstFunction = getFirstFunction;
        this.getLastFunction = getLastFunction;
        this.reverseIteratorFunction = reverseIteratorFunction;
        this.ceilingFunction = ceilingFunction;
        this.floorFunction = floorFunction;
        this.higherFunction = higherFunction;
        this.lowerFunction = lowerFunction;
        this.comparatorSupplier = comparatorSupplier;
    }

    @Override
    public @Nullable E ceiling(E e) {
        return ceilingFunction.apply(e);
    }

    @Override
    public @Nullable Comparator<? super E> comparator() {
        return comparatorSupplier.get();
    }

    @Override
    public @Nullable E floor(E e) {
        return floorFunction.apply(e);
    }

    @Override
    public @Nullable E higher(E e) {
        return higherFunction.apply(e);
    }

    @Override
    public @Nullable E lower(E e) {
        return lowerFunction.apply(e);
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
    public ReadableNavigableSet<E> readOnlyReversed() {
        return new ReadableNavigableSetFacade<>(
                reverseIteratorFunction,
                iteratorFunction,
                sizeFunction,
                containsFunction,
                getLastFunction,
                getFirstFunction,
                floorFunction,
                ceilingFunction,
                lowerFunction,
                higherFunction,
                () -> comparatorSupplier.get().reversed(),
                super.characteristics);
    }
}
