package org.jhotdraw8.icollection.persistent;

import org.jhotdraw8.icollection.readable.ReadableCollection;
import org.jhotdraw8.icollection.readable.ReadableNavigableMap;
import org.jspecify.annotations.Nullable;

import java.util.Map;

/**
 * An interface to an persistent navigable map; the implementation guarantees that the state of the collection does not change.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public interface PersistentNavigableMap<K, V> extends ReadableNavigableMap<K, V>, PersistentSortedMap<K, V> {
    @Override
    PersistentNavigableMap<K, V> clear();

    @Override
    PersistentNavigableMap<K, V> put(K key, @Nullable V value);

    @Override
    default PersistentNavigableMap<K, V> putAll(Map<? extends K, ? extends V> m) {
        return (PersistentNavigableMap<K, V>) PersistentSortedMap.super.putAll(m);
    }

    @Override
    default PersistentNavigableMap<K, V> putAll(Iterable<? extends Map.Entry<? extends K, ? extends V>> c) {
        return (PersistentNavigableMap<K, V>) PersistentSortedMap.super.putAll(c);
    }

    @Override
    default PersistentNavigableMap<K, V> putKeyValues(Object... kv) {
        return (PersistentNavigableMap<K, V>) PersistentSortedMap.super.putKeyValues(kv);
    }

    @Override
    PersistentNavigableMap<K, V> remove(K key);

    @Override
    default PersistentNavigableMap<K, V> removeAll(Iterable<? extends K> c) {
        return (PersistentNavigableMap<K, V>) PersistentSortedMap.super.removeAll(c);
    }

    @Override
    default PersistentNavigableMap<K, V> retainAll(Iterable<? extends K> c) {
        return (PersistentNavigableMap<K, V>) PersistentSortedMap.super.retainAll(c);
    }

    @Override
    default PersistentNavigableMap<K, V> retainAll(ReadableCollection<? extends K> c) {
        return (PersistentNavigableMap<K, V>) PersistentSortedMap.super.retainAll(c);
    }
}
