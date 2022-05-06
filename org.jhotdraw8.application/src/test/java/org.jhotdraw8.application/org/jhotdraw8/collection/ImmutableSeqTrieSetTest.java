/*
 * @(#)ImmutableSeqTrieSetTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;

public class ImmutableSeqTrieSetTest extends AbstractPersistentSequencedSetTest {

    @Override
    protected ImmutableSet<HashCollider> copyOf(@NonNull Iterable<? extends HashCollider> set) {
        return ImmutableSeqTrieSet.copyOf(set);
    }

    @Override
    protected ImmutableSet<HashCollider> of() {
        return ImmutableSeqTrieSet.of();
    }

    @Override
    protected ImmutableSet<HashCollider> of(@NonNull HashCollider... keys) {
        return ImmutableSeqTrieSet.<HashCollider>of().copyAddAll(Arrays.asList(keys));
    }


    @Test
    @Ignore("manual test")
    public void testDumpStructure() {
        ImmutableSeqTrieSet<HashCollider> instance = ImmutableSeqTrieSet.of();
        Random rng = new Random(0);
        for (int i = 0; i < 30; i++) {
            HashCollider key = new HashCollider(rng.nextInt(1_000), ~0xff00);
            String value = "v" + i;
            instance = instance.copyAdd(key);
        }

        System.out.println(instance.dump());

    }

}
