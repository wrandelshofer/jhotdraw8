/*
 * @(#)WrappedReadOnlySet.java
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

    public WrappedReadOnlySequencedSet(@NonNull ReadOnlySequencedSet<E> backingSet) {
        this(backingSet::iterator, backingSet::size,
                backingSet::contains, backingSet::getFirst, backingSet::getLast);
    }

    public WrappedReadOnlySequencedSet(@NonNull SequencedSet<E> backingSet) {
        this(backingSet::iterator, backingSet::size,
                backingSet::contains, backingSet::getFirst, backingSet::getLast);
    }

    public WrappedReadOnlySequencedSet(@NonNull Supplier<Iterator<E>> iteratorFunction,
                                       @NonNull IntSupplier sizeFunction,
                                       @NonNull Predicate<Object> containsFunction,
                                       @NonNull Supplier<E> getFirstFunction,
                                       @NonNull Supplier<E> getLastFunction) {
        super(iteratorFunction, sizeFunction, containsFunction);
        this.getFirstFunction = getFirstFunction;
        this.getLastFunction = getLastFunction;
    }


    @Override
    public E getFirst() {
        return getFirstFunction.get();
    }

    @Override
    public E getLast() {
        return getLastFunction.get();
    }

}
