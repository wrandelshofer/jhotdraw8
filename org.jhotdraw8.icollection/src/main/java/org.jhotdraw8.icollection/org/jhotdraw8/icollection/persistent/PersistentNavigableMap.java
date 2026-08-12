package org.jhotdraw8.icollection.persistent;

import org.jhotdraw8.icollection.readable.ReadableCollection;
import org.jhotdraw8.icollection.readable.ReadableNavigableMap;
import org.jspecify.annotations.Nullable;

import java.util.Map;

/// This interface provides copy-returning operations for a navigable map.
///
/// A navigable map is an ordered group of entries.
/// An entry maps a key to a value.
/// The elements are ordered by height from a floor entry to a ceiling entry.
/// The interface allows to navigate from an entry to a higher or a lower entry.
///
/// A copy-returning operation returns a new copy of the map
/// with changes applied to it. The operation does not change the original
/// map.
///
/// @param <K> the key type
/// @param <V> the value type
public interface PersistentNavigableMap<K, V> extends ReadableNavigableMap<K, V>, PersistentSortedMap<K, V> {
    @Override
    PersistentNavigableMap<K, V> cleared();

    @Override
    PersistentNavigableMap<K, V> putting(K key, @Nullable V value);

    @Override
    default PersistentNavigableMap<K, V> puttingAll(Map<? extends K, ? extends V> m) {
        return (PersistentNavigableMap<K, V>) PersistentSortedMap.super.puttingAll(m);
    }

    @Override
    default PersistentNavigableMap<K, V> puttingAll(Iterable<? extends Map.Entry<? extends K, ? extends V>> c) {
        return (PersistentNavigableMap<K, V>) PersistentSortedMap.super.puttingAll(c);
    }

    @Override
    default PersistentNavigableMap<K, V> puttingKeyValues(Object... kv) {
        return (PersistentNavigableMap<K, V>) PersistentSortedMap.super.puttingKeyValues(kv);
    }

    @Override
    PersistentNavigableMap<K, V> removing(K key);

    @Override
    default PersistentNavigableMap<K, V> removingAll(Iterable<? extends K> c) {
        return (PersistentNavigableMap<K, V>) PersistentSortedMap.super.removingAll(c);
    }

    @Override
    default PersistentNavigableMap<K, V> retainingAll(Iterable<? extends K> c) {
        return (PersistentNavigableMap<K, V>) PersistentSortedMap.super.retainingAll(c);
    }

    @Override
    default PersistentNavigableMap<K, V> retainingAll(ReadableCollection<? extends K> c) {
        return (PersistentNavigableMap<K, V>) PersistentSortedMap.super.retainingAll(c);
    }
}
