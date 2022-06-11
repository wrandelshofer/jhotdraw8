/*
 * @(#)ImmutableSeqChampSetTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.champset.ChampTrieGraphviz;
import org.jhotdraw8.collection.champset.SequencedKey;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;

public class ImmutableSequencedChampSetTest extends AbstractImmutableSequencedSetTest {

    @Override
    protected ImmutableSet<HashCollider> copyOf(@NonNull Iterable<? extends HashCollider> set) {
        return ImmutableSequencedChampSet.copyOf(set);
    }

    @Override
    protected ImmutableSet<HashCollider> of() {
        return ImmutableSequencedChampSet.of();
    }

    @Override
    protected ImmutableSet<HashCollider> of(@NonNull HashCollider... keys) {
        return ImmutableSequencedChampSet.<HashCollider>of().copyAddAll(Arrays.asList(keys));
    }


    @Test
    @Ignore("manual test")
    public void testDumpStructure() {
        ImmutableSequencedChampSet<HashCollider> instance = ImmutableSequencedChampSet.of();
        Random rng = new Random(0);
        for (int i = 0; i < 30; i++) {
            HashCollider key = new HashCollider(rng.nextInt(1_000), ~0xff00);
            String value = "v" + i;
            instance = instance.copyAdd(key);
        }
        System.out.println(new ChampTrieGraphviz<SequencedKey<HashCollider>>().dumpTrie(instance));
    }

}
