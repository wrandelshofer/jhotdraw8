/*
 * @(#)TrieMapTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Map;
import java.util.Random;

public class SeqTrieMapTest extends AbstractSequencedMapTest {


    @Override
    protected @NonNull Map<HashCollider, HashCollider> of() {
        return new SeqTrieMap<>();
    }

    @Override
    protected @NonNull Map<HashCollider, HashCollider> copyOf(@NonNull Map<HashCollider, HashCollider> map) {
        return new SeqTrieMap<>(map);
    }

    @Override
    protected @NonNull Map<HashCollider, HashCollider> copyOf(@NonNull Collection<Map.Entry<HashCollider, HashCollider>> map) {
        return new SeqTrieMap<>(map);
    }

    @Test
    @Ignore("manual test")
    public void testDumpStructure() {
        SeqTrieMap<HashCollider, String> instance = new SeqTrieMap<>();
        Random rng = new Random(0);
        for (int i = 0; i < 30; i++) {
            HashCollider key = new HashCollider(rng.nextInt(1_000), ~0xff00);
            String value = "v" + i;
            instance.put(key, value);
        }

        System.out.println(instance.dump());

    }

}
