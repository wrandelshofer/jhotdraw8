/*
 * @(#)ReadOnlyMapFacade.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.pcollection.impl.facade;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.pcollection.readonly.ReadOnlyMap;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Wraps {@code Map} functions in the {@link ReadOnlyMap} interface.
 *
 * @param <K> the key type
 * @param <V> the value type
 * @author Werner Randelshofer
 */
public class ReadOnlyMapFacade<K, V> implements ReadOnlyMap<K, V> {
    protected final @NonNull Supplier<Iterator<Map.Entry<K, V>>> iteratorFunction;
    protected final @NonNull IntSupplier sizeFunction;
    protected final @NonNull Predicate<Object> containsKeyFunction;
    protected final @NonNull Function<K, V> getFunction;


    public ReadOnlyMapFacade(@NonNull ReadOnlyMap<K, V> m) {
        this(m::iterator, m::size, m::containsKey, m::get);
    }

    public ReadOnlyMapFacade(@NonNull Map<K, V> m) {
        this(() -> m.entrySet().iterator(), m::size, m::containsKey, m::get);
    }

    public ReadOnlyMapFacade(@NonNull Supplier<Iterator<Map.Entry<K, V>>> iteratorFunction,
                             @NonNull IntSupplier sizeFunction,
                             @NonNull Predicate<Object> containsKeyFunction,
                             @NonNull Function<K, V> getFunction) {
        this.iteratorFunction = iteratorFunction;
        this.sizeFunction = sizeFunction;
        this.containsKeyFunction = containsKeyFunction;
        this.getFunction = getFunction;
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
    public boolean isEmpty() {
        return sizeFunction.getAsInt() == 0;
    }

    @Override
    public @NonNull Iterator<Map.Entry<K, V>> iterator() {
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
