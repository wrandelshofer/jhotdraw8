/*
 * @(#)Maps.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Static utility-methods for maps.
 */
public class Maps {
    /**
     * Don't let anyone instantiate this class.
     */
    private Maps() {
    }

    /**
     * Adds the specified entries to the map.
     *
     * @param map     the map
     * @param entries the entries
     * @param <K>     the key type
     * @param <V>     the value type
     * @return the map
     */
    @SafeVarargs
    public static <K, V> @NonNull Map<K, V> putAll(@NonNull Map<K, V> map, Map.@NonNull Entry<? extends K, ? extends V>... entries) {
        for (Map.Entry<? extends K, ? extends V> entry : entries) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

    /**
     * Adds the specified entries to the map.
     *
     * @param map     the map
     * @param entries the entries
     * @param <K>     the key type
     * @param <V>     the value type
     * @return the map
     */
    public static <K, V> @NonNull Map<K, V> putAll(@NonNull Map<K, V> map, @NonNull ReadOnlyMap<? extends K, ? extends V> entries) {
        for (@NonNull Iterator<? extends Map.Entry<? extends K, ? extends V>> it = entries.iterator(); it.hasNext(); ) {
            Map.Entry<? extends K, ? extends V> entry = it.next();
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

    /**
     * Adds the specified entries to the map.
     *
     * @param map     the map
     * @param entries the entries
     * @param <K>     the key type
     * @param <V>     the value type
     * @return the map
     */
    public static <K, V> @NonNull Map<K, V> putAll(@NonNull Map<K, V> map, @NonNull Iterable<? extends Map.Entry<? extends K, ? extends V>> entries) {
        for (Map.Entry<? extends K, ? extends V> entry : entries) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

    /**
     * Adds the specified entries to the map.
     *
     * @param map     the map
     * @param entries the entries
     * @param <K>     the key type
     * @param <V>     the value type
     * @return the map
     */
    @SafeVarargs
    public static @NonNull <K, V> Map<K, V> addAllEntries(@NonNull Map<K, V> map, @NonNull Map.Entry<K, V>... entries) {
        for (Map.Entry<? extends K, ? extends V> entry : entries) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

    public static @NonNull <K, V> Map.Entry<K, V> entry(K k, V v) {
        return new AbstractMap.SimpleImmutableEntry<>(k, v);
    }

    public static @NonNull <K, V> Map<K, V> putAll(@NonNull Map<K, V> map, K k1, V v1) {
        map.put(k1, v1);
        return map;
    }

    public static @NonNull <K, V> Map<K, V> putAll(@NonNull Map<K, V> map, K k1, V v1, K k2, V v2) {
        map.put(k1, v1);
        map.put(k2, v2);
        return map;
    }

    public static @NonNull <K, V> Map<K, V> putAll(@NonNull Map<K, V> map, K k1, V v1, K k2, V v2, K k3, V v3) {
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        return map;
    }

    public static @NonNull <K, V> Map<K, V> putAll(@NonNull Map<K, V> map, K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        map.put(k4, v4);
        return map;
    }

    @SuppressWarnings("unchecked")
    public static @NonNull <K, V> Map<K, V> putAll(@NonNull Map<K, V> map, K k, V v, Object... kv) {
        map.put(k, v);
        for (int i = 0; i < kv.length; i += 2) {
            map.put((K) kv[i], (V) kv[i + 1]);
        }
        return map;
    }

}
