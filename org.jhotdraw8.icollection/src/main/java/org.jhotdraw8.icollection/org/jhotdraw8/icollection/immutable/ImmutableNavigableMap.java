package org.jhotdraw8.icollection.immutable;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.icollection.readonly.ReadOnlyCollection;
import org.jhotdraw8.icollection.readonly.ReadOnlyNavigableMap;

import java.util.Map;

/**
 * An interface to an immutable navigable map; the implementation guarantees that the state of the collection does not change.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public interface ImmutableNavigableMap<K, V> extends ReadOnlyNavigableMap<K, V>, ImmutableSortedMap<K, V> {
    @Override
    @NonNull ImmutableNavigableMap<K, V> clear();

    @Override
    @NonNull ImmutableNavigableMap<K, V> put(@NonNull K key, @Nullable V value);

    @Override
    @NonNull
    default ImmutableNavigableMap<K, V> putAll(@NonNull Map<? extends K, ? extends V> m) {
        return (ImmutableNavigableMap<K, V>) ImmutableSortedMap.super.putAll(m);
    }

    @Override
    @NonNull
    default ImmutableNavigableMap<K, V> putAll(@NonNull Iterable<? extends Map.Entry<? extends K, ? extends V>> c) {
        return (ImmutableNavigableMap<K, V>) ImmutableSortedMap.super.putAll(c);
    }

    @Override
    @NonNull
    default ImmutableNavigableMap<K, V> putKeyValues(@NonNull Object @NonNull ... kv) {
        return (ImmutableNavigableMap<K, V>) ImmutableSortedMap.super.putKeyValues(kv);
    }

    @Override
    @NonNull ImmutableNavigableMap<K, V> remove(@NonNull K key);

    @Override
    @NonNull
    default ImmutableNavigableMap<K, V> removeAll(@NonNull Iterable<? extends K> c) {
        return (ImmutableNavigableMap<K, V>) ImmutableSortedMap.super.removeAll(c);
    }

    @Override
    @NonNull
    default ImmutableNavigableMap<K, V> retainAll(@NonNull Iterable<? extends K> c) {
        return (ImmutableNavigableMap<K, V>) ImmutableSortedMap.super.retainAll(c);
    }

    @Override
    @NonNull
    default ImmutableNavigableMap<K, V> retainAll(@NonNull ReadOnlyCollection<? extends K> c) {
        return (ImmutableNavigableMap<K, V>) ImmutableSortedMap.super.retainAll(c);
    }
}
