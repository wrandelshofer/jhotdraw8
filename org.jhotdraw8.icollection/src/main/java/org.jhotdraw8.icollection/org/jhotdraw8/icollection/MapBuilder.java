package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.readable.ReadableMap;

import java.util.Iterator;
import java.util.Map;
import java.util.Spliterator;

/// Builder for readable maps.
public interface MapBuilder<K, V, M extends ReadableMap<K, V>> {
    /// Adds the specified entry.
    MapBuilder<K, V, M> put(K key, V value);

    /// Puts the specified entry.
    default MapBuilder<K, V, M> putEntry(Map.Entry<? extends K, ? extends V> entry) {
        put(entry.getKey(), entry.getValue());
        return this;
    }

    /// Puts all entries from the specified map.
    default MapBuilder<K, V, M> putAll(Map<? extends K, ? extends V> map) {
        for (Map.Entry<? extends K, ? extends V> e : map.entrySet()) {
            put(e.getKey(), e.getValue());
        }
        return this;
    }

    /// Puts all remaining entries from the specified iterator.
    default MapBuilder<K, V, M> putEntries(Iterator<? extends Map.Entry<? extends K, ? extends V>> it) {
        while (it.hasNext()) {
            Map.Entry<? extends K, ? extends V> next = it.next();
            put(next.getKey(), next.getValue());
        }
        return this;
    }

    /// Puts all remaining entries from the specified spliterator.
    default MapBuilder<K, V, M> putEntries(Spliterator<? extends Map.Entry<? extends K, ? extends V>> it) {
        while (it.tryAdvance(this::putEntry)) {
        }
        return this;
    }

    /// Puts all entries from the specified iterable.
    default MapBuilder<K, V, M> putEntries(java.lang.Iterable<? extends Map.Entry<? extends K, ? extends V>> items) {
        for (Map.Entry<? extends K, ? extends V> e : items) {
            put(e.getKey(), e.getValue());
        }
        return this;
    }

    /// Builds the map and returns it.
    M build();
}
