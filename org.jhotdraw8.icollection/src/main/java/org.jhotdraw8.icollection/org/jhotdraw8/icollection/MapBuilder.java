package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.readable.ReadableMap;

import java.util.Iterator;
import java.util.Map;
import java.util.Spliterator;

/// Builder for readable maps.
public interface MapBuilder<K, V, M extends ReadableMap<K, V>> {
    /// Adds the specified entry.
    ///
    /// @throws IllegalStateException if the map already contains an entry with the same key
    MapBuilder<K, V, M> add(K key, V value);

    /// Adds the specified entry.
    ///
    /// @throws IllegalStateException if the map already contains an entry with the same key
    default MapBuilder<K, V, M> add(Map.Entry<? extends K, ? extends V> entry) {
        add(entry.getKey(), entry.getValue());
        return this;
    }

    /// Adds all specified entries.
    ///
    /// @throws IllegalStateException if the map already contains an entry with the same key
    default MapBuilder<K, V, M> addMap(Map<? extends K, ? extends V> map) {
        for (Map.Entry<? extends K, ? extends V> e : map.entrySet()) {
            add(e.getKey(), e.getValue());
        }
        return this;
    }

    /// Adds all specified entries.
    ///
    /// @throws IllegalStateException if the map already contains an entry with the same key
    default MapBuilder<K, V, M> addReadableMap(ReadableMap<? extends K, ? extends V> map) {
        for (Map.Entry<? extends K, ? extends V> e : map.readableEntrySet()) {
            add(e.getKey(), e.getValue());
        }
        return this;
    }

    /// Adds all remaining elements in the specified iterator.
    default MapBuilder<K, V, M> addEntries(Iterator<? extends Map.Entry<? extends K, ? extends V>> it) {
        while (it.hasNext()) {
            Map.Entry<? extends K, ? extends V> next = it.next();
            add(next.getKey(), next.getValue());
        }
        return this;
    }

    /// Adds all remaining elements in the specified spliterator.
    default MapBuilder<K, V, M> addEntries(Spliterator<? extends Map.Entry<? extends K, ? extends V>> it) {
        while (it.tryAdvance(this::add)) {
        }
        return this;
    }

    /// Adds all specified entries.
    ///
    /// @throws IllegalStateException if the map already contains an entry with the same key
    default MapBuilder<K, V, M> addEntries(Iterable<? extends Map.Entry<? extends K, ? extends V>> items) {
        for (Map.Entry<? extends K, ? extends V> e : items) {
            add(e.getKey(), e.getValue());
        }
        return this;
    }

    /// Builds the map and returns it.
    M build();
}
