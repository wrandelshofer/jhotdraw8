/*
 * @(#)ImmutableSequencedChampMapTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection.alt;

import org.jhotdraw8.icollection.AbstractImmutableSequencedMapTest;
import org.jhotdraw8.icollection.persistent.PersistentMap;
import org.jhotdraw8.icollection.readable.ReadableMap;

import java.util.Map;

public class PersistentBMTrieMapTest extends AbstractImmutableSequencedMapTest {
    @Override
    protected <K, V> PersistentBMTrieMap<K, V> newInstance() {
        return PersistentBMTrieMap.of();
    }


    @Override
    protected <K, V> PersistentBMTrieMap<K, V> newInstance(Map<K, V> map) {
        return PersistentBMTrieMap.<K, V>of().puttingAll(map);
    }

    @Override
    protected <K, V> PersistentBMTrieMap<K, V> newInstance(ReadableMap<K, V> map) {
        return PersistentBMTrieMap.<K, V>of().puttingAll(map);
    }

    @Override
    protected <K, V> PersistentBMTrieMap<K, V> toClonedInstance(PersistentMap<K, V> m) {
        return PersistentBMTrieMap.copyOf(m);
    }

    @Override
    protected <K, V> PersistentBMTrieMap<K, V> newInstance(Iterable<Map.Entry<K, V>> entries) {
        return PersistentBMTrieMap.<K, V>of().puttingAll(entries);
    }

    @Override
    protected boolean supportsNullKeys() {
        return true;
    }

}
