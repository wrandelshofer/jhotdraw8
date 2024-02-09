/*
 * @(#)ImmutableChampMapTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.icollection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.icollection.immutable.ImmutableMap;
import org.jhotdraw8.icollection.readonly.ReadOnlyMap;

import java.util.Map;

public class RedBlackMapTest extends AbstractImmutableNavigableMapTest {
    @Override
    protected <K, V> @NonNull RedBlackMap<K, V> newInstance() {
        return RedBlackMap.of();
    }


    @Override
    protected <K, V> @NonNull RedBlackMap<K, V> newInstance(@NonNull Map<K, V> map) {
        return RedBlackMap.<K, V>of().putAll(map);
    }

    @Override
    protected <K, V> @NonNull RedBlackMap<K, V> newInstance(@NonNull ReadOnlyMap<K, V> map) {
        return RedBlackMap.<K, V>of().putAll(map);
    }

    @Override
    protected @NonNull <K, V> RedBlackMap<K, V> toClonedInstance(@NonNull ImmutableMap<K, V> m) {
        return RedBlackMap.copyOf(m);
    }

    @Override
    protected <K, V> @NonNull RedBlackMap<K, V> newInstance(@NonNull Iterable<Map.Entry<K, V>> entries) {
        return RedBlackMap.<K, V>of().putAll(entries);
    }

    @Override
    protected boolean supportsNullKeys() {
        return true;
    }

}
