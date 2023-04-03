/*
 * @(#)ImmutableSequencedMap.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.immutable;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.readonly.ReadOnlyCollection;
import org.jhotdraw8.collection.readonly.ReadOnlySequencedMap;

import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Interface for an immutable sequenced map.
 * <p>
 * An immutable sequenced map provides methods for creating a new immutable sequenced map with
 * added, updated or removed entries, without changing the original immutable sequenced map.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public interface ImmutableSequencedMap<K, V> extends ImmutableMap<K, V>, ReadOnlySequencedMap<K, V> {
    @Override
    @NonNull ImmutableSequencedMap<K, V> clear();

    @Override
    @NonNull ImmutableSequencedMap<K, V> put(@NonNull K key, @Nullable V value);

    /**
     * Creates an entry for the specified key and value and adds it to the front
     * of the map if an entry for the specified key is not already present.
     * If this map already contains an entry for the specified key, replaces the
     * value and moves the entry to the front.
     *
     * @param key   the key
     * @param value the value
     * @return this map instance if no changes are needed, or a different map
     * instance with the applied changes.
     */
    @NonNull ImmutableSequencedMap<K, V> putFirst(@NonNull K key, @Nullable V value);

    /**
     * Creates an entry for the specified key and value and adds it to the end
     * of the map if an entry for the specified key is not already present.
     * If this map already contains an entry for the specified key, replaces the
     * value and moves the entry to the end.
     *
     * @param key   the key
     * @param value the value
     * @return this map instance if no changes are needed, or a different map
     * instance with the applied changes.
     */
    @NonNull ImmutableSequencedMap<K, V> putLast(@NonNull K key, @Nullable V value);


    @Override
    @NonNull ImmutableSequencedMap<K, V> putAll(@NonNull Iterable<? extends Map.Entry<? extends K, ? extends V>> m);

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
     * @return a new map instance with the first element removed
     * @throws NoSuchElementException if this map is empty
     */
    default @NonNull ImmutableSequencedMap<K, V> removeFirst() {
        Map.Entry<K, V> e = firstEntry();
        return e == null ? this : remove(e.getKey());
    }

    /**
     * Returns a copy of this map that contains all entries
     * of this map except the last.
     *
     * @return a new map instance with the last element removed
     * @throws NoSuchElementException if this set is empty
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
