/*
 * @(#)WrappedReadOnlySequencedSet.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

import java.util.Iterator;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Wraps set functions into the {@link ReadOnlySequencedSet} interface.
 *
 * @author Werner Randelshofer
 */
public class WrappedReadOnlySequencedSet<E> extends WrappedReadOnlySet<E>
        implements ReadOnlySequencedSet<E> {

    final @NonNull Supplier<E> getFirstFunction;
    final @NonNull Supplier<E> getLastFunction;
    final @NonNull Supplier<Iterator<E>> reversedIteratorFunction;

    public WrappedReadOnlySequencedSet(@NonNull ReadOnlySequencedSet<E> backingSet) {
        this(backingSet::iterator, () -> backingSet.readOnlyReversed().iterator(), backingSet::size,
                backingSet::contains, backingSet::getFirst, backingSet::getLast);
    }

    public WrappedReadOnlySequencedSet(@NonNull SequencedSet<E> backingSet) {
        this(backingSet::iterator, () -> backingSet.reversed().iterator(), backingSet::size,
                backingSet::contains, backingSet::getFirst, backingSet::getLast);
    }

    public WrappedReadOnlySequencedSet(@NonNull Supplier<Iterator<E>> iteratorFunction,
                                       @NonNull Supplier<Iterator<E>> reversedIteratorFunction,
                                       @NonNull IntSupplier sizeFunction,
                                       @NonNull Predicate<Object> containsFunction,
                                       @NonNull Supplier<E> getFirstFunction,
                                       @NonNull Supplier<E> getLastFunction) {
        super(iteratorFunction, sizeFunction, containsFunction);
        this.getFirstFunction = getFirstFunction;
        this.getLastFunction = getLastFunction;
        this.reversedIteratorFunction = reversedIteratorFunction;
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
    public @NonNull ReadOnlySequencedSet<E> readOnlyReversed() {
        return new WrappedReadOnlySequencedSet<>(
                reversedIteratorFunction,
                iteratorFunction,
                sizeFunction,
                containsFunction,
                getLastFunction,
                getFirstFunction
        );
    }
}
