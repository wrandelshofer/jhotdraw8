/*
 * @(#)ReadableCollectionFacade.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.icollection.facade;

import org.jhotdraw8.icollection.impl.iteration.IteratorSpliterator;
import org.jhotdraw8.icollection.readable.AbstractReadableCollection;
import org.jhotdraw8.icollection.readable.ReadableCollection;
import org.jhotdraw8.icollection.readable.ReadableSet;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Provides a {@link ReadableCollection} facade to a set of {@code Collection} functions.
 *
 * @param <E> the element type
 */
public class ReadableCollectionFacade<E> extends AbstractReadableCollection<E> {

    protected final Supplier<Iterator<E>> iteratorFunction;
    protected final IntSupplier sizeFunction;
    protected final Predicate<Object> containsFunction;
    protected final int characteristics;


    public ReadableCollectionFacade(Supplier<Iterator<E>> iteratorFunction,
                                    @Nullable IntSupplier sizeFunction,
                                    Predicate<Object> containsFunction, int characteristics) {
        this.iteratorFunction = iteratorFunction;
        this.sizeFunction = sizeFunction;
        this.containsFunction = containsFunction;
        this.characteristics = characteristics;
    }

    public ReadableCollectionFacade(Collection<E> c) {
        this(c::iterator, c::size, c::contains, Spliterator.SIZED | Spliterator.SUBSIZED);
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
    public Iterator<E> iterator() {
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
    public Spliterator<E> spliterator() {
        return new IteratorSpliterator<>(iterator(), size(), characteristics, null);
    }

    @Override
    public boolean equals(Object o) {
        return ReadableSet.setEquals(new ReadableSetFacade<>(this), o);
    }

    @Override
    public int hashCode() {
        return ReadableSet.iteratorToHashCode(iteratorFunction.get());
    }
}
