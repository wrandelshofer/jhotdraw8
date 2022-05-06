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
    Entry<K, V> firstEntry();

    Entry<K, V> lastEntry();

    default K firstKey() {
        return firstEntry().getKey();
    }

    default K lastKey() {
        return lastEntry().getKey();
    }

    V putFirst(K k, V v);

    V putLast(K k, V v);

    SequencedSet<K> keySet();

    SequencedCollection<V> values();

    SequencedSet<Entry<K, V>> entrySet();
}
