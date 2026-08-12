/*
 * @(#)ImmutableSequencedChampMapTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.persistent.PersistentMap;
import org.jhotdraw8.icollection.readable.ReadableMap;

import java.util.Map;

public class PersistentHashVectorMapTest extends AbstractImmutableSequencedMapTest {
    @Override
    protected <K, V> PersistentHashVectorMap<K, V> newInstance() {
        return PersistentHashVectorMap.of();
    }


    @Override
    protected <K, V> PersistentHashVectorMap<K, V> newInstance(Map<K, V> map) {
        return PersistentHashVectorMap.<K, V>of().putAll(map);
    }

    @Override
    protected <K, V> PersistentHashVectorMap<K, V> newInstance(ReadableMap<K, V> map) {
        return PersistentHashVectorMap.<K, V>of().putAll(map);
    }

    @Override
    protected <K, V> PersistentHashVectorMap<K, V> toClonedInstance(PersistentMap<K, V> m) {
        return PersistentHashVectorMap.copyOf(m);
    }

    @Override
    protected <K, V> PersistentHashVectorMap<K, V> newInstance(Iterable<Map.Entry<K, V>> entries) {
        return PersistentHashVectorMap.<K, V>of().putAll(entries);
    }

    @Override
    protected boolean supportsNullKeys() {
        return true;
    }

}
