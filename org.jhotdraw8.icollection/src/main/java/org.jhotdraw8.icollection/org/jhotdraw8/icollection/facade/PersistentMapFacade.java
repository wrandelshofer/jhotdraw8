/*
 * @(#)PersistentMapFacade.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.facade;

import org.jhotdraw8.icollection.impl.iteration.Iterators;
import org.jhotdraw8.icollection.persistent.PersistentMap;
import org.jhotdraw8.icollection.readable.AbstractReadableMap;
import org.jhotdraw8.icollection.readable.ReadableCollection;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Function;

/**
 * Provides a {@link PersistentMap} facade to a set of {@code PersistentMap} functions.
 *
 * @param <K> the key type
 * @param <V> the element type
 */
public class PersistentMapFacade<K, V> extends AbstractReadableMap<K, V> implements PersistentMap<K, V> {

    private final Map<K, V> target;
    private final Function<Map<K, V>, Map<K, V>> cloneFunction;

    public PersistentMapFacade(Map<K, V> target, Function<Map<K, V>, Map<K, V>> cloneFunction) {
        this.target = target;
        this.cloneFunction = cloneFunction;
    }

    @Override
    public PersistentMapFacade<K, V> clear() {
        if (isEmpty()) {
            return this;
        }
        Map<K, V> clone = cloneFunction.apply(target);
        clone.clear();
        return new PersistentMapFacade<>(clone, cloneFunction);
    }

    @Override
    public PersistentMapFacade<K, V> put(K key, @Nullable V value) {
        if (containsKey(key) && Objects.equals(get(key), value)) {
            return this;
        }
        Map<K, V> clone = cloneFunction.apply(target);
        clone.put(key, value);
        return new PersistentMapFacade<>(clone, cloneFunction);
    }


    @Override
    public PersistentMapFacade<K, V> putAll(Iterable<? extends Map.Entry<? extends K, ? extends V>> c) {
        Map<K, V> clone = cloneFunction.apply(target);
        for (Map.Entry<? extends K, ? extends V> e : c) {
            clone.put(e.getKey(), e.getValue());
        }
        if (clone.equals(target)) {
            return this;
        }
        return new PersistentMapFacade<>(clone, cloneFunction);
    }

    @Override
    public PersistentMapFacade<K, V> remove(K key) {
        if (!containsKey(key)) {
            return this;
        }
        Map<K, V> clone = cloneFunction.apply(target);
        clone.remove(key);
        return new PersistentMapFacade<>(clone, cloneFunction);
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @Override
    public PersistentMapFacade<K, V> removeAll(Iterable<? extends K> c) {
        if (isEmpty()) {
            return this;
        }
        Map<K, V> clone = cloneFunction.apply(target);
        if (c instanceof Collection<?> coll) {
            if (coll.isEmpty()) {
                return this;
            }
            clone.keySet().removeAll(coll);
        } else {
            boolean changed = false;
            for (K k : c) {
                changed |= clone.containsKey(k);
                clone.remove(k);
            }
            if (!changed) {
                return this;
            }
        }
        return new PersistentMapFacade<>(clone, cloneFunction);
    }

    @SuppressWarnings("unchecked")
    @Override
    public PersistentMapFacade<K, V> retainAll(Iterable<? extends K> c) {
        if (isEmpty()) {
            return this;
        }
        Map<K, V> clone = cloneFunction.apply(target);
        Collection<K> collection;
        if (c instanceof ReadableCollection<?> rc) {
            collection = (Collection<K>) rc.asCollection();
        } else if (c instanceof Collection<?> cc) {
            collection = (Collection<K>) cc;
        } else {
            collection = new HashSet<>();
            c.forEach(collection::add);
        }
        return clone.keySet().retainAll(collection) ? new PersistentMapFacade<>(clone, cloneFunction) : this;
    }

    @Override
    public boolean isEmpty() {
        return target.isEmpty();
    }

    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
        return Iterators.unmodifiableIterator(target.entrySet().iterator());
    }

    @Override
    public int maxSize() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int size() {
        return target.size();
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @Override
    public @Nullable V get(Object key) {
        return target.get(key);
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @Override
    public boolean containsKey(@Nullable Object key) {
        return target.containsKey(key);
    }

    @Override
    public Map<K, V> toMutable() {
        return cloneFunction.apply(target);
    }

    @Override
    public int characteristics() {
        return Spliterator.IMMUTABLE | super.characteristics();
    }
}
