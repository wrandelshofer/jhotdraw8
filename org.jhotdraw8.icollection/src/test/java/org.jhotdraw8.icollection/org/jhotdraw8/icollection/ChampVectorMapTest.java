/*
 * @(#)ImmutableSequencedChampMapTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.persistent.PersistentMap;
import org.jhotdraw8.icollection.readable.ReadableMap;

import java.util.Map;

public class ChampVectorMapTest extends AbstractImmutableSequencedMapTest {
    @Override
    protected <K, V> ChampVectorMap<K, V> newInstance() {
        return ChampVectorMap.of();
    }


    @Override
    protected <K, V> ChampVectorMap<K, V> newInstance(Map<K, V> map) {
        return ChampVectorMap.<K, V>of().putAll(map);
    }

    @Override
    protected <K, V> ChampVectorMap<K, V> newInstance(ReadableMap<K, V> map) {
        return ChampVectorMap.<K, V>of().putAll(map);
    }

    @Override
    protected <K, V> ChampVectorMap<K, V> toClonedInstance(PersistentMap<K, V> m) {
        return ChampVectorMap.copyOf(m);
    }

    @Override
    protected <K, V> ChampVectorMap<K, V> newInstance(Iterable<Map.Entry<K, V>> entries) {
        return ChampVectorMap.<K, V>of().putAll(entries);
    }

    @Override
    protected boolean supportsNullKeys() {
        return true;
    }

}
