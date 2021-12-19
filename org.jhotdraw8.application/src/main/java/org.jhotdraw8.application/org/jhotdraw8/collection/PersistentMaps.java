/*
 * @(#)PersistentMaps.java
 * Copyright © 2021 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Provides factory methods for persistent maps.
 */
public class PersistentMaps {

    /**
     * Don't let anyone instantiate this class.
     */
    private PersistentMaps() {
    }

    @SafeVarargs
    public static @NonNull <K, V> PersistentMap<K, V> ofEntries(Map.Entry<K, V>... entries) {
        return PersistentTrie5Map.of(entries);
    }

    public static @NonNull <K, V> PersistentMap<K, V> copyOf(ReadOnlyMap<K, V> map) {
        return PersistentTrie5Map.of(map.readOnlyEntrySet());
    }

    public static @NonNull <K, V> PersistentMap<K, V> of(@NonNull ReadOnlyCollection<Map.Entry<K, V>> entrySet) {
        return PersistentTrie5Map.of(entrySet);
    }

    public static @NonNull <K, V> PersistentMap<V, K> inverseOf(@NonNull Set<Map.Entry<K, V>> entrySet) {
        return PersistentTrie5Map.of(new MappedSet<>(entrySet, e -> entry(e.getValue(), e.getKey())));
    }

    public static @NonNull <K, V> PersistentMap<K, V> of(@NonNull Collection<Map.Entry<K, V>> entrySet) {
        return PersistentTrie5Map.of(entrySet);
    }

    public static @NonNull <K, V> PersistentMap<K, V> copyOf(@NonNull Map<? extends K, ? extends V> map) {
        return PersistentTrie5Map.copyOf(map);
    }

    public static @NonNull <K, V> PersistentMap<K, V> of() {
        return PersistentTrie5Map.of();
    }

    @SuppressWarnings("unchecked")
    public static @NonNull <K, V> PersistentMap<K, V> of(K k1, V v1) {
        return PersistentTrie5Map.of(entry(k1, v1));
    }

    @SuppressWarnings("unchecked")
    public static @NonNull <K, V> PersistentMap<K, V> of(K k1, V v1, K k2, V v2) {
        return PersistentTrie5Map.of(entry(k1, v1), entry(k2, v2));
    }

    @SuppressWarnings("unchecked")
    public static @NonNull <K, V> PersistentMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3) {
        return PersistentTrie5Map.of(entry(k1, v1), entry(k2, v2), entry(k3, v3));
    }

    @SuppressWarnings("unchecked")
    public static @NonNull <K, V> PersistentMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        return PersistentTrie5Map.of(entry(k1, v1), entry(k2, v2), entry(k3, v3), entry(k4, v4));
    }

    public static @NonNull <K, V> Map.Entry<K, V> entry(K k, V v) {
        return new AbstractMap.SimpleImmutableEntry<>(k, v);
    }
}
