/*
 * @(#)ReadOnlySortedSetFacade.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.icollection.facade;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.icollection.readonly.ReadOnlySortedSet;

import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.Spliterator;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Provides a {@link ReadOnlySortedSet} facade to a set of {@code SortedSet} functions.
 *
 * @param <E> the element type
 * @author Werner Randelshofer
 */
public class ReadOnlySortedSetFacade<E> extends ReadOnlySetFacade<E>
        implements ReadOnlySortedSet<E> {

    final @NonNull Supplier<E> getFirstFunction;

    final @NonNull Supplier<E> getLastFunction;
    final @NonNull Supplier<Comparator<? super E>> comparatorSupplier;
    final @NonNull Supplier<Iterator<E>> reverseIteratorFunction;

    public ReadOnlySortedSetFacade(@NonNull SortedSet<E> s) {
        this(s::iterator, () -> s.reversed().iterator(), s::size,
                s::contains, s::getFirst, s::getLast,
                s::comparator,
                Spliterator.SIZED | Spliterator.DISTINCT | Spliterator.ORDERED);
    }

    public ReadOnlySortedSetFacade(@NonNull Supplier<Iterator<E>> iteratorFunction,
                                   @NonNull Supplier<Iterator<E>> reverseIteratorFunction,
                                   @NonNull IntSupplier sizeFunction,
                                   @NonNull Predicate<Object> containsFunction,
                                   @NonNull Supplier<E> getFirstFunction,
                                   @NonNull Supplier<E> getLastFunction,

                                   final @NonNull Supplier<Comparator<? super E>> comparatorSupplier,
                                   int characteristics) {
        super(iteratorFunction, sizeFunction, containsFunction, characteristics);
        this.getFirstFunction = getFirstFunction;
        this.getLastFunction = getLastFunction;
        this.reverseIteratorFunction = reverseIteratorFunction;

        this.comparatorSupplier = comparatorSupplier;
    }

    @Override
    public @Nullable Comparator<? super E> comparator() {
        return comparatorSupplier.get();
    }


    @Override
    public E getLast() {
        return getLastFunction.get();
    }

    @Override
    public @NonNull ReadOnlySortedSet<E> readOnlyReversed() {
        return new ReadOnlySortedSetFacade<>(
                reverseIteratorFunction,
                iteratorFunction,
                sizeFunction,
                containsFunction,
                getLastFunction,
                getFirstFunction,
                () -> comparatorSupplier.get().reversed(),
                super.characteristics);
    }
}
