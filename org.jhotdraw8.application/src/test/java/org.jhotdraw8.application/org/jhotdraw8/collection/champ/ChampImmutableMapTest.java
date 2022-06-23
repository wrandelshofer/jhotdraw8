/*
 * @(#)ImmutableChampMapTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.champ;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.AbstractImmutableMapTest;
import org.jhotdraw8.collection.HashCollider;
import org.jhotdraw8.collection.immutable.ImmutableMap;
import org.jhotdraw8.collection.readonly.ReadOnlyMap;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Random;

public class ChampImmutableMapTest extends AbstractImmutableMapTest {
    @Override
    protected <K, V> @NonNull ChampImmutableMap<K, V> newInstance() {
        return ChampImmutableMap.of();
    }


    @Override
    protected <K, V> @NonNull ImmutableMap<K, V> newInstance(@NonNull Map<K, V> map) {
        return ChampImmutableMap.<K, V>of().copyPutAll(map);
    }

    @Override
    protected <K, V> @NonNull ImmutableMap<K, V> newInstance(@NonNull ReadOnlyMap<K, V> map) {
        return ChampImmutableMap.<K, V>of().copyPutAll(map);
    }

    @Override
    protected @NonNull <K, V> ImmutableMap<K, V> toClonedInstance(@NonNull ImmutableMap<K, V> m) {
        return ChampImmutableMap.<K, V>copyOf(m);
    }

    @Override
    protected <K, V> @NonNull ImmutableMap<K, V> newInstance(@NonNull Iterable<Map.Entry<K, V>> entries) {
        return ChampImmutableMap.<K, V>of().copyPutAll(entries);
    }

    @Test
    @Ignore("manual test")
    public void testDumpStructure() {
        ChampImmutableMap<HashCollider, String> instance = ChampImmutableMap.of();
        Random rng = new Random(0);
        for (int i = 0; i < 30; i++) {
            HashCollider key = new HashCollider(rng.nextInt(1_000), ~0xff00);
            String value = "v" + i;
            instance = instance.copyPut(key, value);
        }

        System.out.println(new ChampTrieGraphviz().dumpTrie(instance));
    }
}
