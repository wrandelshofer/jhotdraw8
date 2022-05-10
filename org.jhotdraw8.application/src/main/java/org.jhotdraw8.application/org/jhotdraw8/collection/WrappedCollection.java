/*
 * @(#)WrappedCollection.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Wraps {@code Collection} functions into the {@link Collection} interface.
 *
 * @author Werner Randelshofer
 */
public class WrappedCollection<E> extends AbstractCollection<E> {
    private final @NonNull Supplier<Iterator<E>> iteratorFunction;
    private final @NonNull IntSupplier sizeFunction;
    private final @NonNull Predicate<Object> containsFunction;
    private final @NonNull Runnable clearFunction;
    protected final @NonNull Predicate<Object> removeFunction;

    public WrappedCollection(@NonNull ReadOnlyCollection<E> backingCollection) {
        this(backingCollection::iterator, backingCollection::size,
                backingCollection::contains, null, null);
    }

    public WrappedCollection(@NonNull Collection<E> backingCollection) {
        this(backingCollection::iterator, backingCollection::size,
                backingCollection::contains, backingCollection::clear, backingCollection::remove);
    }

    public WrappedCollection(@NonNull Supplier<Iterator<E>> iteratorFunction,
                             @NonNull IntSupplier sizeFunction,
                             @NonNull Predicate<Object> containsFunction) {
        this(iteratorFunction, sizeFunction, containsFunction, null, null);
    }

    public WrappedCollection(@NonNull Supplier<Iterator<E>> iteratorFunction,
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
