/*
 * @(#)TrieMapTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;

public class PersistentSequencedTrieSetTest extends AbstractPersistentSequencedSetTest {

    @Override
    protected PersistentSet<HashCollider> copyOf(@NonNull Iterable<? extends HashCollider> set) {
        return PersistentSequencedTrieSet.copyOf(set);
    }

    @Override
    protected PersistentSet<HashCollider> of() {
        return PersistentSequencedTrieSet.of();
    }

    @Override
    protected PersistentSet<HashCollider> of(@NonNull HashCollider... keys) {
        return PersistentSequencedTrieSet.<HashCollider>of().copyAddAll(Arrays.asList(keys));
    }


    @Test
    @Ignore("manual test")
    public void testDumpStructure() {
        PersistentSequencedTrieSet<HashCollider> instance = PersistentSequencedTrieSet.of();
        Random rng = new Random(0);
        for (int i = 0; i < 30; i++) {
            HashCollider key = new HashCollider(rng.nextInt(1_000), ~0xff00);
            String value = "v" + i;
            instance = instance.copyAdd(key);
        }

        System.out.println(instance.dump());

    }

}
