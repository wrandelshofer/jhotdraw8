/*
 * @(#)WrappedMap.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Wraps map functions into the {@link java.util.Map} interface.
 *
 * @author Werner Randelshofer
 */
public class WrappedMap<K, V> extends AbstractMap<K, V> {
    protected final @NonNull Supplier<Iterator<Map.Entry<K, V>>> iteratorFunction;
    protected final @NonNull IntSupplier sizeFunction;
    protected final @NonNull Predicate<Object> containsKeyFunction;
    protected final @NonNull Runnable clearFunction;
    protected final @NonNull Function<Object, V> removeFunction;
    protected final @NonNull Function<K, V> getFunction;
    protected final @NonNull BiFunction<K, V, V> putFunction;


    public WrappedMap(@NonNull ReadOnlyMap<K, V> m) {
        this(m::iterator, m::size, m::containsKey, m::get, null, null, null);
    }

    public WrappedMap(@NonNull Map<K, V> m) {
        this(() -> m.entrySet().iterator(), m::size, m::containsKey, m::get, null, null, null);
    }

    public WrappedMap(@NonNull Supplier<Iterator<Entry<K, V>>> iteratorFunction,
                      @NonNull IntSupplier sizeFunction,
                      @NonNull Predicate<Object> containsKeyFunction,
                      @NonNull Function<K, V> getFunction,
                      @Nullable Runnable clearFunction,
                      @Nullable Function<Object, V> removeFunction,
                      @Nullable BiFunction<K, V, V> putFunction) {
        this.iteratorFunction = iteratorFunction;
        this.sizeFunction = sizeFunction;
        this.containsKeyFunction = containsKeyFunction;
        this.getFunction = getFunction;
        this.clearFunction = clearFunction == null ? () -> {
            throw new UnsupportedOperationException();
        } : clearFunction;
        this.removeFunction = removeFunction == null ? o -> {
            throw new UnsupportedOperationException();
        } : removeFunction;
        this.putFunction = putFunction == null ? (k, v) -> {
            throw new UnsupportedOperationException();
        } : putFunction;
    }

    @Override
    public V get(Object key) {
        @SuppressWarnings("unchecked") K unchecked = (K) key;
        return getFunction.apply(unchecked);
    }

    @Override
    public boolean containsKey(Object key) {
        return containsKeyFunction.test(key);
    }

    @Override
    public int size() {
        return sizeFunction.getAsInt();
    }

    public boolean containsEntry(final @Nullable Object o) {
        if (o instanceof Entry) {
            @SuppressWarnings("unchecked") Entry<K, V> entry = (Entry<K, V>) o;
            K key = entry.getKey();
            return containsKey(key) && Objects.equals(entry.getValue(), get(key));
        }
        return false;
    }

    boolean removeEntry(final @Nullable Object o) {
        if (containsEntry(o)) {
            assert o != null;
            @SuppressWarnings("unchecked") Entry<K, V> entry = (Entry<K, V>) o;
            remove(entry.getKey());
            return true;
        }
        return false;
    }

    @Override
    public @NonNull Set<Entry<K, V>> entrySet() {
        return new WrappedSet<Entry<K, V>>(
                iteratorFunction,
                sizeFunction,
                this::containsEntry,
                clearFunction,
                this::removeEntry
        );
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        return super.getOrDefault(key, defaultValue);
    }

    @Override
    public V remove(Object key) {
        return removeFunction.apply(key);
    }
}
