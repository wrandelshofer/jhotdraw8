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

    private void testContains(LinkedHashSet<HashCollider> entries1, LinkedHashSet<HashCollider> entries2) {
        PersistentTrieSet<HashCollider> actual = PersistentTrieSet.of();
        HashCollider firstValue1 = entries1.iterator().next();
        HashCollider firstValue2 = entries2.iterator().next();

        // GIVEN: a set with entries1
        actual = actual.withAddAll(entries1);

        // WHEN: entry1 is in set, then contains must be true
        assertTrue(actual.contains(firstValue1));
        // WHEN: entry2 is not in set, then contains must be false
        assertFalse(actual.contains(firstValue2));

    }

    private void testCopyOf(LinkedHashSet<HashCollider> entries1, LinkedHashSet<HashCollider> entries2) {
        PersistentTrieSet<HashCollider> actual;
        PersistentTrieSet<HashCollider> newActual;

        // WHEN: a set is created with copyOf
        actual = PersistentTrieSet.copyOf(entries1);
        //
        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>(entries1);
        assertEquality(expected, actual);

        // WHEN: a set is created with copyOf from itself
        newActual = PersistentTrieSet.copyOf(actual);
        assertSame(newActual, actual);
        actual = newActual;
        //
        expected = new LinkedHashSet<>(entries1);
        assertEquality(expected, actual);
    }

    private void testEqualsHashCode(LinkedHashSet<HashCollider> entries1, LinkedHashSet<HashCollider> entries2) {
        assertTrue(entries1.size() > 0);
        assertEquals(entries1.size(), entries2.size());
        HashCollider firstValue1 = entries1.iterator().next();
        HashCollider firstValue2 = entries2.iterator().next();

        PersistentTrieSet<HashCollider> actual1a = PersistentTrieSet.copyOf(entries1);
        assertEquals(actual1a, actual1a);//equals of itself

        PersistentTrieSet<HashCollider> actual1b = PersistentTrieSet.copyOf(entries1);
        assertEquals(actual1a, actual1b);//equals of a new set that does not share trie nodes

        PersistentTrieSet<HashCollider> actual1c = actual1a;
        actual1c = actual1c.withRemove(firstValue1);
        actual1c = actual1c.withAdd(firstValue1);
        assertEquals(actual1a, actual1c);// equals of a new set that shares many trie nodes


        PersistentTrieSet<HashCollider> actual2a = PersistentTrieSet.copyOf(entries2);
        PersistentTrieSet<HashCollider> actual2b = actual2a.withRemove(firstValue2);
        HashCollider zero = new HashCollider(0, 0);
        LinkedHashSet<HashCollider> expected1 = new LinkedHashSet<>(entries1);
        LinkedHashSet<HashCollider> expected1plusZero = new LinkedHashSet<>(entries1);
        expected1plusZero.add(zero);

        // some assertions may not make sense, but they are needed for test coverage

        //noinspection AssertBetweenInconvertibleTypes
        assertEquals(actual1a, expected1);
        assertEquals(expected1, actual1a.asSet());
        assertNotEquals(actual1a, actual2a);
        assertNotEquals(actual1a, actual2b);

        assertEquals(expected1.hashCode(), actual1a.hashCode());
        assertNotEquals(actual1a, expected1plusZero);
        assertNotEquals(actual1a, new Object());

    }

    private void testOf(LinkedHashSet<HashCollider> entries1, LinkedHashSet<HashCollider> entries2) {
        HashCollider firstValue1 = entries1.iterator().next();
        PersistentTrieSet<HashCollider> actual;

        // WHEN: a set is created with identical values
        actual = PersistentTrieSet.of(firstValue1, firstValue1, firstValue1);
        //
        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>();
        expected.add(firstValue1);
        assertEquality(expected, actual);

        // WHEN: a set is created with distinct values
        actual = PersistentTrieSet.of(entries1.toArray(new HashCollider[0]));
        //
        expected = new LinkedHashSet<>(entries1);
        assertEquality(expected, actual);
    }

    void testPersistentTrieSet(int hashBitMask) {
        // bulkSize must be at least 32 for good code coverage
        int bulkSize = 32;
        Random rng = new Random(0);
        for (int i = 0; i < 64; i++) {
            // entries1, entries2 are distinct sets of values
            LinkedHashSet<HashCollider> entries1 = new LinkedHashSet<>();
            LinkedHashSet<HashCollider> entries2 = new LinkedHashSet<>();
            while (entries1.size() < bulkSize) {
                entries1.add(new HashCollider(rng.nextInt(), hashBitMask));
            }
            while (entries2.size() < bulkSize) {
                HashCollider e = new HashCollider(rng.nextInt(), hashBitMask);
                if (!entries1.contains(e)) {
                    entries2.add(e);
                }
            }

            testWithAdd(entries1, entries2);
            testWithRemove(entries1, entries2);
            testWithAddAll(entries1, entries2);
            testWithRemoveAll(entries1, entries2);
            testWithRetainAll(entries1, entries2);
            testContains(entries1, entries2);
            testOf(entries1, entries2);
            testCopyOf(entries1, entries2);
            testEqualsHashCode(entries1, entries2);
        }
    }

    private void testWithAdd(LinkedHashSet<HashCollider> entries1, LinkedHashSet<HashCollider> entries2) {
        HashCollider firstValue1 = entries1.iterator().next();
        HashCollider firstValue2 = entries2.iterator().next();
        PersistentTrieSet<HashCollider> actual = PersistentTrieSet.of();
        PersistentTrieSet<HashCollider> newActual;

        // GIVEN: a set with entries1
        newActual = actual.withAddAll(entries1);
        assertNotSame(newActual, actual);
        actual = newActual;

        // WHEN: entry1 is already in set, then withAdd must yield the same set
        newActual = actual.withAdd(firstValue1);
        assertSame(newActual, actual);

        // WHEN: entry2 is not yet in set, then withAdd must yield a new set
        newActual = actual.withAdd(firstValue2);
        assertNotSame(newActual, actual);
        actual = newActual;

        //
        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>(entries1);
        expected.add(firstValue2);
        assertEquality(expected, actual);
    }

    private void testWithAddAll(LinkedHashSet<HashCollider> entries1, LinkedHashSet<HashCollider> entries2) {
        PersistentTrieSet<HashCollider> actual = PersistentTrieSet.of();
        PersistentTrieSet<HashCollider> newActual;

        // GIVEN: a set with entries1
        newActual = actual.withAddAll(entries1);
        assertNotSame(newActual, actual);
        actual = newActual;

        // WHEN: entries1 are already in set, then withAddAll must yield the same set
        newActual = actual.withAddAll(entries1);
        assertSame(newActual, actual);

        // WHEN: entries2 are not yet in set, then withAddAll must yield a new set
        newActual = actual.withAddAll(entries2);
        assertNotSame(newActual, actual);
        actual = newActual;

        //
        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>();
        expected.addAll(entries1);
        expected.addAll(entries2);
        assertEquality(expected, actual);
    }

    private void testWithRemove(LinkedHashSet<HashCollider> entries1, LinkedHashSet<HashCollider> entries2) {
        HashCollider firstValue1 = entries1.iterator().next();
        HashCollider firstValue2 = entries2.iterator().next();
        PersistentTrieSet<HashCollider> actual = PersistentTrieSet.of();
        PersistentTrieSet<HashCollider> newActual;

        // GIVEN: a set with entries1
        newActual = actual.withAddAll(entries1);
        assertNotSame(newActual, actual);
        actual = newActual;

        // WHEN: entry1 is in set, then withRemove must yield a new set
        newActual = actual.withRemove(firstValue1);
        assertNotSame(newActual, actual);
        actual = newActual;

        // WHEN: entry2 is not in set, then withRemove must yield the same set
        newActual = actual.withRemove(firstValue2);
        assertSame(newActual, actual);

        //
        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>(entries1);
        expected.remove(firstValue1);
        assertEquality(expected, actual);
    }

    private void testWithRemoveAll(LinkedHashSet<HashCollider> entries1, LinkedHashSet<HashCollider> entries2) {
        PersistentTrieSet<HashCollider> actual = PersistentTrieSet.of();
        PersistentTrieSet<HashCollider> newActual;

        // GIVEN: a set with entries1
        newActual = actual.withAddAll(entries1);
        assertNotSame(newActual, actual);
        actual = newActual;

        // WHEN: entries2 are not in set, then withRemoveAll must yield the same set
        newActual = actual.withRemoveAll(entries2);
        assertSame(newActual, actual);

        // WHEN: entries1 are in set, then withRemoveAll must yield a new set
        newActual = actual.withRemoveAll(entries1);
        assertNotSame(newActual, actual);
        actual = newActual;

        //
        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>();
        assertEquality(expected, actual);
    }

    private void testWithRetainAll(LinkedHashSet<HashCollider> entries1, LinkedHashSet<HashCollider> entries2) {
        PersistentTrieSet<HashCollider> actual = PersistentTrieSet.of();
        PersistentTrieSet<HashCollider> newActual;

        // GIVEN: a set with entries1
        newActual = actual.withAddAll(entries1);
        assertNotSame(newActual, actual);
        actual = newActual;

        // WHEN: entries1 are in set, then withRetainAll must yield the same set
        newActual = actual.withRetainAll(entries1);
        assertSame(newActual, actual);

        // WHEN: entries2 are not in set, then withRetainAll must yield a new set
        newActual = actual.withRetainAll(entries2);
        assertNotSame(newActual, actual);
        actual = newActual;

        //
        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>();
        assertEquality(expected, actual);
    }
}