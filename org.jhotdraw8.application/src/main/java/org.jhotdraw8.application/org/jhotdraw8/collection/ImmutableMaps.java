/*
 * @(#)ImmutableMaps.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Provides factory methods for immutable maps.
 */
public class ImmutableMaps {

    /**
     * Don't let anyone instantiate this class.
     */
    private ImmutableMaps() {
    }

    @SafeVarargs
    public static @NonNull <K, V> ImmutableMap<K, V> ofEntries(Map.Entry<K, V>... entries) {
        @SuppressWarnings("varargs")
        ImmutableHashMap<K, V> result = new ImmutableHashMap<>(Arrays.asList(entries));
        return result;
    }

    @SuppressWarnings("unchecked")
    public static @NonNull <K, V> ImmutableMap<K, V> copyOf(ReadOnlyMap<? extends K, ? extends V> map) {
        return map instanceof ImmutableMap<?, ?> ? (ImmutableMap<K, V>) map : new ImmutableHashMap<>(map);
    }

    public static @NonNull <K, V> ImmutableMap<K, V> of(@NonNull ReadOnlyCollection<Map.Entry<? extends K, ? extends V>> entrySet) {
        return new ImmutableHashMap<>(entrySet);
    }

    public static @NonNull <K, V> ImmutableMap<V, K> inverseOf(@NonNull Set<Map.Entry<K, V>> entrySet) {
        return new ImmutableHashMap<>(new MappedSet<Map.Entry<V, K>, Map.Entry<K, V>>(entrySet, e -> entry(e.getValue(), e.getKey())));
    }

    public static @NonNull <K, V> ImmutableMap<K, V> of(@NonNull Collection<Map.Entry<K, V>> entrySet) {
        return new ImmutableHashMap<>(entrySet);
    }

    public static @NonNull <K, V> ImmutableMap<K, V> copyOf(@NonNull Map<? extends K, ? extends V> map) {
        return new ImmutableHashMap<>(map);
    }

    public static @NonNull <K, V> ImmutableMap<K, V> of() {
        @SuppressWarnings("unchecked")
        ImmutableMap<K, V> map = (ImmutableMap<K, V>) ImmutableHashMap.EMPTY_MAP;
        return map;
    }

    public static @NonNull <K, V> ImmutableMap<K, V> of(K k1, V v1) {
        return new ImmutableHashMap<>(k1, v1);
    }

    public static @NonNull <K, V> ImmutableMap<K, V> of(K k1, V v1, K k2, V v2) {
        return new ImmutableHashMap<>(k1, v1, k2, v2);
    }

    public static @NonNull <K, V> ImmutableMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3) {
        return new ImmutableHashMap<>(k1, v1, k2, v2, k3, v3);
    }

    public static @NonNull <K, V> ImmutableMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        return new ImmutableHashMap<>(k1, v1, k2, v2, k3, v3, k4, v4);
    }

    public static @NonNull <K, V> Map.Entry<K, V> entry(K k, V v) {
        return new AbstractMap.SimpleImmutableEntry<>(k, v);
    }
}
