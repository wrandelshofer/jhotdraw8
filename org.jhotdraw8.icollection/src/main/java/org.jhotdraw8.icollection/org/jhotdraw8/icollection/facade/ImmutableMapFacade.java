/*
 * @(#)ImmutableMapFacade.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.facade;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.icollection.immutable.ImmutableMap;
import org.jhotdraw8.icollection.impl.iteration.Iterators;
import org.jhotdraw8.icollection.readonly.AbstractReadOnlyMap;
import org.jhotdraw8.icollection.readonly.ReadOnlyCollection;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Provides a {@link ImmutableMap} facade to a set of {@code ImmutableMap} functions.
 *
 * @param <K> the key type
 * @param <V> the element type
 */
public class ImmutableMapFacade<K, V> extends AbstractReadOnlyMap<K, V> implements ImmutableMap<K, V> {

    private final @NonNull Map<K, V> target;
    private final @NonNull Function<Map<K, V>, Map<K, V>> cloneFunction;

    public ImmutableMapFacade(@NonNull Map<K, V> target, @NonNull Function<Map<K, V>, Map<K, V>> cloneFunction) {
        this.target = target;
        this.cloneFunction = cloneFunction;
    }

    @Override
    public @NonNull ImmutableMapFacade<K, V> clear() {
        if (isEmpty()) {
            return this;
        }
        Map<K, V> clone = cloneFunction.apply(target);
        clone.clear();
        return new ImmutableMapFacade<>(clone, cloneFunction);
    }

    @Override
    public @NonNull ImmutableMapFacade<K, V> put(@NonNull K key, @Nullable V value) {
        if (containsKey(key) && Objects.equals(get(key), value)) {
            return this;
        }
        Map<K, V> clone = cloneFunction.apply(target);
        clone.put(key, value);
        return new ImmutableMapFacade<>(clone, cloneFunction);
    }


    @Override
    public @NonNull ImmutableMapFacade<K, V> putAll(@NonNull Iterable<? extends Map.Entry<? extends K, ? extends V>> c) {
        Map<K, V> clone = cloneFunction.apply(target);
        for (Map.Entry<? extends K, ? extends V> e : c) {
            clone.put(e.getKey(), e.getValue());
        }
        if (clone.equals(target)) {
            return this;
        }
        return new ImmutableMapFacade<>(clone, cloneFunction);
    }

    @Override
    public @NonNull ImmutableMapFacade<K, V> remove(@NonNull K key) {
        if (!containsKey(key)) {
            return this;
        }
        Map<K, V> clone = cloneFunction.apply(target);
        clone.remove(key);
        return new ImmutableMapFacade<>(clone, cloneFunction);
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @Override
    public @NonNull ImmutableMapFacade<K, V> removeAll(@NonNull Iterable<? extends K> c) {
        if (isEmpty()) {
            return this;
        }
        Map<K, V> clone = cloneFunction.apply(target);
        if (c instanceof Collection<?>) {
            Collection<?> coll = (Collection<?>) c;
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
        return new ImmutableMapFacade<>(clone, cloneFunction);
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NonNull ImmutableMapFacade<K, V> retainAll(@NonNull Iterable<? extends K> c) {
        if (isEmpty()) return this;
        Map<K, V> clone = cloneFunction.apply(target);
        Collection<K> collection;
        if (c instanceof ReadOnlyCollection<?> rc) {
            collection = (Collection<K>) rc.asCollection();
        } else if (c instanceof Collection<?> cc) {
            collection = (Collection<K>) cc;
        } else {
            collection = new HashSet<K>();
            c.forEach(collection::add);
        }
        return clone.keySet().retainAll(collection) ? new ImmutableMapFacade<>(clone, cloneFunction) : this;
    }

    @Override
    public boolean isEmpty() {
        return target.isEmpty();
    }

    @Override
    public @NonNull Iterator<Map.Entry<K, V>> iterator() {
        return Iterators.unmodifiableIterator(target.entrySet().iterator());
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
    public @NonNull Map<K, V> toMutable() {
        return cloneFunction.apply(target);
    }
}
