/*
 * @(#)PersistentSequencedMap.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.persistent;

import org.jhotdraw8.icollection.readable.ReadableSequencedMap;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.NoSuchElementException;

/**
 * An interface to an persistent map with a well-defined iteration order; the
 * implementation guarantees that the state of the collection does not change.
 * <p>
 * An interface to an persistent sequenced map provides methods for creating a new persistent sequenced map with
 * added, updated or removed entries, without changing the original persistent sequenced map.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public interface PersistentSequencedMap<K, V> extends PersistentMap<K, V>, ReadableSequencedMap<K, V> {
    @Override
    PersistentSequencedMap<K, V> clear();

    @Override
    PersistentSequencedMap<K, V> put(K key, @Nullable V value);

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
    PersistentSequencedMap<K, V> putFirst(K key, @Nullable V value);

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
    PersistentSequencedMap<K, V> putLast(K key, @Nullable V value);


    @Override
    PersistentSequencedMap<K, V> putAll(Iterable<? extends Map.Entry<? extends K, ? extends V>> c);

    @Override
    default PersistentSequencedMap<K, V> putKeyValues(Object... kv) {
        return (PersistentSequencedMap<K, V>) PersistentMap.super.putKeyValues(kv);
    }

    @Override
    PersistentSequencedMap<K, V> remove(K key);

    @Override
    PersistentSequencedMap<K, V> removeAll(Iterable<? extends K> c);

    /**
     * Returns a copy of this map that contains all entries
     * of this map except the first.
     *
     * @return a new map instance with the first element removed
     * @throws NoSuchElementException if this map is empty
     */
    default PersistentSequencedMap<K, V> removeFirst() {
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
    default PersistentSequencedMap<K, V> removeLast() {
        Map.Entry<K, V> e = lastEntry();
        return e == null ? this : remove(e.getKey());
    }

    @Override
    PersistentSequencedMap<K, V> retainAll(Iterable<? extends K> c);

    @Override
    Map<K, V> toMutable();

    /**
     * Returns a reversed copy of this map.
     * <p>
     * This operation may be implemented in O(N).
     * <p>
     * Use {@link #readOnlyReversed()} if you only
     * need to iterate in the reversed sequence over this set.
     *
     * @return a reversed copy of this set.
     */
    default PersistentSequencedMap<K, V> reverse() {
        if (size() < 2) {
            return this;
        }
        return clear().putAll(readOnlyReversed());
    }
}
