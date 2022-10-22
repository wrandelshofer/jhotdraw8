/*
 * @(#)ImmutableChampMapTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.champ;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.AbstractImmutableMapTest;
import org.jhotdraw8.collection.immutable.ImmutableMap;
import org.jhotdraw8.collection.readonly.ReadOnlyMap;

import java.util.Map;

public class ChampImmutableMapTest extends AbstractImmutableMapTest {
    @Override
    protected <K, V> @NonNull ChampImmutableMap<K, V> newInstance() {
        return ChampImmutableMap.of();
    }


    @Override
    protected <K, V> @NonNull ImmutableMap<K, V> newInstance(@NonNull Map<K, V> map) {
        return ChampImmutableMap.<K, V>of().putAll(map);
    }

    @Override
    protected <K, V> @NonNull ImmutableMap<K, V> newInstance(@NonNull ReadOnlyMap<K, V> map) {
        return ChampImmutableMap.<K, V>of().putAll(map);
    }

    @Override
    protected @NonNull <K, V> ImmutableMap<K, V> toClonedInstance(@NonNull ImmutableMap<K, V> m) {
        return ChampImmutableMap.<K, V>copyOf(m);
    }

    @Override
    protected <K, V> @NonNull ImmutableMap<K, V> newInstance(@NonNull Iterable<Map.Entry<K, V>> entries) {
        return ChampImmutableMap.<K, V>of().putAll(entries);
    }
}
