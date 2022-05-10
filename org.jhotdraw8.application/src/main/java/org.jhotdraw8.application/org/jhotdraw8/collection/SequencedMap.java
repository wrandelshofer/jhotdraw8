/*
 * @(#)SequencedMap.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;

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
 * @param <K> the key type
 * @param <V> the value type
 */
public interface SequencedMap<K, V> extends Map<K, V>, ReadOnlySequencedMap<K, V> {


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

    @Override
    default boolean containsValue(Object value) {
        return ReadOnlySequencedMap.super.containsValue(value);
    }
}
