package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

class PersistentTrieSetTest {
    private void assertEquality(LinkedHashSet<HashCollider> expected, PersistentTrieSet<HashCollider> actual) {
        //noinspection EqualsBetweenInconvertibleTypes
        assertTrue(actual.equals(expected));
        assertEquals(actual, actual);
        //noinspection ConstantConditions
        assertFalse(actual.equals(null));
        assertEquals(expected, actual.asSet());
        assertEquals(expected.hashCode(), actual.hashCode());
        assertEquals(expected.size(), actual.size());
        assertEquals(expected.isEmpty(), actual.isEmpty());
        assertEquals(actual, PersistentTrieSet.of(actual.toArray(new HashCollider[0])));
        assertFalse(actual.toString().isEmpty());
    }

    @TestFactory
    public @NonNull List<DynamicTest> dynamicTests() {
        return Arrays.asList(
                dynamicTest("32-bits hash", () -> testPersistentTrieSet(-1)),
                dynamicTest("3-bits hash", () -> testPersistentTrieSet(7)),
                dynamicTest("0-bits hash", () -> testPersistentTrieSet(0))
        );
    }

    private void testContains(LinkedHashSet<HashCollider> values1, LinkedHashSet<HashCollider> values2) {
        PersistentTrieSet<HashCollider> actual = PersistentTrieSet.of();
        HashCollider firstValue1 = values1.iterator().next();
        HashCollider firstValue2 = values2.iterator().next();

        // GIVEN: a set with values1
        actual = actual.withAddAll(values1);

        // WHEN: value1 is in set, then contains must be true
        assertTrue(actual.contains(firstValue1));
        // WHEN: value2 is not in set, then contains must be false
        assertFalse(actual.contains(firstValue2));

    }

    private void testCopyOf(LinkedHashSet<HashCollider> values1, LinkedHashSet<HashCollider> values2) {
        PersistentTrieSet<HashCollider> actual;
        PersistentTrieSet<HashCollider> newActual;

        // WHEN: a set is created with copyOf
        actual = PersistentTrieSet.copyOf(values1);
        //
        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>(values1);
        assertEquality(expected, actual);

        // WHEN: a set is created with copyOf from itself
        newActual = PersistentTrieSet.copyOf(actual);
        assertSame(newActual, actual);
        actual = newActual;
        //
        expected = new LinkedHashSet<>(values1);
        assertEquality(expected, actual);
    }

    private void testEqualsHashCode(LinkedHashSet<HashCollider> values1, LinkedHashSet<HashCollider> values2) {
        assertTrue(values1.size() > 0);
        assertEquals(values1.size(), values2.size());
        HashCollider firstValue2 = values2.iterator().next();

        PersistentTrieSet<HashCollider> actual1a = PersistentTrieSet.copyOf(values1);
        PersistentTrieSet<HashCollider> actual1b = PersistentTrieSet.copyOf(values1);
        PersistentTrieSet<HashCollider> actual2a = PersistentTrieSet.copyOf(values2);
        PersistentTrieSet<HashCollider> actual2b = actual2a.withRemove(firstValue2);
        HashCollider zero = new HashCollider(0, 0);
        LinkedHashSet<HashCollider> expected1 = new LinkedHashSet<>(values1);
        LinkedHashSet<HashCollider> expected1plusZero = new LinkedHashSet<>(values1);
        expected1plusZero.add(zero);

        // some assertions may not make sense, but they are needed for test coverage

        assertEquals(actual1a, actual1a);
        assertEquals(actual1a, actual1b);
        //noinspection AssertBetweenInconvertibleTypes
        assertEquals(actual1a, expected1);
        assertEquals(expected1, actual1a.asSet());
        assertNotEquals(actual1a, actual2a);
        assertNotEquals(actual1a, actual2b);

        assertEquals(expected1.hashCode(), actual1a.hashCode());
        assertNotEquals(actual1a, expected1plusZero);
        assertNotEquals(actual1a, new Object());

    }

    private void testOf1Arg(LinkedHashSet<HashCollider> values1, LinkedHashSet<HashCollider> values2) {
        HashCollider firstValue1 = values1.iterator().next();
        PersistentTrieSet<HashCollider> actual;

        // WHEN: a set is created with 1 value
        actual = PersistentTrieSet.of(firstValue1);
        //
        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>();
        expected.add(firstValue1);
        assertEquality(expected, actual);
    }

    private void testOf2Arg(LinkedHashSet<HashCollider> values1, LinkedHashSet<HashCollider> values2) {
        HashCollider firstValue1 = values1.iterator().next();
        HashCollider firstValue2 = values2.iterator().next();
        PersistentTrieSet<HashCollider> actual;

        // WHEN: a set is created with two distinct values
        actual = PersistentTrieSet.of(firstValue1, firstValue2);
        //
        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>();
        expected.add(firstValue1);
        expected.add(firstValue2);
        assertEquality(expected, actual);

        // WHEN: a set is created with two identical values
        actual = PersistentTrieSet.of(firstValue1, firstValue1);
        //
        expected = new LinkedHashSet<>();
        expected.add(firstValue1);
        assertEquality(expected, actual);
    }

    private void testOfNArg(LinkedHashSet<HashCollider> values1, LinkedHashSet<HashCollider> values2) {
        HashCollider firstValue1 = values1.iterator().next();
        PersistentTrieSet<HashCollider> actual;

        // WHEN: a set is created with identical values
        actual = PersistentTrieSet.of(firstValue1, firstValue1, firstValue1);
        //
        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>();
        expected.add(firstValue1);
        assertEquality(expected, actual);

        // WHEN: a set is created with distinct values
        actual = PersistentTrieSet.of(values1.toArray(new HashCollider[0]));
        //
        expected = new LinkedHashSet<>(values1);
        assertEquality(expected, actual);
    }

    void testPersistentTrieSet(int hashBitMask) {
        // bulkSize must be at least 32 for good code coverage
        int bulkSize = 32;
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
            testWithRemove(values1, values2);
            testWithAddAll(values1, values2);
            testWithRemoveAll(values1, values2);
            testWithRetainAll(values1, values2);
            testContains(values1, values2);
            testOf1Arg(values1, values2);
            testOf2Arg(values1, values2);
            testOfNArg(values1, values2);
            testCopyOf(values1, values2);
            testEqualsHashCode(values1, values2);
        }
    }

    private void testWithAdd(LinkedHashSet<HashCollider> values1, LinkedHashSet<HashCollider> values2) {
        HashCollider firstValue1 = values1.iterator().next();
        HashCollider firstValue2 = values2.iterator().next();
        PersistentTrieSet<HashCollider> actual = PersistentTrieSet.of();
        PersistentTrieSet<HashCollider> newActual;

        // GIVEN: a set with values1
        newActual = actual.withAddAll(values1);
        assertNotSame(newActual, actual);
        actual = newActual;

        // WHEN: value1 is already in set, then withAdd must yield the same set
        newActual = actual.withAdd(firstValue1);
        assertSame(newActual, actual);

        // WHEN: value2 is not yet in set, then withAdd must yield a new set
        newActual = actual.withAdd(firstValue2);
        assertNotSame(newActual, actual);
        actual = newActual;

        //
        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>(values1);
        expected.add(firstValue2);
        assertEquality(expected, actual);
    }

    private void testWithAddAll(LinkedHashSet<HashCollider> values1, LinkedHashSet<HashCollider> values2) {
        PersistentTrieSet<HashCollider> actual = PersistentTrieSet.of();
        PersistentTrieSet<HashCollider> newActual;

        // GIVEN: a set with values1
        newActual = actual.withAddAll(values1);
        assertNotSame(newActual, actual);
        actual = newActual;

        // WHEN: values1 are already in set, then withAddAll must yield the same set
        newActual = actual.withAddAll(values1);
        assertSame(newActual, actual);

        // WHEN: values2 are not yet in set, then withAddAll must yield a new set
        newActual = actual.withAddAll(values2);
        assertNotSame(newActual, actual);
        actual = newActual;

        //
        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>();
        expected.addAll(values1);
        expected.addAll(values2);
        assertEquality(expected, actual);
    }

    private void testWithRemove(LinkedHashSet<HashCollider> values1, LinkedHashSet<HashCollider> values2) {
        HashCollider firstValue1 = values1.iterator().next();
        HashCollider firstValue2 = values2.iterator().next();
        PersistentTrieSet<HashCollider> actual = PersistentTrieSet.of();
        PersistentTrieSet<HashCollider> newActual;

        // GIVEN: a set with values1
        newActual = actual.withAddAll(values1);
        assertNotSame(newActual, actual);
        actual = newActual;

        // WHEN: value1 is in set, then withRemove must yield a new set
        newActual = actual.withRemove(firstValue1);
        assertNotSame(newActual, actual);
        actual = newActual;

        // WHEN: value2 is not in set, then withRemove must yield the same set
        newActual = actual.withRemove(firstValue2);
        assertSame(newActual, actual);

        //
        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>(values1);
        expected.remove(firstValue1);
        assertEquality(expected, actual);
    }

    private void testWithRemoveAll(LinkedHashSet<HashCollider> values1, LinkedHashSet<HashCollider> values2) {
        PersistentTrieSet<HashCollider> actual = PersistentTrieSet.of();
        PersistentTrieSet<HashCollider> newActual;

        // GIVEN: a set with values1
        newActual = actual.withAddAll(values1);
        assertNotSame(newActual, actual);
        actual = newActual;

        // WHEN: values2 are not in set, then withRemoveAll must yield the same set
        newActual = actual.withRemoveAll(values2);
        assertSame(newActual, actual);

        // WHEN: values1 are in set, then withRemoveAll must yield a new set
        newActual = actual.withRemoveAll(values1);
        assertNotSame(newActual, actual);
        actual = newActual;

        //
        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>();
        assertEquality(expected, actual);
    }

    private void testWithRetainAll(LinkedHashSet<HashCollider> values1, LinkedHashSet<HashCollider> values2) {
        PersistentTrieSet<HashCollider> actual = PersistentTrieSet.of();
        PersistentTrieSet<HashCollider> newActual;

        // GIVEN: a set with values1
        newActual = actual.withAddAll(values1);
        assertNotSame(newActual, actual);
        actual = newActual;

        // WHEN: values1 are in set, then withRetainAll must yield the same set
        newActual = actual.withRetainAll(values1);
        assertSame(newActual, actual);

        // WHEN: values2 are not in set, then withRetainAll must yield a new set
        newActual = actual.withRetainAll(values2);
        assertNotSame(newActual, actual);
        actual = newActual;

        //
        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>();
        assertEquality(expected, actual);
    }
}