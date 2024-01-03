/*
 * @(#)ImmutableSetFacade.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.facade;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.icollection.immutable.ImmutableSet;
import org.jhotdraw8.icollection.impl.iteration.Iterators;
import org.jhotdraw8.icollection.readonly.AbstractReadOnlySet;
import org.jhotdraw8.icollection.readonly.ReadOnlyCollection;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Function;

/**
 * Provides a {@link ImmutableSet} facade to a set of {@code ImmutableSet} functions.
 *
 * @param <E> the element type
 */
public class ImmutableSetFacade<E> extends AbstractReadOnlySet<E> implements ImmutableSet<E> {
    private final @NonNull Set<E> target;
    private final @NonNull Function<Set<E>, Set<E>> cloneFunction;

    public ImmutableSetFacade(@NonNull Set<E> target, @NonNull Function<Set<E>, Set<E>> cloneFunction) {
        this.target = target;
        this.cloneFunction = cloneFunction;
    }

    @Override
    public @NonNull ImmutableSet<E> clear() {
        if (isEmpty()) {
            return this;
        }
        Set<E> clone = cloneFunction.apply(target);
        clone.clear();
        return new ImmutableSetFacade<>(clone, cloneFunction);
    }

    @Override
    public @NonNull ImmutableSet<E> add(E element) {
        Set<E> clone = cloneFunction.apply(target);
        return clone.add(element) ? new ImmutableSetFacade<>(clone, cloneFunction) : this;
    }

    @Override
    public @NonNull ImmutableSet<E> addAll(@NonNull Iterable<? extends E> c) {
        Set<E> clone = cloneFunction.apply(target);
        boolean changed = false;
        for (E e : c) {
            changed |= clone.add(e);
        }
        return changed ? new ImmutableSetFacade<>(clone, cloneFunction) : this;
    }

    @Override
    public @NonNull ImmutableSet<E> remove(E element) {
        Set<E> clone = cloneFunction.apply(target);
        return clone.remove(element) ? new ImmutableSetFacade<>(clone, cloneFunction) : this;
    }

    @Override
    public @NonNull ImmutableSet<E> removeAll(@NonNull Iterable<?> c) {
        Set<E> clone = cloneFunction.apply(target);
        boolean changed = false;
        for (Object e : c) {
            changed |= clone.remove(e);
        }
        return changed ? new ImmutableSetFacade<>(clone, cloneFunction) : this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NonNull ImmutableSet<E> retainAll(@NonNull Iterable<?> c) {
        if (isEmpty()) return this;
        Set<E> clone = cloneFunction.apply(target);
        Collection<E> collection;
        if (c instanceof ReadOnlyCollection<?> rc) {
            collection = (Collection<E>) rc.asCollection();
        } else if (c instanceof Collection<?> cc) {
            collection = (Collection<E>) cc;
        } else {
            collection = new HashSet<E>();
            c.forEach(e -> collection.add((E) e));
        }
        return clone.retainAll(collection) ? new ImmutableSetFacade<>(clone, cloneFunction) : this;
    }

    @Override
    public int size() {
        return target.size();
    }

    @Override
    public boolean contains(Object o) {
        return target.contains(o);
    }

    @Override
    public int maxSize() {
        return Integer.MAX_VALUE;
    }

    @Override
    public @NonNull Iterator<E> iterator() {
        return Iterators.unmodifiableIterator(target.iterator());
    }

    @Override
    public @NonNull Set<E> toMutable() {
        return cloneFunction.apply(target);
    }
}
