package org.jhotdraw8.icollection;


import org.jhotdraw8.icollection.facade.ReadOnlySequencedMapFacade;
import org.jhotdraw8.icollection.immutable.ImmutableSequencedMap;
import org.jhotdraw8.icollection.readonly.ReadOnlySequencedMap;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SequencedMap;

/**
 * Provides factory methods for {@link ReadOnlySequencedMap}s.
 */
public class ReadOnlySequencedMaps {
    /**
     * Don't let anyone instantiate this class.
     */
    private ReadOnlySequencedMaps() {
    }

    /**
     * Returns an empty read-only sequenced map.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @return empty read-only sequenced map
     */
    public static <K, V> ReadOnlySequencedMap<K, V> of() {
        return SimpleImmutableSequencedMap.of();
    }

    /**
     * Returns a new read-only sequenced map with the specified entries.
     * <p>
     * If the provided iterable can be cast to {@link ImmutableSequencedMap},
     * it will be cast, otherwise a new map will be created.
     *
     * @param entries the specified entries
     * @param <K>     the key type
     * @param <V>     the value type
     * @return read-only sequenced map with the specified entries
     */
    @SuppressWarnings("unchecked")
    public static <K, V> ReadOnlySequencedMap<K, V> of(Iterable<? extends Map.Entry<K, V>> entries) {
        if (entries instanceof ImmutableSequencedMap<?, ?>) return (ImmutableSequencedMap<K, V>) entries;
        LinkedHashMap<K, V> m = new LinkedHashMap<>();
        for (var e : entries) {
            m.put(e.getKey(), e.getValue());
        }
        return new ReadOnlySequencedMapFacade<>(m);
    }

    /**
     * Returns a read-only sequenced map with the specified entries.
     * <p>
     * If the map can be cast to {@link ImmutableSequencedMap},
     * it will be cast, otherwise a new map will be created.
     *
     * @param entries the specified entries
     * @param <K>     the key type
     * @param <V>     the value type
     * @return read-only sequenced map with the specified entries
     */
    public static <K, V> ReadOnlySequencedMap<K, V> copyOf(Map<K, V> entries) {
        if (entries instanceof ImmutableSequencedMap<?, ?>) return (ReadOnlySequencedMap<K, V>) entries;
        LinkedHashMap<K, V> m = new LinkedHashMap<>(entries);
        return new ReadOnlySequencedMapFacade<>(m);
    }

    /**
     * Returns the same map wrapped or cast into a read-only sequenced map interface.
     * <p>
     * If the map can be cast to a {@link ReadOnlySequencedMap}, it will be cast.
     *
     * @param entries the specified entries
     * @param <K>     the key type
     * @param <V>     the value type
     * @return a  list
     */
    @SuppressWarnings("unchecked")
    public static <K, V> ReadOnlySequencedMap<K, V> asReadOnly(SequencedMap<K, V> entries) {
        return entries instanceof ReadOnlySequencedMap<?, ?> ? (ReadOnlySequencedMap<K, V>) entries : new ReadOnlySequencedMapFacade<>(entries);
    }
}
