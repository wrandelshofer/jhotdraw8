/*
 * @(#)SequencedMap.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.sequenced;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Interface for a map with a well-defined iteration order.
 * <p>
 * References:
 * <dl>
 *     <dt>JEP draft: Sequenced Collections</dt>
 *     <dd><a href="https://openjdk.java.net/jeps/8280836">java.ne</a></dd>
 * </dl>
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public interface SequencedMap<K, V> extends Map<K, V> {

    /**
     * Gets the first entry in this map or {@code null} if this map is empty.
     *
     * @return the first entry or {@code null}
     * @throws java.util.NoSuchElementException if the map is empty
     */
    default Map.@Nullable Entry<K, V> firstEntry() {
        return isEmpty() ? null : _sequencedEntrySet().iterator().next();
    }

    /**
     * Gets the last entry in this map or {@code null} if this map is empty.
     *
     * @return the last entry or {@code null}
     * @throws java.util.NoSuchElementException if the map is empty
     */
    default Map.@Nullable Entry<K, V> lastEntry() {
        return isEmpty() ? null : _reversed()._sequencedEntrySet().iterator().next();
    }

    /**
     * Returns a reversed-order view of this map.
     * <p>
     * Modifications write through to the underlying map.
     * Changes to the underlying map are visible in the reversed view.
     *
     * @return a reversed-order view of this map
     */
    @NonNull SequencedMap<K, V> _reversed();

    /**
     * Removes and returns the first entry in this map or code {@code null}
     * if the map is empty.
     *
     * @return the removed first entry of this map or code {@code null}
     */
    default Map.Entry<K, V> pollFirstEntry() {
        Iterator<Entry<K, V>> it = _sequencedEntrySet().iterator();
        if (it.hasNext()) {
            Entry<K, V> entry = it.next();
            it.remove();
            return entry;
        } else {
            return null;
        }
    }

    /**
     * Removes and returns the first entry in this map or code {@code null}
     * if the map is empty.
     *
     * @return the removed first entry of this map or code {@code null}
     */
    default Map.Entry<K, V> pollLastEntry() {
        Iterator<Entry<K, V>> it = _reversed()._sequencedEntrySet().iterator();
        if (it.hasNext()) {
            Entry<K, V> entry = it.next();
            it.remove();
            return entry;
        } else {
            return null;
        }
    }

    /**
     * Creates an entry for the specified key and value and adds it to the front
     * of the map if an entry for the specified key is not already present.
     * If this map already contains an entry for the specified key, replaces the
     * value and moves the entry to the front.
     *
     * @param k the key
     * @param v the value
     * @return the value previously associated with the key
     */
    V putFirst(K k, V v);

    /**
     * Creates an entry for the specified key and value and adds it to the tail
     * of the map if an entry for the specified key is not already present.
     * If this map already contains an entry for the specified key, replaces the
     * value and moves the entry to the tail.
     *
     * @param k the key
     * @param v the value
     * @return the previous value associated with the key
     */
    V putLast(K k, V v);

    /**
     * Returns a {@link SequencedSet} view of the keys contained in this map.
     *
     * @return a {@link SequencedSet} view of the keys
     */
    @NonNull
    SequencedSet<K> _sequencedKeySet();

    /**
     * Returns a {@link SequencedCollection} view of the values contained in
     * this map.
     *
     * @return a {@link SequencedCollection} view of the values
     */
    @NonNull
    SequencedCollection<V> _sequencedValues();

    /**
     * Returns a {@link SequencedSet} view of the entries contained in this map.
     *
     * @return a {@link SequencedSet} view of the entries
     */
    @NonNull
    SequencedSet<Entry<K, V>> _sequencedEntrySet();

    @NonNull
    @Override
    default Set<K> keySet() {
        return _sequencedKeySet();
    }

    @NonNull
    @Override
    default Collection<V> values() {
        return _sequencedValues();
    }

    @NonNull
    @Override
    default Set<Entry<K, V>> entrySet() {
        return _sequencedEntrySet();
    }

    @Override
    default V getOrDefault(Object key, V defaultValue) {
        return Map.super.getOrDefault(key, defaultValue);
    }

}
