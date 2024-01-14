package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.facade.ReadOnlyMapFacade;
import org.jhotdraw8.icollection.immutable.ImmutableMap;
import org.jhotdraw8.icollection.readonly.ReadOnlyMap;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides factory methods for {@link ReadOnlyMap}s.
 */
public class ReadOnlyMaps {
    /**
     * Don't let anyone instantiate this class.
     */
    private ReadOnlyMaps() {
    }

    /**
     * Returns an empty read-only map.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @return empty read-only map
     */
    public static <K, V> ReadOnlyMap<K, V> of() {
        return SimpleImmutableMap.of();
    }

    /**
     * Returns a new read-only map with the specified entries.
     * <p>
     * If the provided iterable can be cast to {@link ImmutableMap},
     * it will be cast, otherwise a new map will be created.
     *
     * @param entries the specified entries
     * @param <K>     the key type
     * @param <V>     the value type
     * @return read-only map with the specified entries
     */
    @SuppressWarnings("unchecked")
    public static <K, V> ReadOnlyMap<K, V> of(Iterable<? extends Map.Entry<K, V>> entries) {
        if (entries instanceof ImmutableMap<?, ?>) return (ImmutableMap<K, V>) entries;
        HashMap<K, V> m = new HashMap<>();
        for (var e : entries) {
            m.put(e.getKey(), e.getValue());
        }
        return new ReadOnlyMapFacade<>(m);
    }

    /**
     * Returns a read-only map with the specified entries.
     * <p>
     * If the map can be cast to {@link org.jhotdraw8.icollection.immutable.ImmutableMap},
     * it will be cast, otherwise a new map will be created.
     *
     * @param entries the specified entries
     * @param <K>     the key type
     * @param <V>     the value type
     * @return read-only map with the specified entries
     */
    public static <K, V> ReadOnlyMap<K, V> copyOf(Map<K, V> entries) {
        if (entries instanceof ImmutableMap<?, ?>) return (ReadOnlyMap<K, V>) entries;
        HashMap<K, V> m = new HashMap<>(entries);
        return new ReadOnlyMapFacade<>(m);
    }

    /**
     * Returns the same map wrapped or cast into a read-only map interface.
     * <p>
     * If the map can be cast to a {@link ReadOnlyMap}, it will be cast.
     *
     * @param entries the specified entries
     * @param <K>     the key type
     * @param <V>     the value type
     * @return read-only map with the specified entries
     */
    @SuppressWarnings("unchecked")
    public static <K, V> ReadOnlyMap<K, V> asReadOnly(Map<K, V> entries) {
        return entries instanceof ReadOnlyMap<?, ?> ? (ReadOnlyMap<K, V>) entries : new ReadOnlyMapFacade<>(entries);
    }
}
