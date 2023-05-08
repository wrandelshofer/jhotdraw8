/*
 * @(#)SetFacade.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection.facade;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.readonly.ReadOnlySet;

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
 * Wraps {@code Set} functions into the {@link Set} interface.
 *
 * @param <E> the element type
 * @author Werner Randelshofer
 */
public class SetFacade<E> extends AbstractSet<E> implements ReadOnlySet<E> {
    protected final @NonNull Supplier<Iterator<E>> iteratorFunction;
    protected final @NonNull Supplier<Spliterator<E>> spliteratorFunction;
    protected final @NonNull IntSupplier sizeFunction;
    protected final @NonNull Predicate<Object> containsFunction;
    protected final @NonNull Predicate<E> addFunction;
    protected final @NonNull Runnable clearFunction;
    protected final @NonNull Predicate<Object> removeFunction;

    public SetFacade(@NonNull ReadOnlySet<E> backingSet) {
        this(backingSet::iterator, backingSet::spliterator, backingSet::size,
                backingSet::contains, null, null, null);
    }

    public SetFacade(@NonNull Set<E> backingSet) {
        this(backingSet::iterator,
                backingSet::spliterator,
                backingSet::size,
                backingSet::contains, backingSet::clear, backingSet::add, backingSet::remove);
    }

    public SetFacade(@NonNull Supplier<Iterator<E>> iteratorFunction,
                     @NonNull IntSupplier sizeFunction,
                     @NonNull Predicate<Object> containsFunction) {
        this(iteratorFunction,
                () -> Spliterators.spliterator(iteratorFunction.get(), sizeFunction.getAsInt(), Spliterator.DISTINCT),
                sizeFunction, containsFunction, null, null, null);
    }
    public SetFacade(@NonNull Supplier<Iterator<E>> iteratorFunction,
                     @NonNull Supplier<Spliterator<E>> spliteratorFunction,
                     @NonNull IntSupplier sizeFunction,
                     @NonNull Predicate<Object> containsFunction,
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

    @SuppressWarnings("unchecked")
    @Override
    public Spliterator<E> spliterator() {
        return (Spliterator<E>) spliteratorFunction.get();
    }

    @Override
    public Stream<E> stream() {
        return super.stream();
    }

    @Override
    public @NonNull Iterator<E> iterator() {
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
