/*
 * @(#)ImmutableSeqChampMapTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;
import java.util.Random;

public class ImmutableSeqChampMapTest extends AbstractPersistentSequencedMapTest {


    @Override
    protected @NonNull ImmutableMap<HashCollider, HashCollider> of() {
        return ImmutableSeqChampMap.of();
    }

    @Override
    protected @NonNull ImmutableMap<HashCollider, HashCollider> of(Map.@NonNull Entry<HashCollider, HashCollider>... entries) {
        return ImmutableSeqChampMap.<HashCollider, HashCollider>of().copyPutAll(Arrays.asList(entries));
    }

    @Override
    protected @NonNull ImmutableMap<HashCollider, HashCollider> copyOf(@NonNull Map<? extends HashCollider, ? extends HashCollider> map) {
        return ImmutableSeqChampMap.<HashCollider, HashCollider>of().copyPutAll(map);
    }

    @Override
    protected @NonNull ImmutableMap<HashCollider, HashCollider> copyOf(@NonNull ReadOnlyMap<? extends HashCollider, ? extends HashCollider> map) {
        return ImmutableSeqChampMap.<HashCollider, HashCollider>of().copyPutAll(map);
    }

    @Override
    protected @NonNull ImmutableMap<HashCollider, HashCollider> copyOf(@NonNull Iterable<? extends Map.Entry<? extends HashCollider, ? extends HashCollider>> entries) {
        return ImmutableSeqChampMap.<HashCollider, HashCollider>of().copyPutAll(entries);
    }

    @Test
    @Ignore("manual test")
    public void testDumpStructure() {
        ImmutableSeqChampMap<HashCollider, String> instance = ImmutableSeqChampMap.of();
        Random rng = new Random(0);
        for (int i = 0; i < 30; i++) {
            HashCollider key = new HashCollider(rng.nextInt(1_000), ~0xff00);
            String value = "v" + i;
            instance = instance.copyPut(key, value);
        }

        System.out.println(instance.dump());

    }

}
