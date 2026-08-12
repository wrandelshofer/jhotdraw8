/*
 * @(#)ImmutableChampMapTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.persistent.PersistentMap;
import org.jhotdraw8.icollection.readable.ReadableMap;

import java.util.Map;

public class PersistentTreeMapTest extends AbstractImmutableNavigableMapTest {
    @Override
    protected <K, V> PersistentTreeMap<K, V> newInstance() {
        return PersistentTreeMap.of();
    }


    @Override
    protected <K, V> PersistentTreeMap<K, V> newInstance(Map<K, V> map) {
        return PersistentTreeMap.<K, V>of().putAll(map);
    }

    @Override
    protected <K, V> PersistentTreeMap<K, V> newInstance(ReadableMap<K, V> map) {
        return PersistentTreeMap.<K, V>of().putAll(map);
    }

    @Override
    protected <K, V> PersistentTreeMap<K, V> toClonedInstance(PersistentMap<K, V> m) {
        return PersistentTreeMap.copyOf(m);
    }

    @Override
    protected <K, V> PersistentTreeMap<K, V> newInstance(Iterable<Map.Entry<K, V>> entries) {
        return PersistentTreeMap.<K, V>of().putAll(entries);
    }

    @Override
    protected boolean supportsNullKeys() {
        return true;
    }

}
