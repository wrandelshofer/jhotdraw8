package org.jhotdraw8.icollection;


import org.jhotdraw8.icollection.immutable.ImmutableMap;

import java.util.Map;

/**
 * Provides factory methods for {@link ImmutableMap}s.
 */
public class ImmutableMaps {
    /**
     * Don't let anyone instantiate this class.
     */
    private ImmutableMaps() {
    }

    /**
     * Returns an empty immutable map.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @return empty map
     */
    public static <K, V> ImmutableMap<K, V> of() {
        return ChampMap.of();
    }

    /**
     * Returns a new immutable map with the specified entries.
     * <p>
     * If the provided iterable can be cast to {@link ImmutableMap},
     * it will be cast, otherwise a new map will be created.
     *
     * @param entries the specified entries
     * @param <K>     the key type
     * @param <V>     the value type
     * @return immutable map of the specified entries
     */
    @SuppressWarnings("unchecked")
    public static <K, V> ImmutableMap<K, V> of(Iterable<? extends Map.Entry<K, V>> entries) {
        return (entries instanceof ImmutableMap<?, ?>) ? (ImmutableMap<K, V>) entries : ChampMap.<K, V>of().putAll(entries);
    }

    /**
     * Returns an immutable map with the specified entries.
     *
     * @param entries the specified entries
     * @param <K>     the key type
     * @param <V>     the value type
     * @return immutable map of the specified entries
     */
    public static <K, V> ImmutableMap<K, V> copyOf(Map<K, V> entries) {
        return ChampMap.<K, V>of().putAll(entries);
    }
}
