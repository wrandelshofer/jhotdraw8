/*
 * @(#)SequencedMap.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import java.util.Map;

/**
 * Interface for a set with a well-defined linear ordering of its elements.
 * <p>
 * References:
 * <dl>
 *     <dt>JEP draft: Sequenced Collections</dt>
 *     <dd><a href="https://openjdk.java.net/jeps/8280836">java.ne</a></dd>
 * </dl>
 *
 * @param <E> the element type
 */
public interface SequencedMap<K, V> extends Map<K, V> {
    /**
     * Gets the first entry.
     *
     * @return an entry
     * @throws java.util.NoSuchElementException if the map is empty
     */
    Entry<K, V> firstEntry();

    /**
     * Gets the last entry.
     *
     * @return an entry
     * @throws java.util.NoSuchElementException if the map is empty
     */
    Entry<K, V> lastEntry();

    /**
     * Gets the first key.
     *
     * @return a key
     * @throws java.util.NoSuchElementException if the map is empty
     */
    default K firstKey() {
        return firstEntry().getKey();
    }

    /**
     * Gets the last key.
     *
     * @return a key
     * @throws java.util.NoSuchElementException if the map is empty
     */
    default K lastKey() {
        return lastEntry().getKey();
    }

    /**
     * Creates an entry for the specified key and value and adds it to the front
     * of the map if an entry for the specified key is not already present.
     * If this map already contains an entry for the specified key, replaces the
     * value and moves the entry to the front.
     *
     * @param k a key
     * @param v a value
     * @return the previous value associated with the key
     */
    V putFirst(K k, V v);

    /**
     * Creates an entry for the specified key and value and adds it to the tail
     * of the map if an entry for the specified key is not already present.
     * If this map already contains an entry for the specified key, replaces the
     * value and moves the entry to the tail.
     *
     * @param k a key
     * @param v a value
     * @return the previous value associated with the key
     */
    V putLast(K k, V v);

    @Override
    SequencedSet<K> keySet();

    @Override
    SequencedCollection<V> values();

    @Override
    SequencedSet<Entry<K, V>> entrySet();
}
