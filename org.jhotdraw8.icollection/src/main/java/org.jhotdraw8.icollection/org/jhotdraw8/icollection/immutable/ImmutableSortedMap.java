package org.jhotdraw8.icollection.immutable;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
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
    @NonNull ImmutableSortedMap<K, V> clear();

    @Override
    @NonNull ImmutableSortedMap<K, V> put(@NonNull K key, @Nullable V value);

    @Override
    @NonNull
    default ImmutableSortedMap<K, V> putAll(@NonNull Map<? extends K, ? extends V> m) {
        return (ImmutableSortedMap<K, V>) ImmutableMap.super.putAll(m);
    }

    @Override
    @NonNull
    default ImmutableSortedMap<K, V> putAll(@NonNull Iterable<? extends Map.Entry<? extends K, ? extends V>> c) {
        return (ImmutableSortedMap<K, V>) ImmutableMap.super.putAll(c);
    }

    @Override
    @NonNull
    default ImmutableSortedMap<K, V> putKeyValues(@NonNull Object @NonNull ... kv) {
        return (ImmutableSortedMap<K, V>) ImmutableMap.super.putKeyValues(kv);
    }

    @Override
    @NonNull ImmutableSortedMap<K, V> remove(@NonNull K key);

    @Override
    @NonNull
    default ImmutableSortedMap<K, V> removeAll(@NonNull Iterable<? extends K> c) {
        return (ImmutableSortedMap<K, V>) ImmutableMap.super.removeAll(c);
    }

    @Override
    @NonNull
    default ImmutableSortedMap<K, V> retainAll(@NonNull Iterable<? extends K> c) {
        return (ImmutableSortedMap<K, V>) ImmutableMap.super.retainAll(c);
    }

    @Override
    @NonNull
    default ImmutableSortedMap<K, V> retainAll(@NonNull ReadOnlyCollection<? extends K> c) {
        return (ImmutableSortedMap<K, V>) ImmutableMap.super.retainAll(c);
    }
}
