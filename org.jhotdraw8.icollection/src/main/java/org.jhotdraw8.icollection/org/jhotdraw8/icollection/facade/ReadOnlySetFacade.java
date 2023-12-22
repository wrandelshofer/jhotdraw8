/*
 * @(#)ReadOnlySetFacade.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.icollection.facade;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.icollection.readonly.AbstractReadOnlySet;
import org.jhotdraw8.icollection.readonly.ReadOnlyCollection;
import org.jhotdraw8.icollection.readonly.ReadOnlySet;

import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Provides a {@link ReadOnlySet} facade to a set of {@code Set} functions.
 *
 * @param <E> the element type
 * @author Werner Randelshofer
 */
public class ReadOnlySetFacade<E> extends AbstractReadOnlySet<E> {

    protected final Supplier<Iterator<E>> iteratorFunction;
    protected final IntSupplier sizeFunction;
    protected final Predicate<Object> containsFunction;
    /**
     * Characteristics of the spliterator.
     */
    protected final int characteristics;

    public ReadOnlySetFacade(@NonNull ReadOnlyCollection<E> backingSet) {
        this(backingSet::iterator, backingSet::size, backingSet::contains, Spliterator.SIZED | Spliterator.IMMUTABLE | Spliterator.DISTINCT);
    }

    public ReadOnlySetFacade(@NonNull Collection<E> backingSet) {
        this(backingSet::iterator, backingSet::size, backingSet::contains, Spliterator.SIZED | Spliterator.IMMUTABLE | Spliterator.DISTINCT);
    }

    public ReadOnlySetFacade(Supplier<Iterator<E>> iteratorFunction, IntSupplier sizeFunction, Predicate<Object> containsFunction, int characteristics) {
        this.iteratorFunction = iteratorFunction;
        this.sizeFunction = sizeFunction;
        this.containsFunction = containsFunction;
        this.characteristics = characteristics | Spliterator.SIZED | Spliterator.IMMUTABLE | Spliterator.DISTINCT;
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
        return Spliterators.spliterator(iterator(), size(), characteristics);
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
