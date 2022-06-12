/*
 * @(#)WrappedReadOnlySet.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Wraps set functions into the {@link ReadOnlySet} interface.
 *
 * @author Werner Randelshofer
 */
public class WrappedReadOnlySet<E> extends AbstractReadOnlySet<E> {

    protected final Supplier<Iterator<E>> iteratorFunction;
    protected final IntSupplier sizeFunction;
    protected final Predicate<Object> containsFunction;

    public WrappedReadOnlySet(@NonNull ReadOnlyCollection<E> backingSet) {
        this(backingSet::iterator, backingSet::size, backingSet::contains);
    }

    public WrappedReadOnlySet(@NonNull Collection<E> backingSet) {
        this(backingSet::iterator, backingSet::size, backingSet::contains);
    }

    public WrappedReadOnlySet(Supplier<Iterator<E>> iteratorFunction, IntSupplier sizeFunction, Predicate<Object> containsFunction) {
        this.iteratorFunction = iteratorFunction;
        this.sizeFunction = sizeFunction;
        this.containsFunction = containsFunction;
    }

    @Override
    public int size() {
        return sizeFunction.getAsInt();
    }

    @Override
    public boolean contains(Object o) {
        return containsFunction.test(o);
    }

    @Override
    public @NonNull Iterator<E> iterator() {
        return new Iterator<E>() {
            private final Iterator<? extends E> i = iteratorFunction.get();

            @Override
            public boolean hasNext() {
                return i.hasNext();
            }

            @Override
            public E next() {
                return i.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public boolean equals(Object o) {
        return ReadOnlySet.setEquals(this, o);
    }

    @Override
    public int hashCode() {
        return ReadOnlySet.iteratorToHashCode(iteratorFunction.get());
    }
}
