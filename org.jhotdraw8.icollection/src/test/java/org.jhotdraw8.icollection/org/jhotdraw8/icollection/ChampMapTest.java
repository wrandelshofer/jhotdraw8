/*
 * @(#)ImmutableChampMapTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.persistent.PersistentMap;
import org.jhotdraw8.icollection.readable.ReadableMap;

import java.util.Map;

public class ChampMapTest extends AbstractPersistentMapTest {
    @Override
    protected <K, V> ChampMap<K, V> newInstance() {
        return ChampMap.of();
    }


    @Override
    protected <K, V> PersistentMap<K, V> newInstance(Map<K, V> map) {
        return ChampMap.<K, V>of().putAll(map);
    }

    @Override
    protected <K, V> PersistentMap<K, V> newInstance(ReadableMap<K, V> map) {
        return ChampMap.<K, V>of().putAll(map);
    }

    @Override
    protected <K, V> PersistentMap<K, V> toClonedInstance(PersistentMap<K, V> m) {
        return ChampMap.copyOf(m);
    }

    @Override
    protected <K, V> PersistentMap<K, V> newInstance(Iterable<Map.Entry<K, V>> entries) {
        return ChampMap.<K, V>of().putAll(entries);
    }

    @Override
    protected boolean supportsNullKeys() {
        return true;
    }

}
