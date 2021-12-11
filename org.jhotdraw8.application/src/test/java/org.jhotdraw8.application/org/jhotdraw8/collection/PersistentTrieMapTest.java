package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

class PersistentTrieMapTest {
    private void assertEquality(LinkedHashMap<HashCollider, HashCollider> expected, PersistentTrieMap<HashCollider, HashCollider> actual) {
        //noinspection EqualsBetweenInconvertibleTypes
        assertTrue(actual.equals(expected));
        assertEquals(actual, actual);
        //noinspection ConstantConditions
        assertFalse(actual.equals(null));
        assertEquals(expected, actual.asMap());
        assertEquals(expected.hashCode(), actual.hashCode());
        assertEquals(expected.size(), actual.size());
        assertEquals(expected.isEmpty(), actual.isEmpty());
        //noinspection unchecked
        assertEquals(actual, PersistentTrieMap.ofEntries(actual.readOnlyEntrySet()));
        assertFalse(actual.toString().isEmpty());
    }

    @TestFactory
    public @NonNull List<DynamicTest> dynamicTests() {
        return Arrays.asList(
                dynamicTest("32-bits hash", () -> testPersistentTrieMap(-1)),
                dynamicTest("3-bits hash", () -> testPersistentTrieMap(7)),
                dynamicTest("0-bits hash", () -> testPersistentTrieMap(0))
        );
    }

    private void testContains(LinkedHashMap<HashCollider, HashCollider> values1, LinkedHashMap<HashCollider, HashCollider> values2) {
        PersistentTrieMap<HashCollider, HashCollider> actual = PersistentTrieMap.of();
        Map.Entry<HashCollider, HashCollider> firstValue1 = values1.entrySet().iterator().next();
        Map.Entry<HashCollider, HashCollider> firstValue2 = values2.entrySet().iterator().next();

        // GIVEN: a set with values1
        actual = actual.withPutAll(values1);

        // WHEN: value1 is in set, then contains must be true
        assertTrue(actual.readOnlyEntrySet().contains(firstValue1));
        // WHEN: value2 is not in set, then contains must be false
        assertFalse(actual.readOnlyEntrySet().contains(firstValue2));

    }

    private void testCopyOf(LinkedHashMap<HashCollider, HashCollider> values1, LinkedHashMap<HashCollider, HashCollider> values2) {
        PersistentTrieMap<HashCollider, HashCollider> actual;
        PersistentTrieMap<HashCollider, HashCollider> newActual;

        // WHEN: a set is created with copyOf a java.util.Map
        actual = PersistentTrieMap.copyOf(values1);
        LinkedHashMap<HashCollider, HashCollider> expected = new LinkedHashMap<>(values1);
        assertEquality(expected, actual);

        // WHEN: a set is created with copyOf a ImmutableMap
        ImmutableMap<HashCollider, HashCollider> immutableExpected = ImmutableMaps.copyOf(expected);
        actual = PersistentTrieMap.copyOf(immutableExpected);
        assertEquals(immutableExpected, actual);

        // WHEN: a set is created with copyOf from itself
        newActual = PersistentTrieMap.copyOf(actual);
        assertSame(newActual, actual);
        actual = newActual;
        //
        expected = new LinkedHashMap<>(values1);
        assertEquality(expected, actual);
    }

    private void testEqualsHashCode(LinkedHashMap<HashCollider, HashCollider> values1, LinkedHashMap<HashCollider, HashCollider> values2, int hashBitMask) {
        assertTrue(values1.size() > 0);
        assertEquals(values1.size(), values2.size());
        Map.Entry<HashCollider, HashCollider> firstValue1 = values1.entrySet().iterator().next();
        Map.Entry<HashCollider, HashCollider> firstValue2 = values2.entrySet().iterator().next();

        PersistentTrieMap<HashCollider, HashCollider> actual1a = PersistentTrieMap.copyOf(values1);
        assertEquals(actual1a, actual1a);// equals of itself

        PersistentTrieMap<HashCollider, HashCollider> actual1b = PersistentTrieMap.copyOf(values1);
        assertEquals(actual1a, actual1b);// equals of a new map that does not share trie nodes

        PersistentTrieMap<HashCollider, HashCollider> actual1c = actual1a;
        actual1c = actual1c.withRemove(firstValue1.getKey());
        actual1c = actual1c.withPut(firstValue1.getKey(), firstValue1.getValue());
        assertEquals(actual1a, actual1c);// equals of a new map that shares many trie nodes

        PersistentTrieMap<HashCollider, HashCollider> actual2a = PersistentTrieMap.copyOf(values2);
        PersistentTrieMap<HashCollider, HashCollider> actual2b = actual2a.withRemove(firstValue2.getKey());
        HashCollider zero = new HashCollider(0, 0);
        LinkedHashMap<HashCollider, HashCollider> expected1 = new LinkedHashMap<>(values1);
        LinkedHashMap<HashCollider, HashCollider> expected1plusZero = new LinkedHashMap<>(values1);
        expected1plusZero.put(zero, null);

        // some assertions may not make sense, but they are needed for test coverage

        //noinspection AssertBetweenInconvertibleTypes
        assertEquals(actual1a, expected1);
        assertEquals(expected1, actual1a.asMap());
        assertNotEquals(actual1a, actual2a);
        assertNotEquals(actual1a, actual2b);

        assertEquals(expected1.hashCode(), actual1a.hashCode());
        assertNotEquals(actual1a, expected1plusZero);
        assertNotEquals(actual1a, new Object());

        LinkedHashMap<HashCollider, HashCollider> values1WithDifferentValues = new LinkedHashMap<>();
        LinkedHashMap<HashCollider, HashCollider> values2WithDifferentValues = new LinkedHashMap<>();
        for (Map.Entry<HashCollider, HashCollider> entry : values1.entrySet()) {
            values1WithDifferentValues.put(entry.getKey(),
                    (entry.getValue() == null) ? new HashCollider(-1, hashBitMask) :
                            new HashCollider(entry.getValue().getValue() + 1, hashBitMask));
        }
        for (Map.Entry<HashCollider, HashCollider> entry : values2.entrySet()) {
            values2WithDifferentValues.put(entry.getKey(),
                    (entry.getValue() == null) ? new HashCollider(-1, hashBitMask) :
                            new HashCollider(entry.getValue().getValue() + 1, hashBitMask));
        }
        PersistentTrieMap<HashCollider, HashCollider> actual1WithDifferentValues = PersistentTrieMap.copyOf(values1WithDifferentValues);
        assertNotEquals(actual1a, actual1WithDifferentValues);
        assertNotEquals(actual1a, values1WithDifferentValues);
        assertNotEquals(actual1a, values2WithDifferentValues);
    }

    @SuppressWarnings("unchecked")
    private void testOfEntries(LinkedHashMap<HashCollider, HashCollider> values1, LinkedHashMap<HashCollider, HashCollider> values2) {
        Map.Entry<HashCollider, HashCollider> firstValue1 = values1.entrySet().iterator().next();
        PersistentTrieMap<HashCollider, HashCollider> actual;

        // WHEN: a set is created with identical values
        actual = PersistentTrieMap.ofEntries(
                ImmutableMaps.of(firstValue1.getKey(), firstValue1.getValue(),
                        firstValue1.getKey(), firstValue1.getValue(),
                        firstValue1.getKey(), firstValue1.getValue()).readOnlyEntrySet());
        //
        LinkedHashMap<HashCollider, HashCollider> expected = new LinkedHashMap<>();
        expected.put(firstValue1.getKey(), firstValue1.getValue());
        assertEquality(expected, actual);

        // WHEN: a set is created with distinct values
        actual = PersistentTrieMap.ofEntries(values1.entrySet());
        //
        expected = new LinkedHashMap<>(values1);
        assertEquality(expected, actual);
    }

    void testPersistentTrieMap(int hashBitMask) {
        // bulkSize must be at least 32 for good code coverage
        int bulkSize = 32;
        Random rng = new Random(0);
        for (int i = 0; i < 64; i++) {
            // values1, values2 are distinct sets of values
            LinkedHashMap<HashCollider, HashCollider> values1 = new LinkedHashMap<>();
            LinkedHashMap<HashCollider, HashCollider> values2 = new LinkedHashMap<>();
            if (rng.nextBoolean()) {
                values1.put(new HashCollider(Integer.MIN_VALUE, hashBitMask), null);
            } else {
                values2.put(new HashCollider(Integer.MIN_VALUE, hashBitMask), null);
            }
            while (values1.size() < bulkSize) {
                int value = rng.nextInt();
                if (value == Integer.MIN_VALUE) {
                    continue;
                }
                values1.put(new HashCollider(value, hashBitMask), new HashCollider(value, hashBitMask));
            }
            while (values2.size() < bulkSize) {
                int value = rng.nextInt();
                HashCollider e = new HashCollider(value, hashBitMask);
                if (!values1.containsKey(e)) {
                    values2.put(e, new HashCollider(value, hashBitMask));
                }
            }

            testWithPut(values1, values2, hashBitMask);
            testWithRemove(values1, values2);
            testWithPutAll(values1, values2, hashBitMask);
            testWithRemoveAll(values1, values2);
            testWithRetainAll(values1, values2);
            testContains(values1, values2);
            testOfEntries(values1, values2);
            testCopyOf(values1, values2);
            testEqualsHashCode(values1, values2, hashBitMask);
        }
    }

    private void testWithPut(LinkedHashMap<HashCollider, HashCollider> values1, LinkedHashMap<HashCollider, HashCollider> values2, int hashBitMask) {
        Map.Entry<HashCollider, HashCollider> firstValue1 = values1.entrySet().iterator().next();
        Map.Entry<HashCollider, HashCollider> firstValue2 = values2.entrySet().iterator().next();
        PersistentTrieMap<HashCollider, HashCollider> actual = PersistentTrieMap.of();
        PersistentTrieMap<HashCollider, HashCollider> newActual;

        // GIVEN: a set with values1
        newActual = actual.withPutAll(values1);
        assertNotSame(newActual, actual);
        actual = newActual;

        // WHEN: value1 is already in set, then withAdd must yield the same map
        newActual = actual.withPut(firstValue1.getKey(), firstValue1.getValue());
        assertSame(newActual, actual);

        // WHEN: value1 is updated in set, then withAdd must yield a new map
        newActual = actual.withPut(firstValue1.getKey(),
                new HashCollider(firstValue1.getValue() == null ? -1 : firstValue1.getValue().getValue() + 1, hashBitMask));
        assertNotSame(newActual, actual);

        // WHEN: value2 is not yet in set, then withAdd must yield a new map
        newActual = actual.withPut(firstValue2.getKey(), firstValue2.getValue());
        assertNotSame(newActual, actual);
        actual = newActual;

        //
        LinkedHashMap<HashCollider, HashCollider> expected = new LinkedHashMap<>(values1);
        expected.put(firstValue2.getKey(), firstValue2.getValue());
        assertEquality(expected, actual);
    }

    private void testWithPutAll(LinkedHashMap<HashCollider, HashCollider> values1, LinkedHashMap<HashCollider, HashCollider> values2, int hashBitMask) {
        PersistentTrieMap<HashCollider, HashCollider> actual = PersistentTrieMap.of();
        PersistentTrieMap<HashCollider, HashCollider> newActual;

        // GIVEN: a set with values1
        newActual = actual.withPutAll(values1);
        assertNotSame(newActual, actual);
        actual = newActual;

        // WHEN: values1 are already in set, then withPutAll must yield the same map
        newActual = actual.withPutAll(values1);
        assertSame(newActual, actual);

        // WHEN: values1 are updated in set, then withPutAll must yield a new map
        LinkedHashMap<HashCollider, HashCollider> values1WithDifferentValues = new LinkedHashMap<>();
        for (Map.Entry<HashCollider, HashCollider> entry : values1.entrySet()) {
            values1WithDifferentValues.put(entry.getKey(),
                    new HashCollider(entry.getValue() == null ? -1 : entry.getValue().getValue() + 1, hashBitMask));
        }
        newActual = actual.withPutAll(values1WithDifferentValues);
        assertNotSame(newActual, actual);


        // WHEN: values2 are not yet in set, then withPutAll must yield a new map
        newActual = actual.withPutAll(values2);
        assertNotSame(newActual, actual);
        actual = newActual;

        //
        LinkedHashMap<HashCollider, HashCollider> expected = new LinkedHashMap<>();
        expected.putAll(values1);
        expected.putAll(values2);
        assertEquality(expected, actual);
    }

    private void testWithRemove(LinkedHashMap<HashCollider, HashCollider> values1, LinkedHashMap<HashCollider, HashCollider> values2) {
        Map.Entry<HashCollider, HashCollider> firstValue1 = values1.entrySet().iterator().next();
        Map.Entry<HashCollider, HashCollider> firstValue2 = values2.entrySet().iterator().next();
        PersistentTrieMap<HashCollider, HashCollider> actual = PersistentTrieMap.of();
        PersistentTrieMap<HashCollider, HashCollider> newActual;

        // GIVEN: a set with values1
        newActual = actual.withPutAll(values1);
        assertNotSame(newActual, actual);
        actual = newActual;

        // WHEN: value1 is in set, then withRemove must yield a new map
        newActual = actual.withRemove(firstValue1.getKey());
        assertNotSame(newActual, actual);
        actual = newActual;

        // WHEN: value2 is not in set, then withRemove must yield the same map
        newActual = actual.withRemove(firstValue2.getKey());
        assertSame(newActual, actual);

        //
        LinkedHashMap<HashCollider, HashCollider> expected = new LinkedHashMap<>(values1);
        expected.remove(firstValue1.getKey());
        assertEquality(expected, actual);
    }

    private void testWithRemoveAll(LinkedHashMap<HashCollider, HashCollider> values1, LinkedHashMap<HashCollider, HashCollider> values2) {
        PersistentTrieMap<HashCollider, HashCollider> actual = PersistentTrieMap.of();
        PersistentTrieMap<HashCollider, HashCollider> newActual;

        // GIVEN: a set with values1
        newActual = actual.withPutAll(values1);
        assertNotSame(newActual, actual);
        actual = newActual;

        // WHEN: values2 are not in set, then withRemoveAll must yield the same map
        newActual = actual.withRemoveAll(values2.keySet());
        assertSame(newActual, actual);

        // WHEN: values1 are in set, then withRemoveAll must yield a new map
        newActual = actual.withRemoveAll(values1.keySet());
        assertNotSame(newActual, actual);
        actual = newActual;

        //
        LinkedHashMap<HashCollider, HashCollider> expected = new LinkedHashMap<>();
        assertEquality(expected, actual);
    }

    private void testWithRetainAll(LinkedHashMap<HashCollider, HashCollider> values1, LinkedHashMap<HashCollider, HashCollider> values2) {
        PersistentTrieMap<HashCollider, HashCollider> actual = PersistentTrieMap.of();
        PersistentTrieMap<HashCollider, HashCollider> newActual;

        // GIVEN: a set with values1
        newActual = actual.withPutAll(values1);
        assertNotSame(newActual, actual);
        actual = newActual;

        // WHEN: values1 are in set, then withRetainAll must yield the same map
        newActual = actual.withRetainAll(values1.keySet());
        assertSame(newActual, actual);

        // WHEN: values2 are not in set, then withRetainAll must yield a new map
        newActual = actual.withRetainAll(values2.keySet());
        assertNotSame(newActual, actual);
        actual = newActual;

        //
        LinkedHashMap<HashCollider, HashCollider> expected = new LinkedHashMap<>();
        assertEquality(expected, actual);
    }
}