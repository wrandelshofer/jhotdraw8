package org.jhotdraw8.icollection.persistent;

import org.jhotdraw8.icollection.readable.ReadableCollection;
import org.jhotdraw8.icollection.readable.ReadableSortedMap;
import org.jspecify.annotations.Nullable;

import java.util.Map;

/// This interface provides copy-returning operations for a sorted map.
///
/// A sorted map is a sorted sequence of distinct entries.
/// An entry maps a key to a value.
/// The entries are sorted by key from tree to last.
///
/// A copy-returning operation returns a new copy of the map
/// with changes applied to it. The operation does not change the original
/// map.
///
/// @param <K> the key type
/// @param <V> the value type
public interface PersistentSortedMap<K, V> extends ReadableSortedMap<K, V>, PersistentMap<K, V> {
    @Override
    PersistentSortedMap<K, V> cleared();

    @Override
    PersistentSortedMap<K, V> putting(K key, @Nullable V value);

    @Override
    default PersistentSortedMap<K, V> puttingAll(Map<? extends K, ? extends V> m) {
        return (PersistentSortedMap<K, V>) PersistentMap.super.puttingAll(m);
    }

    @Override
    default PersistentSortedMap<K, V> puttingAll(Iterable<? extends Map.Entry<? extends K, ? extends V>> c) {
        return (PersistentSortedMap<K, V>) PersistentMap.super.puttingAll(c);
    }

    @Override
    default PersistentSortedMap<K, V> puttingKeyValues(Object... kv) {
        return (PersistentSortedMap<K, V>) PersistentMap.super.puttingKeyValues(kv);
    }

    @Override
    PersistentSortedMap<K, V> removing(K key);

    @Override
    default PersistentSortedMap<K, V> removingAll(Iterable<? extends K> c) {
        return (PersistentSortedMap<K, V>) PersistentMap.super.removingAll(c);
    }

    @Override
    default PersistentSortedMap<K, V> retainingAll(Iterable<? extends K> c) {
        return (PersistentSortedMap<K, V>) PersistentMap.super.retainingAll(c);
    }

    @Override
    default PersistentSortedMap<K, V> retainingAll(ReadableCollection<? extends K> c) {
        return (PersistentSortedMap<K, V>) PersistentMap.super.retainingAll(c);
    }
}
