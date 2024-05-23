package org.jhotdraw8.icollection.immutable;

import org.jhotdraw8.icollection.readonly.ReadOnlyCollection;
import org.jhotdraw8.icollection.readonly.ReadOnlyNavigableMap;
import org.jspecify.annotations.Nullable;

import java.util.Map;

/**
 * An interface to an immutable navigable map; the implementation guarantees that the state of the collection does not change.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public interface ImmutableNavigableMap<K, V> extends ReadOnlyNavigableMap<K, V>, ImmutableSortedMap<K, V> {
    @Override
    ImmutableNavigableMap<K, V> clear();

    @Override
    ImmutableNavigableMap<K, V> put(K key, @Nullable V value);

    @Override
    default ImmutableNavigableMap<K, V> putAll(Map<? extends K, ? extends V> m) {
        return (ImmutableNavigableMap<K, V>) ImmutableSortedMap.super.putAll(m);
    }

    @Override
    default ImmutableNavigableMap<K, V> putAll(Iterable<? extends Map.Entry<? extends K, ? extends V>> c) {
        return (ImmutableNavigableMap<K, V>) ImmutableSortedMap.super.putAll(c);
    }

    @Override
    default ImmutableNavigableMap<K, V> putKeyValues(Object... kv) {
        return (ImmutableNavigableMap<K, V>) ImmutableSortedMap.super.putKeyValues(kv);
    }

    @Override
    ImmutableNavigableMap<K, V> remove(K key);

    @Override
    default ImmutableNavigableMap<K, V> removeAll(Iterable<? extends K> c) {
        return (ImmutableNavigableMap<K, V>) ImmutableSortedMap.super.removeAll(c);
    }

    @Override
    default ImmutableNavigableMap<K, V> retainAll(Iterable<? extends K> c) {
        return (ImmutableNavigableMap<K, V>) ImmutableSortedMap.super.retainAll(c);
    }

    @Override
    default ImmutableNavigableMap<K, V> retainAll(ReadOnlyCollection<? extends K> c) {
        return (ImmutableNavigableMap<K, V>) ImmutableSortedMap.super.retainAll(c);
    }
}
