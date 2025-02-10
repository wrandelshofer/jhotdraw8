/*
 * @(#)CollectionFacade.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.icollection.facade;

import org.jhotdraw8.icollection.impl.iteration.IteratorSpliterator;
import org.jhotdraw8.icollection.readable.ReadableCollection;
import org.jspecify.annotations.Nullable;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Provides a {@link Collection} facade to a set of {@code Collection} functions.
 *
 * @param <E> the element type
 */
public class CollectionFacade<E> extends AbstractCollection<E> implements ReadableCollection<E> {
    protected final Supplier<Iterator<E>> iteratorFunction;
    protected final Supplier<Spliterator<E>> spliteratorFunction;
    protected final IntSupplier sizeFunction;
    protected final Predicate<Object> containsFunction;
    protected final Runnable clearFunction;
    protected final Predicate<Object> removeFunction;
    protected final Predicate<E> addFunction;

    public CollectionFacade(ReadableCollection<E> backingCollection) {
        this(backingCollection::iterator, backingCollection::spliterator, backingCollection::size,
                backingCollection::contains, null, null, null);
    }

    public CollectionFacade(Collection<E> backingCollection) {
        this(backingCollection::iterator, backingCollection::spliterator, backingCollection::size,
                backingCollection::contains, backingCollection::clear, backingCollection::add, backingCollection::remove);
    }

    public CollectionFacade(Supplier<Iterator<E>> iteratorFunction,
                            IntSupplier sizeFunction,
                            Predicate<Object> containsFunction) {
        this(iteratorFunction, null,
                sizeFunction, containsFunction, null, null, null);
    }

    public CollectionFacade(Supplier<Iterator<E>> iteratorFunction,
                            @Nullable Supplier<Spliterator<E>> spliteratorFunction,
                            IntSupplier sizeFunction,
                            Predicate<Object> containsFunction,
                            @Nullable Runnable clearFunction,
                            @Nullable Predicate<E> addFunction,
                            @Nullable Predicate<Object> removeFunction) {
        this.iteratorFunction = iteratorFunction;
        this.spliteratorFunction = spliteratorFunction == null ? () -> new IteratorSpliterator<>(iteratorFunction.get(), sizeFunction.getAsInt(), Spliterator.SIZED, null) : spliteratorFunction;
        this.sizeFunction = sizeFunction;
        this.containsFunction = containsFunction;
        this.addFunction = addFunction == null ? o -> {
            throw new UnsupportedOperationException();
        } : addFunction;
        this.clearFunction = clearFunction == null ? () -> {
            throw new UnsupportedOperationException();
        } : clearFunction;
        this.removeFunction = removeFunction == null ? o -> {
            throw new UnsupportedOperationException();
        } : removeFunction;

    }
    @Override
    public boolean remove(Object o) {
        return removeFunction.test(o);
    }

    @Override
    public boolean add(E e) {
        return addFunction.test(e);
    }
    @Override
    public void clear() {
        clearFunction.run();
    }

    @Override
    public Stream<E> stream() {
        return super.stream();
    }

    @Override
    public Spliterator<E> spliterator() {
        return spliteratorFunction.get();
    }


    @Override
    public Iterator<E> iterator() {
        return iteratorFunction.get();
    }

    @Override
    public int size() {
        return sizeFunction.getAsInt();
    }

    @Override
    public boolean contains(Object o) {
        return containsFunction.test(o);
    }
}
