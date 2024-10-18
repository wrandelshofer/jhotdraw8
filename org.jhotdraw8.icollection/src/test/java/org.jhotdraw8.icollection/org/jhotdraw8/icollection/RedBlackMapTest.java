/*
 * @(#)ImmutableChampMapTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.persistent.PersistentMap;
import org.jhotdraw8.icollection.readable.ReadableMap;

import java.util.Map;

public class RedBlackMapTest extends AbstractImmutableNavigableMapTest {
    @Override
    protected <K, V> RedBlackMap<K, V> newInstance() {
        return RedBlackMap.of();
    }


    @Override
    protected <K, V> RedBlackMap<K, V> newInstance(Map<K, V> map) {
        return RedBlackMap.<K, V>of().putAll(map);
    }

    @Override
    protected <K, V> RedBlackMap<K, V> newInstance(ReadableMap<K, V> map) {
        return RedBlackMap.<K, V>of().putAll(map);
    }

    @Override
    protected <K, V> RedBlackMap<K, V> toClonedInstance(PersistentMap<K, V> m) {
        return RedBlackMap.copyOf(m);
    }

    @Override
    protected <K, V> RedBlackMap<K, V> newInstance(Iterable<Map.Entry<K, V>> entries) {
        return RedBlackMap.<K, V>of().putAll(entries);
    }

    @Override
    protected boolean supportsNullKeys() {
        return true;
    }

}
