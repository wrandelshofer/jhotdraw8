/*
 * @(#)ImmutableSequencedChampMapTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.icollection.immutable.ImmutableMap;
import org.jhotdraw8.icollection.readonly.ReadOnlyMap;

import java.util.Map;

public class VectorMapTest extends AbstractImmutableSequencedMapTest {
    @Override
    protected <K, V> @NonNull VectorMap<K, V> newInstance() {
        return VectorMap.of();
    }


    @Override
    protected <K, V> @NonNull VectorMap<K, V> newInstance(@NonNull Map<K, V> map) {
        return VectorMap.<K, V>of().putAll(map);
    }

    @Override
    protected <K, V> @NonNull VectorMap<K, V> newInstance(@NonNull ReadOnlyMap<K, V> map) {
        return VectorMap.<K, V>of().putAll(map);
    }

    @Override
    protected @NonNull <K, V> VectorMap<K, V> toClonedInstance(@NonNull ImmutableMap<K, V> m) {
        return VectorMap.copyOf(m);
    }

    @Override
    protected <K, V> @NonNull VectorMap<K, V> newInstance(@NonNull Iterable<Map.Entry<K, V>> entries) {
        return VectorMap.<K, V>of().putAll(entries);
    }

    @Override
    protected boolean supportsNullKeys() {
        return true;
    }

}