/*
 * @(#)ReadableMapFacade.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.facade;

import org.jhotdraw8.icollection.readable.ReadableMap;
import org.jspecify.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Provides a {@link ReadableMap} facade to a set of {@code ReadableMap} functions.
 *
 * @param <K> the key type
 * @param <V> the value type
 * @author Werner Randelshofer
 */
public class ReadableMapFacade<K, V> implements ReadableMap<K, V> {
    protected final Supplier<Iterator<Map.Entry<K, V>>> iteratorFunction;
    protected final IntSupplier sizeFunction;
    protected final Predicate<Object> containsKeyFunction;
    protected final Function<K, V> getFunction;


    public ReadableMapFacade(ReadableMap<K, V> m) {
        this(m::iterator, m::size, m::containsKey, m::get);
    }

    public ReadableMapFacade(Map<K, V> m) {
        this(() -> m.entrySet().iterator(), m::size, m::containsKey, m::get);
    }

    public ReadableMapFacade(Supplier<Iterator<Map.Entry<K, V>>> iteratorFunction,
                             IntSupplier sizeFunction,
                             Predicate<Object> containsKeyFunction,
                             Function<K, V> getFunction) {
        this.iteratorFunction = iteratorFunction;
        this.sizeFunction = sizeFunction;
        this.containsKeyFunction = containsKeyFunction;
        this.getFunction = getFunction;
    }

    @Override
    public @Nullable V get(Object key) {
        @SuppressWarnings("unchecked") K unchecked = (K) key;
        return getFunction.apply(unchecked);
    }

    @Override
    public boolean containsKey(Object key) {
        return containsKeyFunction.test(key);
    }

    @Override
    public boolean isEmpty() {
        return sizeFunction.getAsInt() == 0;
    }

    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
        return iteratorFunction.get();
    }

    @Override
    public int size() {
        return sizeFunction.getAsInt();
    }

    public boolean containsEntry(final @Nullable Object o) {
        if (o instanceof Map.Entry) {
            @SuppressWarnings("unchecked") Map.Entry<K, V> entry = (Map.Entry<K, V>) o;
            K key = entry.getKey();
            return containsKey(key) && Objects.equals(entry.getValue(), get(key));
        }
        return false;
    }

}
