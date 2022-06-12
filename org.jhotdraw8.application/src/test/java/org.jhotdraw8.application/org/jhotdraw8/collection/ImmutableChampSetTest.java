/*
 * @(#)PersistentChampSetTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.champ.ChampTrieGraphviz;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Random;

public class ImmutableChampSetTest extends AbstractImmutableSetTest {

    @Override
    protected ImmutableSet<HashCollider> of() {
        return ImmutableChampSet.of();
    }

    @Override
    protected ImmutableSet<HashCollider> of(@NonNull HashCollider... keys) {
        return ImmutableChampSet.<HashCollider>of().copyAddAll(Arrays.asList(keys));
    }

    @Override
    protected ImmutableSet<HashCollider> copyOf(@NonNull Iterable<? extends HashCollider> set) {
        return ImmutableChampSet.<HashCollider>of().copyAddAll(set);
    }

    @Test
    public void testDumpStructure() {
        ImmutableChampSet<Integer> instance = ImmutableChampSet.of();
        Random rng = new Random(0);
        for (int i = 0; i < 5; i++) {
            int key = rng.nextInt(10_000);
            instance = instance.copyAdd(key);
        }

        System.out.println(new ChampTrieGraphviz().dumpTrie(instance));
    }

}