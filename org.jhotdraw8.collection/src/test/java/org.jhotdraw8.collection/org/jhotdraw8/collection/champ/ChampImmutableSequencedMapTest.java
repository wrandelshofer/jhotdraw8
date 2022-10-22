/*
 * @(#)ImmutableSequencedChampMapTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.champ;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.AbstractImmutableSequencedMapTest;
import org.jhotdraw8.collection.immutable.ImmutableMap;
import org.jhotdraw8.collection.readonly.ReadOnlyMap;

import java.util.Map;

public class ChampImmutableSequencedMapTest extends AbstractImmutableSequencedMapTest {
    @Override
    protected <K, V> @NonNull ChampImmutableSequencedMap<K, V> newInstance() {
        return ChampImmutableSequencedMap.of();
    }


    @Override
    protected <K, V> @NonNull ChampImmutableSequencedMap<K, V> newInstance(@NonNull Map<K, V> map) {
        return ChampImmutableSequencedMap.<K, V>of().putAll(map);
    }

    @Override
    protected <K, V> @NonNull ChampImmutableSequencedMap<K, V> newInstance(@NonNull ReadOnlyMap<K, V> map) {
        return ChampImmutableSequencedMap.<K, V>of().putAll(map);
    }

    @Override
    protected @NonNull <K, V> ChampImmutableSequencedMap<K, V> toClonedInstance(@NonNull ImmutableMap<K, V> m) {
        return ChampImmutableSequencedMap.<K, V>copyOf(m);
    }

    @Override
    protected <K, V> @NonNull ChampImmutableSequencedMap<K, V> newInstance(@NonNull Iterable<Map.Entry<K, V>> entries) {
        return ChampImmutableSequencedMap.<K, V>of().putAll(entries);
    }
}
