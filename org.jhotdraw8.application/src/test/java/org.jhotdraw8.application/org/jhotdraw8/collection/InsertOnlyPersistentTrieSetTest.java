package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

class InsertOnlyPersistentTrieSetTest {

    @TestFactory
    public @NonNull List<DynamicTest> dynamicTests() {
        return Arrays.asList(
                dynamicTest("32-bits hash", () -> testPersistentTrieSet(-1)),
                dynamicTest("3-bits hash", () -> testPersistentTrieSet(7)),
                dynamicTest("0-bits hash", () -> testPersistentTrieSet(0))
        );
    }


    void testPersistentTrieSet(int hashBitMask) {
        int bulkSize = 16;
        Random rng = new Random(0);
        for (int i = 0; i < 64; i++) {
            // values1, values2 are distinct sets of values
            LinkedHashSet<HashCollider> values1 = new LinkedHashSet<>();
            LinkedHashSet<HashCollider> values2 = new LinkedHashSet<>();
            while (values1.size() < bulkSize) {
                values1.add(new HashCollider(rng.nextInt(), hashBitMask));
            }
            while (values2.size() < bulkSize) {
                HashCollider e = new HashCollider(rng.nextInt(), hashBitMask);
                if (!values1.contains(e)) {
                    values2.add(e);
                }
            }

            testWithAdd(values1, values2);
            testWithAddFromOfWith1Value(values1, values2);
        }
    }


    private void testWithAdd(LinkedHashSet<HashCollider> values1, LinkedHashSet<HashCollider> values2) {
        HashCollider firstValue1 = values1.iterator().next();
        HashCollider firstValue2 = values2.iterator().next();
        InsertOnlyPersistentTrieSet<HashCollider> actual = InsertOnlyPersistentTrieSet.of();
        InsertOnlyPersistentTrieSet<HashCollider> newActual;

        // GIVEN: a set with values1
        for (HashCollider v : values1) {
            actual = actual.withAdd(v);
        }

        // WHEN: value1 is already in set, then withAdd must yield the same set
        newActual = actual.withAdd(firstValue1);
        assertSame(newActual, actual);

        // WHEN: value2 is not yet in set, then withAdd must yield a new set
        newActual = actual.withAdd(firstValue2);
        assertNotSame(newActual, actual);
    }

    private void testWithAddFromOfWith1Value(LinkedHashSet<HashCollider> values1, LinkedHashSet<HashCollider> values2) {
        HashCollider firstValue1 = values1.iterator().next();
        HashCollider firstValue2 = values2.iterator().next();
        InsertOnlyPersistentTrieSet<HashCollider> actual = InsertOnlyPersistentTrieSet.of(firstValue1);
        InsertOnlyPersistentTrieSet<HashCollider> newActual;

        // GIVEN: a set with values1
        for (HashCollider v : values1) {
            actual = actual.withAdd(v);
        }

        // WHEN: value1 is already in set, then withAdd must yield the same set
        newActual = actual.withAdd(firstValue1);
        assertSame(newActual, actual);

        // WHEN: value2 is not yet in set, then withAdd must yield a new set
        newActual = actual.withAdd(firstValue2);
        assertNotSame(newActual, actual);
    }
}