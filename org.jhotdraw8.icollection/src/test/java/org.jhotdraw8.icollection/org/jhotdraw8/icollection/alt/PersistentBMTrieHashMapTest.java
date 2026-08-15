/*
 * @(#)ImmutableSequencedChampMapTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.alt;

import org.jhotdraw8.icollection.AbstractImmutableSequencedMapTest;
import org.jhotdraw8.icollection.persistent.PersistentMap;
import org.jhotdraw8.icollection.readable.ReadableMap;

import java.util.Map;

public class PersistentBMTrieHashMapTest extends AbstractImmutableSequencedMapTest {
    @Override
    protected <K, V> PersistentBMTrieHashMap<K, V> newInstance() {
        return PersistentBMTrieHashMap.of();
    }


    @Override
    protected <K, V> PersistentBMTrieHashMap<K, V> newInstance(Map<K, V> map) {
        return PersistentBMTrieHashMap.<K, V>of().puttingAll(map);
    }

    @Override
    protected <K, V> PersistentBMTrieHashMap<K, V> newInstance(ReadableMap<K, V> map) {
        return PersistentBMTrieHashMap.<K, V>of().puttingAll(map);
    }

    @Override
    protected <K, V> PersistentBMTrieHashMap<K, V> toClonedInstance(PersistentMap<K, V> m) {
        return PersistentBMTrieHashMap.copyOf(m);
    }

    @Override
    protected <K, V> PersistentBMTrieHashMap<K, V> newInstance(Iterable<Map.Entry<K, V>> entries) {
        return PersistentBMTrieHashMap.<K, V>of().puttingAll(entries);
    }

    @Override
    protected boolean supportsNullKeys() {
        return true;
    }

}
