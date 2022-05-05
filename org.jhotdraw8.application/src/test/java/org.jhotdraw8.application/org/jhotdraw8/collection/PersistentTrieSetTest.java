/*
 * @(#)PersistentTrieSetTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.junit.jupiter.api.Test;

import java.util.Random;

public class PersistentTrieSetTest extends AbstractPersistentSetTest {

    @Override
    protected PersistentSet<HashCollider> of() {
        return PersistentTrieSet.of();
    }

    @Override
    protected PersistentSet<HashCollider> of(@NonNull HashCollider... keys) {
        return PersistentTrieSet.of(keys);
    }

    @Override
    protected PersistentSet<HashCollider> copyOf(@NonNull Iterable<? extends HashCollider> set) {
        return PersistentTrieSet.copyOf(set);
    }

    @Test
    public void testDumpStructure() {
        PersistentTrieSet<Integer> instance = PersistentTrieSet.of();
        Random rng = new Random(0);
        for (int i = 0; i < 5; i++) {
            int key = rng.nextInt(10_000);
            instance = instance.copyAdd(key);
        }

        System.out.println(instance.dump());
    }

}