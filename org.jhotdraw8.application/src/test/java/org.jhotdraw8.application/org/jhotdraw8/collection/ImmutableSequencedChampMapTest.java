/*
 * @(#)ImmutableChampMapTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.champ.ChampTrieGraphviz;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Random;

public class ImmutableSequencedChampMapTest extends AbstractImmutableSequencedMapTest {
    @Override
    protected <K, V> @NonNull ImmutableSequencedChampMap<K, V> newInstance() {
        return ImmutableSequencedChampMap.of();
    }


    @Override
    protected <K, V> @NonNull ImmutableSequencedChampMap<K, V> newInstance(@NonNull Map<K, V> map) {
        return ImmutableSequencedChampMap.<K, V>of().copyPutAll(map);
    }

    @Override
    protected <K, V> @NonNull ImmutableSequencedChampMap<K, V> newInstance(@NonNull ReadOnlyMap<K, V> map) {
        return ImmutableSequencedChampMap.<K, V>of().copyPutAll(map);
    }

    @Override
    protected @NonNull <K, V> ImmutableSequencedChampMap<K, V> toClonedInstance(@NonNull ImmutableMap<K, V> m) {
        return ImmutableSequencedChampMap.<K, V>copyOf(m);
    }

    @Override
    protected <K, V> @NonNull ImmutableSequencedChampMap<K, V> newInstance(@NonNull Iterable<Map.Entry<K, V>> entries) {
        return ImmutableSequencedChampMap.<K, V>of().copyPutAll(entries);
    }

    @Test
    @Ignore("manual test")
    public void testDumpStructure() {
        ImmutableSequencedChampMap<HashCollider, String> instance = ImmutableSequencedChampMap.of();
        Random rng = new Random(0);
        for (int i = 0; i < 30; i++) {
            HashCollider key = new HashCollider(rng.nextInt(1_000), ~0xff00);
            String value = "v" + i;
            instance = instance.copyPut(key, value);
        }

        System.out.println(new ChampTrieGraphviz().dumpTrie(instance));
    }
}
