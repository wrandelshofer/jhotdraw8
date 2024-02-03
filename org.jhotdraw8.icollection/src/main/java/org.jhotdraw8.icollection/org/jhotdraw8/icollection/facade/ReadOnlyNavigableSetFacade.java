/*
 * @(#)ReadOnlyNavigableSetFacade.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.icollection.facade;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.icollection.readonly.ReadOnlyNavigableSet;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.Spliterator;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Provides a {@link ReadOnlyNavigableSet} facade to a set of {@code NavigableSet} functions.
 *
 * @param <E> the element type
 * @author Werner Randelshofer
 */
public class ReadOnlyNavigableSetFacade<E> extends ReadOnlySetFacade<E>
        implements ReadOnlyNavigableSet<E> {

    final @NonNull Supplier<E> getFirstFunction;
    final @NonNull Function<E, E> ceilingFunction;
    final @NonNull Function<E, E> floorFunction;
    final @NonNull Function<E, E> higherFunction;
    final @NonNull Function<E, E> lowerFunction;
    final @NonNull Supplier<E> getLastFunction;
    final @NonNull Supplier<Comparator<? super E>> comparatorSupplier;
    final @NonNull Supplier<Iterator<E>> reverseIteratorFunction;

    public ReadOnlyNavigableSetFacade(@NonNull NavigableSet<E> s) {
        this(s::iterator, () -> s.reversed().iterator(), s::size,
                s::contains, s::getFirst, s::getLast,
                s::ceiling, s::floor, s::higher, s::lower, s::comparator,
                Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.ORDERED);
    }

    public ReadOnlyNavigableSetFacade(@NonNull Supplier<Iterator<E>> iteratorFunction,
                                      @NonNull Supplier<Iterator<E>> reverseIteratorFunction,
                                      @NonNull IntSupplier sizeFunction,
                                      @NonNull Predicate<Object> containsFunction,
                                      @NonNull Supplier<E> getFirstFunction,
                                      @NonNull Supplier<E> getLastFunction,
                                      final @NonNull Function<E, E> ceilingFunction,
                                      final @NonNull Function<E, E> floorFunction,
                                      final @NonNull Function<E, E> higherFunction,
                                      final @NonNull Function<E, E> lowerFunction,
                                      final @NonNull Supplier<Comparator<? super E>> comparatorSupplier,
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
    public @NonNull ReadOnlyNavigableSet<E> readOnlyReversed() {
        return new ReadOnlyNavigableSetFacade<>(
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
