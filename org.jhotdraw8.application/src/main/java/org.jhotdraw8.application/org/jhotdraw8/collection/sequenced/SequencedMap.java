/*
 * @(#)SequencedMap.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.sequenced;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.util.Iterator;
import java.util.Map;

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
    default @Nullable Map.Entry<K, V> firstEntry() {
        return isEmpty() ? null : entrySet().iterator().next();
    }

    /**
     * Gets the first key in this map.
     *
     * @return the first key
     * @throws java.util.NoSuchElementException if the map is empty
     */
    default K firstKey() {
        return keySet().iterator().next();
    }

    /**
     * Gets the last entry in this map or {@code null} if this map is empty.
     *
     * @return the last entry or {@code null}
     * @throws java.util.NoSuchElementException if the map is empty
     */
    default @Nullable Map.Entry<K, V> lastEntry() {
        return isEmpty() ? null : reversed().entrySet().iterator().next();
    }

    /**
     * Gets the last key in this map.
     *
     * @return the last key
     * @throws java.util.NoSuchElementException if the map is empty
     */
    default K lastKey() {
        return reversed().keySet().iterator().next();
    }

    /**
     * Returns a reversed-order view of this map.
     * <p>
     * Modifications write through to the underlying map.
     * Changes to the underlying map are visible in the reversed view.
     *
     * @return a reversed-order view of this map
     */
    @NonNull SequencedMap<K, V> reversed();

    /**
     * Removes and returns the first entry in this map or code {@code null}
     * if the map is empty.
     *
     * @return the removed first entry of this map or code {@code null}
     */
    default Map.Entry<K, V> pollFirstEntry() {
        Iterator<Entry<K, V>> it = entrySet().iterator();
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
        Iterator<Entry<K, V>> it = reversed().entrySet().iterator();
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

    @Override
    @NonNull
    SequencedSet<K> keySet();

    @Override
    @NonNull
    SequencedCollection<V> values();

    @Override
    @NonNull
    SequencedSet<Entry<K, V>> entrySet();

    @Override
    default V getOrDefault(Object key, V defaultValue) {
        return Map.super.getOrDefault(key, defaultValue);
    }

}
