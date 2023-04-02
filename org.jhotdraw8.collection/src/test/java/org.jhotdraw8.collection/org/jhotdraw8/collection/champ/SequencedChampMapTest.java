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

public class SequencedChampMapTest extends AbstractImmutableSequencedMapTest {
    @Override
    protected <K, V> @NonNull SequencedChampMap<K, V> newInstance() {
        return SequencedChampMap.of();
    }


    @Override
    protected <K, V> @NonNull SequencedChampMap<K, V> newInstance(@NonNull Map<K, V> map) {
        return SequencedChampMap.<K, V>of().putAll(map);
    }

    @Override
    protected <K, V> @NonNull SequencedChampMap<K, V> newInstance(@NonNull ReadOnlyMap<K, V> map) {
        return SequencedChampMap.<K, V>of().putAll(map);
    }

    @Override
    protected @NonNull <K, V> SequencedChampMap<K, V> toClonedInstance(@NonNull ImmutableMap<K, V> m) {
        return SequencedChampMap.<K, V>copyOf(m);
    }

    @Override
    protected <K, V> @NonNull SequencedChampMap<K, V> newInstance(@NonNull Iterable<Map.Entry<K, V>> entries) {
        return SequencedChampMap.<K, V>of().putAll(entries);
    }
}
