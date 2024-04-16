/*
 * @(#)ReadOnlyCollectionFacade.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.icollection.facade;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.icollection.impl.iteration.IteratorSpliterator;
import org.jhotdraw8.icollection.readonly.AbstractReadOnlyCollection;
import org.jhotdraw8.icollection.readonly.ReadOnlyCollection;
import org.jhotdraw8.icollection.readonly.ReadOnlySet;

import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Provides a {@link ReadOnlyCollection} facade to a set of {@code Collection} functions.
 *
 * @param <E> the element type
 * @author Werner Randelshofer
 */
public class ReadOnlyCollectionFacade<E> extends AbstractReadOnlyCollection<E> {

    protected final @NonNull Supplier<Iterator<E>> iteratorFunction;
    protected final @NonNull IntSupplier sizeFunction;
    protected final @NonNull Predicate<Object> containsFunction;
    protected final int characteristics;


    public ReadOnlyCollectionFacade(@NonNull Supplier<Iterator<E>> iteratorFunction,
                                    @Nullable IntSupplier sizeFunction,
                                    @NonNull Predicate<Object> containsFunction, int characteristics) {
        this.iteratorFunction = iteratorFunction;
        this.sizeFunction = sizeFunction;
        this.containsFunction = containsFunction;
        this.characteristics = characteristics;
    }

    public ReadOnlyCollectionFacade(@NonNull Collection<E> c) {
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
    public Spliterator<E> spliterator() {
        return new IteratorSpliterator<>(iterator(), size(), characteristics, null);
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
