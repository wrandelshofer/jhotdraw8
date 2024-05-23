/*
 * @(#)ImmutableChampMapTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.immutable.ImmutableMap;
import org.jhotdraw8.icollection.readonly.ReadOnlyMap;

import java.util.Map;

public class ChampMapTest extends AbstractImmutableMapTest {
    @Override
    protected <K, V> ChampMap<K, V> newInstance() {
        return ChampMap.of();
    }


    @Override
    protected <K, V> ImmutableMap<K, V> newInstance(Map<K, V> map) {
        return ChampMap.<K, V>of().putAll(map);
    }

    @Override
    protected <K, V> ImmutableMap<K, V> newInstance(ReadOnlyMap<K, V> map) {
        return ChampMap.<K, V>of().putAll(map);
    }

    @Override
    protected <K, V> ImmutableMap<K, V> toClonedInstance(ImmutableMap<K, V> m) {
        return ChampMap.copyOf(m);
    }

    @Override
    protected <K, V> ImmutableMap<K, V> newInstance(Iterable<Map.Entry<K, V>> entries) {
        return ChampMap.<K, V>of().putAll(entries);
    }

    @Override
    protected boolean supportsNullKeys() {
        return true;
    }

}
