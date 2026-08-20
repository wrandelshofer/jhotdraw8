/*
 * @(#)ImmutableChampMapTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.persistent.PersistentMap;
import org.jhotdraw8.icollection.readable.ReadableMap;

import java.util.Map;

public class PersistentHashMapTest extends AbstractPersistentMapTest {
    @Override
    protected <K, V> PersistentHashMap<K, V> newInstance() {
        return PersistentHashMap.of();
    }


    @Override
    protected <K, V> PersistentMap<K, V> newInstance(Map<K, V> map) {
        return PersistentHashMap.<K, V>of().puttingAll(map);
    }

    @Override
    protected <K, V> PersistentMap<K, V> newInstance(ReadableMap<K, V> map) {
        return PersistentHashMap.<K, V>of().puttingAll(map);
    }

    @Override
    protected <K, V> PersistentMap<K, V> toClonedInstance(PersistentMap<K, V> m) {
        return PersistentHashMap.copyOf(m);
    }

    @Override
    protected <K, V> PersistentMap<K, V> newInstance(java.lang.Iterable<Map.Entry<K, V>> entries) {
        return PersistentHashMap.<K, V>of().puttingAll(entries);
    }

    @Override
    protected boolean supportsNullKeys() {
        return true;
    }

}
