package org.jhotdraw8.icollection;


import org.jhotdraw8.icollection.immutable.ImmutableMap;
import org.jhotdraw8.icollection.immutable.ImmutableSequencedMap;

import java.util.Map;

/**
 * Provides factory methods for {@link ImmutableSequencedMap}s.
 */
public class ImmutableSequencedMaps {
    /**
     * Don't let anyone instantiate this class.
     */
    private ImmutableSequencedMaps() {
    }

    /**
     * Returns an empty immutable sequenced map.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @return empty map
     */
    public static <K, V> ImmutableSequencedMap<K, V> of() {
        return SimpleImmutableSequencedMap.of();
    }

    /**
     * Returns an immutable sequenced map with the specified entries.
     * <p>
     * If the provided iterable can be cast to {@link ImmutableMap},
     * it will be cast, otherwise a new map will be created.
     *
     * @param entries the specified entries
     * @param <K>     the key type
     * @param <V>     the value type
     * @return immutable sequenced map of the specified entries
     */
    @SuppressWarnings("unchecked")
    public static <K, V> ImmutableSequencedMap<K, V> of(Iterable<? extends Map.Entry<K, V>> entries) {
        return (entries instanceof ImmutableSequencedMap<?, ?>)
                ? (ImmutableSequencedMap<K, V>) entries
                : SimpleImmutableSequencedMap.<K, V>of().putAll(entries);
    }

    /**
     * Returns an immutable sequenced map with the specified entries.
     *
     * @param entries the specified entries
     * @param <K>     the key type
     * @param <V>     the value type
     * @return immutable sequenced map of the specified entries
     */
    public static <K, V> ImmutableSequencedMap<K, V> copyOf(Map<K, V> entries) {
        return SimpleImmutableSequencedMap.<K, V>of().putAll(entries);
    }
}
