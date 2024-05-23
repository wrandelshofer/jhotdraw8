package org.jhotdraw8.icollection.immutable;

import org.jspecify.annotations.Nullable;
import org.jhotdraw8.icollection.readonly.ReadOnlyCollection;
import org.jhotdraw8.icollection.readonly.ReadOnlySortedMap;

import java.util.Map;

/**
 * An interface to an immutable sorted map; the implementation guarantees that the state of the collection does not change.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public interface ImmutableSortedMap<K, V> extends ReadOnlySortedMap<K, V>, ImmutableMap<K, V> {
    @Override
    ImmutableSortedMap<K, V> clear();

    @Override
    ImmutableSortedMap<K, V> put(K key, @Nullable V value);

    @Override
    default ImmutableSortedMap<K, V> putAll(Map<? extends K, ? extends V> m) {
        return (ImmutableSortedMap<K, V>) ImmutableMap.super.putAll(m);
    }

    @Override
    default ImmutableSortedMap<K, V> putAll(Iterable<? extends Map.Entry<? extends K, ? extends V>> c) {
        return (ImmutableSortedMap<K, V>) ImmutableMap.super.putAll(c);
    }

    @Override
    default ImmutableSortedMap<K, V> putKeyValues(Object... kv) {
        return (ImmutableSortedMap<K, V>) ImmutableMap.super.putKeyValues(kv);
    }

    @Override
    ImmutableSortedMap<K, V> remove(K key);

    @Override
    default ImmutableSortedMap<K, V> removeAll(Iterable<? extends K> c) {
        return (ImmutableSortedMap<K, V>) ImmutableMap.super.removeAll(c);
    }

    @Override
    default ImmutableSortedMap<K, V> retainAll(Iterable<? extends K> c) {
        return (ImmutableSortedMap<K, V>) ImmutableMap.super.retainAll(c);
    }

    @Override
    default ImmutableSortedMap<K, V> retainAll(ReadOnlyCollection<? extends K> c) {
        return (ImmutableSortedMap<K, V>) ImmutableMap.super.retainAll(c);
    }
}
