/*
 * @(#)ImmutableSequencedMap.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.immutable;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.readonly.ReadOnlyCollection;
import org.jhotdraw8.collection.readonly.ReadOnlyMap;
import org.jhotdraw8.collection.readonly.ReadOnlySequencedMap;

import java.util.Collection;
import java.util.Map;

/**
 * Interface for an immutable sequenced map.
 * <p>
 * An immutable sequenced map provides methods for creating a new immutable sequenced map with
 * added, updated or removed entries, without changing the original immutable sequenced map.
 */
public interface ImmutableSequencedMap<K, V> extends ImmutableMap<K, V>, ReadOnlySequencedMap<K, V> {
    @Override
    @NonNull ImmutableSequencedMap<K, V> clear();

    @Override
    @NonNull ImmutableSequencedMap<K, V> put(@NonNull K key, @Nullable V value);

    @Override
    @NonNull
    default ImmutableSequencedMap<K, V> putAll(@NonNull Map<? extends K, ? extends V> m) {
        return (ImmutableSequencedMap<K, V>) ImmutableMap.super.putAll(m);
    }

    @Override
    @NonNull ImmutableSequencedMap<K, V> putAll(@NonNull ImmutableMap<? extends K, ? extends V> m);

    @Override
    @NonNull ImmutableSequencedMap<K, V> putAll(@NonNull Iterable<? extends Map.Entry<? extends K, ? extends V>> m);

    @Override
    @NonNull
    default ImmutableSequencedMap<K, V> putAll(@NonNull ReadOnlyMap<? extends K, ? extends V> map) {
        return (ImmutableSequencedMap<K, V>) ImmutableMap.super.putAll(map);
    }

    @Override
    @NonNull
    default ImmutableSequencedMap<K, V> putKeyValues(@NonNull Object @NonNull ... kv) {
        return (ImmutableSequencedMap<K, V>) ImmutableMap.super.putKeyValues(kv);
    }

    @Override
    @NonNull ImmutableSequencedMap<K, V> remove(@NonNull K key);

    @Override
    @NonNull ImmutableSequencedMap<K, V> removeAll(@NonNull Iterable<? extends K> c);

    /**
     * Returns a copy of this map that contains all entries
     * of this map except the first.
     *
     * @return this map instance if it is already empty, or
     * a different map instance with the first entry removed
     */
    default @NonNull ImmutableSequencedMap<K, V> removeFirst() {
        Map.Entry<K, V> e = firstEntry();
        return e == null ? this : remove(e.getKey());
    }

    /**
     * Returns a copy of this map that contains all entries
     * of this map except the last.
     *
     * @return this map instance if it is already empty, or
     * a different map instance with the last entry removed
     */
    default @NonNull ImmutableSequencedMap<K, V> removeLast() {
        Map.Entry<K, V> e = lastEntry();
        return e == null ? this : remove(e.getKey());
    }

    @Override
    @NonNull ImmutableSequencedMap<K, V> retainAll(@NonNull Collection<? extends K> c);

    @Override
    @NonNull
    default ImmutableSequencedMap<K, V> retainAll(@NonNull ReadOnlyCollection<? extends K> c) {
        return (ImmutableSequencedMap<K, V>) ImmutableMap.super.retainAll(c);
    }

    @Override
    @NonNull Map<K, V> toMutable();
}
