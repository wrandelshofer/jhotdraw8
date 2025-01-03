/*
 * @(#)PersistentSetFacade.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.facade;

import org.jhotdraw8.icollection.impl.iteration.IteratorSpliterator;
import org.jhotdraw8.icollection.impl.iteration.Iterators;
import org.jhotdraw8.icollection.persistent.PersistentSet;
import org.jhotdraw8.icollection.readable.AbstractReadableSet;
import org.jhotdraw8.icollection.readable.ReadableCollection;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Function;

/**
 * Provides a {@link PersistentSet} facade to a set of {@code PersistentSet} functions.
 *
 * @param <E> the element type
 */
public class PersistentSetFacade<E> extends AbstractReadableSet<E> implements PersistentSet<E> {
    private final Set<E> target;
    private final Function<Set<E>, Set<E>> cloneFunction;

    public PersistentSetFacade(Set<E> target, Function<Set<E>, Set<E>> cloneFunction) {
        this.target = target;
        this.cloneFunction = cloneFunction;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> PersistentSet<T> empty() {
        return new PersistentSetFacade<>(new LinkedHashSet<>(), k -> (Set<T>) ((LinkedHashSet<?>) k).clone());
    }

    @Override
    public PersistentSet<E> add(E element) {
        Set<E> clone = cloneFunction.apply(target);
        return clone.add(element) ? new PersistentSetFacade<>(clone, cloneFunction) : this;
    }

    @Override
    public PersistentSet<E> addAll(Iterable<? extends E> c) {
        Set<E> clone = cloneFunction.apply(target);
        boolean changed = false;
        for (E e : c) {
            changed |= clone.add(e);
        }
        return changed ? new PersistentSetFacade<>(clone, cloneFunction) : this;
    }

    @Override
    public PersistentSet<E> remove(E element) {
        Set<E> clone = cloneFunction.apply(target);
        return clone.remove(element) ? new PersistentSetFacade<>(clone, cloneFunction) : this;
    }

    @Override
    public PersistentSet<E> removeAll(Iterable<?> c) {
        Set<E> clone = cloneFunction.apply(target);
        boolean changed = false;
        for (Object e : c) {
            changed |= clone.remove(e);
        }
        return changed ? new PersistentSetFacade<>(clone, cloneFunction) : this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public PersistentSet<E> retainAll(Iterable<?> c) {
        if (isEmpty()) {
            return this;
        }
        Set<E> clone = cloneFunction.apply(target);
        Collection<E> collection;
        if (c instanceof ReadableCollection<?> rc) {
            collection = (Collection<E>) rc.asCollection();
        } else if (c instanceof Collection<?> cc) {
            collection = (Collection<E>) cc;
        } else {
            collection = new HashSet<>();
            c.forEach(e -> collection.add((E) e));
        }
        return clone.retainAll(collection) ? new PersistentSetFacade<>(clone, cloneFunction) : this;
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
    public Iterator<E> iterator() {
        return Iterators.unmodifiableIterator(target.iterator());
    }

    @Override
    public Spliterator<E> spliterator() {
        return new IteratorSpliterator<>(iterator(), size(), characteristics(), null);
    }

    @Override
    public Set<E> toMutable() {
        return cloneFunction.apply(target);
    }


    @Override
    public int characteristics() {
        return Spliterator.IMMUTABLE | super.characteristics();
    }
}
