/*
 * @(#)MapFacade.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.icollection.facade;

import org.jhotdraw8.icollection.readable.ReadableMap;
import org.jspecify.annotations.Nullable;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Provides a {@link Map} facade to a set of {@code Map} functions.
 *
 * @param <K> the key type
 * @param <V> the value type
 * @author Werner Randelshofer
 */
public class MapFacade<K, V> extends AbstractMap<K, V> {
    protected final Supplier<Iterator<Map.Entry<K, V>>> iteratorFunction;
    protected final Supplier<Spliterator<Entry<K, V>>> spliteratorFunction;
    protected final IntSupplier sizeFunction;
    protected final Predicate<Object> containsKeyFunction;
    protected final Runnable clearFunction;
    protected final Function<Object, V> removeFunction;
    protected final Function<K, V> getFunction;
    protected final BiFunction<K, V, V> putFunction;


    public MapFacade(ReadableMap<K, V> m) {
        this(m::iterator, m::spliterator, m::size, m::containsKey, m::get, null, null, null);
    }

    public MapFacade(Map<K, V> m) {
        this(() -> m.entrySet().iterator(), () -> m.entrySet().spliterator(), m::size, m::containsKey, m::get, m::clear,
                m::remove, m::put);
    }

    public MapFacade(Supplier<Iterator<Entry<K, V>>> iteratorFunction,
                     Supplier<Spliterator<Entry<K, V>>> spliteratorFunction, IntSupplier sizeFunction,
                     Predicate<Object> containsKeyFunction,
                     Function<K, V> getFunction,
                     @Nullable Runnable clearFunction,
                     @Nullable Function<Object, V> removeFunction,
                     @Nullable BiFunction<K, V, V> putFunction) {
        this.iteratorFunction = iteratorFunction;
        this.spliteratorFunction = spliteratorFunction;
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
    public Set<Entry<K, V>> entrySet() {
        return new SetFacade<>(
                iteratorFunction,
                spliteratorFunction, sizeFunction,
                this::containsEntry,
                clearFunction,
                null,
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

    @Override
    public @Nullable V put(K key, V value) {
        return putFunction.apply(key, value);
    }
}
