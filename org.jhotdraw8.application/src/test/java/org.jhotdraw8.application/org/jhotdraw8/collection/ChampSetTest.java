/*
 * @(#)ChampSetTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;
import java.util.Set;

public class ChampSetTest extends AbstractSetTest {
    @Override
    protected @NonNull <T> Set<T> create(int expectedMaxSize, float maxLoadFactor) {
        return new ChampSet<>();
    }


    @Override
    public void doTest(List<HashCollider> list) throws Exception {
        super.doTest(list);
        doTestCopyConstructor(list);
        doTestClone(list);
    }


    public void doTestCopyConstructor(List<HashCollider> list) {
        ChampSet<HashCollider> expected = new ChampSet<>(list);
        ChampSet<HashCollider> instance = new ChampSet<>(expected);
        assertEqualSets(expected, instance);
    }

    public void doTestClone(List<HashCollider> list) {
        ChampSet<HashCollider> expected = new ChampSet<>(list);
        ChampSet<HashCollider> instance = expected.clone();
        assertEqualSets(expected, instance);
    }

    @Test
    public void testDumpStructure() {
        ChampSet<HashCollider> instance = new ChampSet<>();
        Random rng = new Random(0);
        for (int i = 0; i < 30; i++) {
            HashCollider key = new HashCollider(rng.nextInt(1_000), ~0xff00);
            instance.add(key);
        }
        System.out.println(instance.dump());
    }

}
