/*
 * @(#)SetWrapper.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Wraps {@code Set} functions into the {@link Set} API.
 *
 * @author Werner Randelshofer
 */
public class WrappedSet<E> extends AbstractSet<E> {
    private final @NonNull Supplier<Iterator<E>> iteratorFunction;
    private final @NonNull IntSupplier sizeFunction;
    private final @NonNull Predicate<Object> containsFunction;
    private final @NonNull Runnable clearFunction;
    private final @NonNull Predicate<Object> removeFunction;

    public WrappedSet(ReadOnlySet<E> backingSet) {
        this(backingSet::iterator, backingSet::size,
                backingSet::contains, null, null);
    }

    public WrappedSet(Set<E> backingSet) {
        this(backingSet::iterator, backingSet::size,
                backingSet::contains, backingSet::clear, backingSet::remove);
    }

    public WrappedSet(@NonNull Supplier<Iterator<E>> iteratorFunction,
                      @NonNull IntSupplier sizeFunction,
                      @NonNull Predicate<Object> containsFunction) {
        this(iteratorFunction, sizeFunction, containsFunction, null, null);
    }

    public WrappedSet(@NonNull Supplier<Iterator<E>> iteratorFunction,
                      @NonNull IntSupplier sizeFunction,
                      @NonNull Predicate<Object> containsFunction,
                      @Nullable Runnable clearFunction,
                      @Nullable Predicate<Object> removeFunction) {
        this.iteratorFunction = iteratorFunction;
        this.sizeFunction = sizeFunction;
        this.containsFunction = containsFunction;
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
    public void clear() {
        clearFunction.run();
    }

    @Override
    public @NonNull Iterator<E> iterator() {
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
