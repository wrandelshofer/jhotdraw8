package org.jhotdraw8.icollection.persistent;

import org.jhotdraw8.icollection.readable.ReadableCollection;
import org.jhotdraw8.icollection.readable.ReadableSortedMap;
import org.jspecify.annotations.Nullable;

import java.util.Map;

/**
 * An interface to an persistent sorted map; the implementation guarantees that the state of the collection does not change.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public interface PersistentSortedMap<K, V> extends ReadableSortedMap<K, V>, PersistentMap<K, V> {
    @Override
    PersistentSortedMap<K, V> clear();

    @Override
    PersistentSortedMap<K, V> put(K key, @Nullable V value);

    @Override
    default PersistentSortedMap<K, V> putAll(Map<? extends K, ? extends V> m) {
        return (PersistentSortedMap<K, V>) PersistentMap.super.putAll(m);
    }

    @Override
    default PersistentSortedMap<K, V> putAll(Iterable<? extends Map.Entry<? extends K, ? extends V>> c) {
        return (PersistentSortedMap<K, V>) PersistentMap.super.putAll(c);
    }

    @Override
    default PersistentSortedMap<K, V> putKeyValues(Object... kv) {
        return (PersistentSortedMap<K, V>) PersistentMap.super.putKeyValues(kv);
    }

    @Override
    PersistentSortedMap<K, V> remove(K key);

    @Override
    default PersistentSortedMap<K, V> removeAll(Iterable<? extends K> c) {
        return (PersistentSortedMap<K, V>) PersistentMap.super.removeAll(c);
    }

    @Override
    default PersistentSortedMap<K, V> retainAll(Iterable<? extends K> c) {
        return (PersistentSortedMap<K, V>) PersistentMap.super.retainAll(c);
    }

    @Override
    default PersistentSortedMap<K, V> retainAll(ReadableCollection<? extends K> c) {
        return (PersistentSortedMap<K, V>) PersistentMap.super.retainAll(c);
    }
}
