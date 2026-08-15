/*
 * @(#)ImmutableSequencedChampMapTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.persistent.PersistentMap;
import org.jhotdraw8.icollection.readable.ReadableMap;

import java.util.Map;

public class PersistentVectorHashMapTest extends AbstractImmutableSequencedMapTest {
    @Override
    protected <K, V> PersistentVectorHashMap<K, V> newInstance() {
        return PersistentVectorHashMap.of();
    }


    @Override
    protected <K, V> PersistentVectorHashMap<K, V> newInstance(Map<K, V> map) {
        return PersistentVectorHashMap.<K, V>copyOf(map);
    }

    @Override
    protected <K, V> PersistentVectorHashMap<K, V> newInstance(ReadableMap<K, V> map) {
        return PersistentVectorHashMap.<K, V>copyOf(map);
    }

    @Override
    protected <K, V> PersistentVectorHashMap<K, V> toClonedInstance(PersistentMap<K, V> m) {
        return PersistentVectorHashMap.copyOf(m);
    }

    @Override
    protected <K, V> PersistentVectorHashMap<K, V> newInstance(Iterable<Map.Entry<K, V>> entries) {
        return new PersistentVectorHashMapBuilder<K, V>().addEntries(entries).build();
    }

    @Override
    protected boolean supportsNullKeys() {
        return true;
    }

}
