/*
 * @(#)ImmutableSequencedChampMapTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.icollection.immutable.ImmutableMap;
import org.jhotdraw8.icollection.readonly.ReadOnlyMap;

import java.util.Map;

public class ChampVectorMapTest extends AbstractImmutableSequencedMapTest {
    @Override
    protected <K, V> @NonNull ChampVectorMap<K, V> newInstance() {
        return ChampVectorMap.of();
    }


    @Override
    protected <K, V> @NonNull ChampVectorMap<K, V> newInstance(@NonNull Map<K, V> map) {
        return ChampVectorMap.<K, V>of().putAll(map);
    }

    @Override
    protected <K, V> @NonNull ChampVectorMap<K, V> newInstance(@NonNull ReadOnlyMap<K, V> map) {
        return ChampVectorMap.<K, V>of().putAll(map);
    }

    @Override
    protected @NonNull <K, V> ChampVectorMap<K, V> toClonedInstance(@NonNull ImmutableMap<K, V> m) {
        return ChampVectorMap.copyOf(m);
    }

    @Override
    protected <K, V> @NonNull ChampVectorMap<K, V> newInstance(@NonNull Iterable<Map.Entry<K, V>> entries) {
        return ChampVectorMap.<K, V>of().putAll(entries);
    }

    @Override
    protected boolean supportsNullKeys() {
        return true;
    }

}
