/*
 * @(#)ReadOnlyCollectionFacade.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.immutable_collection.facade;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.immutable_collection.readonly.AbstractReadOnlyCollection;
import org.jhotdraw8.immutable_collection.readonly.ReadOnlyCollection;
import org.jhotdraw8.immutable_collection.readonly.ReadOnlySet;

import java.util.Iterator;
import java.util.Set;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Wraps {@code Collection} functions into the {@link ReadOnlyCollection} interface.
 *
 * @param <E> the element type
 * @author Werner Randelshofer
 */
public class ReadOnlyCollectionFacade<E> extends AbstractReadOnlyCollection<E> {

    protected final Supplier<Iterator<E>> iteratorFunction;
    protected final IntSupplier sizeFunction;
    protected final Predicate<Object> containsFunction;

    public ReadOnlyCollectionFacade(@NonNull Set<E> backingSet) {
        this(backingSet::iterator, backingSet::size, backingSet::contains);
    }

    public ReadOnlyCollectionFacade(Supplier<Iterator<E>> iteratorFunction, IntSupplier sizeFunction, Predicate<Object> containsFunction) {
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
        return new Iterator<>() {
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
        return ReadOnlySet.setEquals(new ReadOnlySetFacade<>(this), o);
    }

    @Override
    public int hashCode() {
        return ReadOnlySet.iteratorToHashCode(iteratorFunction.get());
    }
}
