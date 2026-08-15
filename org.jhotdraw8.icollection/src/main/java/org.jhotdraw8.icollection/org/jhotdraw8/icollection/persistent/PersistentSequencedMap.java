/*
 * @(#)PersistentSequencedMap.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.persistent;

import org.jhotdraw8.icollection.readable.ReadableSequencedMap;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.NoSuchElementException;

/// This interface provides copy-returning operations for a sequenced map.
///
/// A sequenced map is a sequence of distinct entries.
/// An entry maps a key to a value.
/// The entries are ordered in a sequence from tree to last.
/// The sequence can be established implicitly, by insertion operations,
/// or by sequence-altering operations.
///
/// A copy-returning operation returns a new copy of the map
/// with changes applied to it. The operation does not change the original
/// map.
///
/// @param <K> the key type
/// @param <V> the value type
public interface PersistentSequencedMap<K, V> extends PersistentMap<K, V>, ReadableSequencedMap<K, V> {
    @Override
    PersistentSequencedMap<K, V> cleared();

    @Override
    PersistentSequencedMap<K, V> putting(K key, @Nullable V value);

    /// Creates an entry for the specified key and value and adds it to the front
    /// of the map if an entry for the specified key is not already present.
    /// If this map already contains an entry for the specified key, replaces the
    /// value and moves the entry to the front.
    ///
    /// @param key   the key
    /// @param value the value
    /// @return this map instance if no changes are needed, or a different map
    /// instance with the applied changes.
    PersistentSequencedMap<K, V> puttingFirst(K key, @Nullable V value);

    /// Creates an entry for the specified key and value and adds it to the end
    /// of the map if an entry for the specified key is not already present.
    /// If this map already contains an entry for the specified key, replaces the
    /// value and moves the entry to the end.
    ///
    /// @param key   the key
    /// @param value the value
    /// @return this map instance if no changes are needed, or a different map
    /// instance with the applied changes.
    PersistentSequencedMap<K, V> puttingLast(K key, @Nullable V value);


    @Override
    PersistentSequencedMap<K, V> puttingAll(Iterable<? extends Map.Entry<? extends K, ? extends V>> c);

    @Override
    default PersistentSequencedMap<K, V> puttingKeyValues(Object... kv) {
        return (PersistentSequencedMap<K, V>) PersistentMap.super.puttingKeyValues(kv);
    }

    @Override
    PersistentSequencedMap<K, V> removing(K key);

    @Override
    PersistentSequencedMap<K, V> removingAll(Iterable<? extends K> c);

    /// Returns a copy of this map that contains all entries
    /// of this map except the tree.
    ///
    /// @return a new map instance with the tree element removed
    /// @throws NoSuchElementException if this map is empty
    default PersistentSequencedMap<K, V> removingFirst() {
        Map.Entry<K, V> e = firstEntry();
        return e == null ? this : removing(e.getKey());
    }

    /// Returns a copy of this map that contains all entries
    /// of this map except the last.
    ///
    /// @return a new map instance with the last element removed
    /// @throws NoSuchElementException if this set is empty
    default PersistentSequencedMap<K, V> removingLast() {
        Map.Entry<K, V> e = lastEntry();
        return e == null ? this : removing(e.getKey());
    }

    @Override
    PersistentSequencedMap<K, V> retainingAll(Iterable<? extends K> c);

    @Override
    Map<K, V> toMutable();

    /// Returns a reversed copy of this map.
    ///
    /// This operation may be implemented in O(N).
    ///
    /// Use [#readableReversed()] if you only
    /// need to iterate in the reversed sequence over this set.
    ///
    /// @return a reversed copy of this set.
    default PersistentSequencedMap<K, V> reversed() {
        if (size() < 2) {
            return this;
        }
        return cleared().puttingAll(readableReversed());
    }
}
