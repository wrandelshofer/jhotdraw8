/*
 * @(#)SetFacade.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.icollection.facade;

import org.jhotdraw8.icollection.readonly.ReadOnlySet;
import org.jspecify.annotations.Nullable;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Provides a {@link Set} facade to a set of {@code Set} functions.
 *
 * @param <E> the element type
 * @author Werner Randelshofer
 */
public class SetFacade<E> extends AbstractSet<E> implements ReadOnlySet<E> {
    protected final Supplier<Iterator<E>> iteratorFunction;
    protected final Supplier<Spliterator<E>> spliteratorFunction;
    protected final IntSupplier sizeFunction;
    protected final Predicate<Object> containsFunction;
    protected final Predicate<E> addFunction;
    protected final Runnable clearFunction;
    protected final Predicate<Object> removeFunction;

    public SetFacade(ReadOnlySet<E> backingSet) {
        this(backingSet::iterator, backingSet::spliterator, backingSet::size,
                backingSet::contains, null, null, null);
    }

    public SetFacade(Set<E> backingSet) {
        this(backingSet::iterator,
                backingSet::spliterator,
                backingSet::size,
                backingSet::contains, backingSet::clear, backingSet::add, backingSet::remove);
    }

    public SetFacade(Supplier<Iterator<E>> iteratorFunction,
                     IntSupplier sizeFunction,
                     Predicate<Object> containsFunction) {
        this(iteratorFunction,
                () -> Spliterators.spliterator(iteratorFunction.get(), sizeFunction.getAsInt(), Spliterator.DISTINCT | Spliterator.SIZED),
                sizeFunction, containsFunction, null, null, null);
    }

    public SetFacade(Supplier<Iterator<E>> iteratorFunction,
                     Supplier<Spliterator<E>> spliteratorFunction,
                     IntSupplier sizeFunction,
                     Predicate<Object> containsFunction,
                     @Nullable Runnable clearFunction,
                     @Nullable Predicate<E> addFunction,
                     @Nullable Predicate<Object> removeFunction) {
        this.iteratorFunction = iteratorFunction;
        this.spliteratorFunction = spliteratorFunction;
        this.sizeFunction = sizeFunction;
        this.containsFunction = containsFunction;
        this.clearFunction = clearFunction == null ? () -> {
            throw new UnsupportedOperationException();
        } : clearFunction;
        this.removeFunction = removeFunction == null ? o -> {
            throw new UnsupportedOperationException();
        } : removeFunction;
        this.addFunction = addFunction == null ? o -> {
            throw new UnsupportedOperationException();
        } : addFunction;

    }

    @Override
    public boolean remove(Object o) {
        return removeFunction.test(o);
    }

    @Override
    public void clear() {
        clearFunction.run();
    }

    @Override
    public Spliterator<E> spliterator() {
        return spliteratorFunction.get();
    }

    @Override
    public Stream<E> stream() {
        return super.stream();
    }

    @Override
    public Iterator<E> iterator() {
        return iteratorFunction.get();
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
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
    public boolean add(E e) {
        return addFunction.test(e);
    }
}
