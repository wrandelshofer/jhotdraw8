/*
 * @(#)AbstractPersistentMapTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
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

public abstract class AbstractPersistentMapTest {
    protected abstract @NonNull PersistentMap<HashCollider, HashCollider> of();

    @SuppressWarnings("unchecked")
    protected abstract @NonNull PersistentMap<HashCollider, HashCollider> of(@NonNull Map.Entry<HashCollider, HashCollider>... entries);

    protected abstract @NonNull PersistentMap<HashCollider, HashCollider> copyOf(@NonNull Map<? extends HashCollider, ? extends HashCollider> map);

    protected abstract @NonNull PersistentMap<HashCollider, HashCollider> copyOf(@NonNull ReadOnlyMap<? extends HashCollider, ? extends HashCollider> map);


    protected abstract @NonNull PersistentMap<HashCollider, HashCollider> copyOf(@NonNull Iterable<? extends Map.Entry<? extends HashCollider, ? extends HashCollider>> entries);


    private void assertEquality(LinkedHashMap<HashCollider, HashCollider> expected, PersistentMap<HashCollider, HashCollider> actual) {
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
        assertEquals(actual, copyOf(actual.readOnlyEntrySet()));
        assertFalse(actual.toString().isEmpty());
    }

    @TestFactory
    public @NonNull List<DynamicTest> dynamicTests() {
        return Arrays.asList(
                dynamicTest("32-bits hash", () -> testPersistentMap(-1)),
                dynamicTest("3-bits hash", () -> testPersistentMap(7)),
                dynamicTest("0-bits hash", () -> testPersistentMap(0))
        );
    }

    private void testContains(LinkedHashMap<HashCollider, HashCollider> values1, LinkedHashMap<HashCollider, HashCollider> values2) {
        PersistentMap<HashCollider, HashCollider> actual = of();
        Map.Entry<HashCollider, HashCollider> firstValue1 = values1.entrySet().iterator().next();
        Map.Entry<HashCollider, HashCollider> firstValue2 = values2.entrySet().iterator().next();

        // GIVEN: a set with values1
        actual = actual.copyPutAll(values1);

        // WHEN: value1 is in set, then contains must be true
        assertTrue(actual.readOnlyEntrySet().contains(firstValue1));
        // WHEN: value2 is not in set, then contains must be false
        assertFalse(actual.readOnlyEntrySet().contains(firstValue2));

    }

    private void testCopyOf(LinkedHashMap<HashCollider, HashCollider> values1, LinkedHashMap<HashCollider, HashCollider> values2) {
        PersistentMap<HashCollider, HashCollider> actual;
        PersistentMap<HashCollider, HashCollider> newActual;

        // WHEN: a set is created with copyOf a java.util.Map
        actual = copyOf(values1);
        LinkedHashMap<HashCollider, HashCollider> expected = new LinkedHashMap<>(values1);
        assertEquality(expected, actual);

        // WHEN: a set is created with copyOf a ImmutableMap
        ImmutableMap<HashCollider, HashCollider> immutableExpected = ImmutableMaps.copyOf(expected);
        actual = copyOf(immutableExpected);
        assertEquals(immutableExpected, actual);

        // WHEN: a set is created with copyOf from itself
        newActual = copyOf(actual);
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

        PersistentMap<HashCollider, HashCollider> actual1a = copyOf(values1);
        assertEquals(actual1a, actual1a);// equals of itself

        PersistentMap<HashCollider, HashCollider> actual1b = copyOf(values1);
        assertEquals(actual1a, actual1b);// equals of a new map that does not share trie nodes

        PersistentMap<HashCollider, HashCollider> actual1c = actual1a;
        actual1c = actual1c.copyRemove(firstValue1.getKey());
        actual1c = actual1c.copyPut(firstValue1.getKey(), firstValue1.getValue());
        assertEquals(actual1a, actual1c);// equals of a new map that shares many trie nodes

        PersistentMap<HashCollider, HashCollider> actual2a = copyOf(values2);
        PersistentMap<HashCollider, HashCollider> actual2b = actual2a.copyRemove(firstValue2.getKey());
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
        PersistentMap<HashCollider, HashCollider> actual1WithDifferentValues = copyOf(values1WithDifferentValues);
        assertNotEquals(actual1a, actual1WithDifferentValues);
        assertNotEquals(actual1a, values1WithDifferentValues);
        assertNotEquals(actual1a, values2WithDifferentValues);
    }

    @SuppressWarnings("unchecked")
    private void testOfEntries(LinkedHashMap<HashCollider, HashCollider> values1, LinkedHashMap<HashCollider, HashCollider> values2) {
        Map.Entry<HashCollider, HashCollider> firstValue1 = values1.entrySet().iterator().next();
        PersistentMap<HashCollider, HashCollider> actual;

        // WHEN: a set is created with identical values
        actual = copyOf(
                ImmutableMaps.of(firstValue1.getKey(), firstValue1.getValue(),
                        firstValue1.getKey(), firstValue1.getValue(),
                        firstValue1.getKey(), firstValue1.getValue()).readOnlyEntrySet());
        //
        LinkedHashMap<HashCollider, HashCollider> expected = new LinkedHashMap<>();
        expected.put(firstValue1.getKey(), firstValue1.getValue());
        assertEquality(expected, actual);

        // WHEN: a set is created with distinct values
        actual = copyOf(values1.entrySet());
        //
        expected = new LinkedHashMap<>(values1);
        assertEquality(expected, actual);
    }

    void testPersistentMap(int hashBitMask) {
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

            testCopyPut(values1, values2, hashBitMask);
            testCopyRemove(values1, values2);
            testCopyPutAll(values1, values2, hashBitMask);
            testCopyRemoveAll(values1, values2);
            testCopyRetainAll(values1, values2);
            testContains(values1, values2);
            testOfEntries(values1, values2);
            testCopyOf(values1, values2);
            testEqualsHashCode(values1, values2, hashBitMask);
        }
    }

    private void testCopyPut(LinkedHashMap<HashCollider, HashCollider> values1, LinkedHashMap<HashCollider, HashCollider> values2, int hashBitMask) {
        Map.Entry<HashCollider, HashCollider> firstValue1 = values1.entrySet().iterator().next();
        Map.Entry<HashCollider, HashCollider> firstValue2 = values2.entrySet().iterator().next();
        PersistentMap<HashCollider, HashCollider> actual = of();
        PersistentMap<HashCollider, HashCollider> newActual;
        LinkedHashMap<HashCollider, HashCollider> expected = new LinkedHashMap<>();

        // GIVEN: a set with values1
        for (Map.Entry<HashCollider, HashCollider> entry : values1.entrySet()) {
            actual = actual.copyPut(entry.getKey(), entry.getValue());
            expected.put(entry.getKey(), entry.getValue());
            assertEquality(expected, actual);
        }

        // WHEN: value1 is already in set, then copyPut must yield the same map
        newActual = actual.copyPut(firstValue1.getKey(), firstValue1.getValue());
        assertSame(newActual, actual);

        // WHEN: value1 is updated in set, then copyPut must yield a new map
        HashCollider newValue1 = new HashCollider(firstValue1.getValue() == null ? -1 : firstValue1.getValue().getValue() + 1, hashBitMask);
        newActual = actual.copyPut(firstValue1.getKey(), newValue1);
        assertNotSame(newActual, actual);
        actual = newActual;

        // WHEN: value2 is not yet in set, then copyPut must yield a new map
        newActual = actual.copyPut(firstValue2.getKey(), firstValue2.getValue());
        assertNotSame(newActual, actual);
        actual = newActual;

        //
        expected.put(firstValue1.getKey(), newValue1);
        expected.put(firstValue2.getKey(), firstValue2.getValue());
        assertEquality(expected, actual);
    }

    private void testCopyPutAll(LinkedHashMap<HashCollider, HashCollider> values1, LinkedHashMap<HashCollider, HashCollider> values2, int hashBitMask) {
        PersistentMap<HashCollider, HashCollider> actual = of();
        PersistentMap<HashCollider, HashCollider> newActual;

        // GIVEN: a set with values1
        newActual = actual.copyPutAll(values1);
        assertNotSame(newActual, actual);
        actual = newActual;

        // WHEN: values1 are already in set, then copyPutAll must yield the same map
        newActual = actual.copyPutAll(values1);
        assertSame(newActual, actual);

        // WHEN: values1 are updated in set, then copyPutAll must yield a new map
        LinkedHashMap<HashCollider, HashCollider> values1WithDifferentValues = new LinkedHashMap<>();
        for (Map.Entry<HashCollider, HashCollider> entry : values1.entrySet()) {
            values1WithDifferentValues.put(entry.getKey(),
                    new HashCollider(entry.getValue() == null ? -1 : entry.getValue().getValue() + 1, hashBitMask));
        }
        newActual = actual.copyPutAll(values1WithDifferentValues);
        assertNotSame(newActual, actual);


        // WHEN: values2 are not yet in set, then copyPutAll must yield a new map
        newActual = actual.copyPutAll(values2);
        assertNotSame(newActual, actual);
        actual = newActual;

        //
        LinkedHashMap<HashCollider, HashCollider> expected = new LinkedHashMap<>();
        expected.putAll(values1);
        expected.putAll(values2);
        assertEquality(expected, actual);
    }

    private void testCopyRemove(LinkedHashMap<HashCollider, HashCollider> values1, LinkedHashMap<HashCollider, HashCollider> values2) {
        Map.Entry<HashCollider, HashCollider> firstValue1 = values1.entrySet().iterator().next();
        Map.Entry<HashCollider, HashCollider> firstValue2 = values2.entrySet().iterator().next();
        PersistentMap<HashCollider, HashCollider> actual = of();
        PersistentMap<HashCollider, HashCollider> newActual;

        // GIVEN: a set with values1
        newActual = actual.copyPutAll(values1);
        assertNotSame(newActual, actual);
        actual = newActual;

        // WHEN: value1 is in set, then withRemove must yield a new map
        newActual = actual.copyRemove(firstValue1.getKey());
        assertNotSame(newActual, actual);
        actual = newActual;

        // WHEN: value2 is not in set, then withRemove must yield the same map
        newActual = actual.copyRemove(firstValue2.getKey());
        assertSame(newActual, actual);

        //
        LinkedHashMap<HashCollider, HashCollider> expected = new LinkedHashMap<>(values1);
        expected.remove(firstValue1.getKey());
        assertEquality(expected, actual);
    }

    private void testCopyRemoveAll(LinkedHashMap<HashCollider, HashCollider> values1, LinkedHashMap<HashCollider, HashCollider> values2) {
        PersistentMap<HashCollider, HashCollider> actual = of();
        PersistentMap<HashCollider, HashCollider> newActual;

        // GIVEN: a set with values1
        newActual = actual.copyPutAll(values1);
        assertNotSame(newActual, actual);
        actual = newActual;

        // WHEN: values2 are not in set, then withRemoveAll must yield the same map
        newActual = actual.copyRemoveAll(values2.keySet());
        assertSame(newActual, actual);

        // WHEN: values1 are in set, then withRemoveAll must yield a new map
        newActual = actual.copyRemoveAll(values1.keySet());
        assertNotSame(newActual, actual);
        actual = newActual;

        //
        LinkedHashMap<HashCollider, HashCollider> expected = new LinkedHashMap<>();
        assertEquality(expected, actual);
    }

    private void testCopyRetainAll(LinkedHashMap<HashCollider, HashCollider> values1, LinkedHashMap<HashCollider, HashCollider> values2) {
        PersistentMap<HashCollider, HashCollider> actual = of();
        PersistentMap<HashCollider, HashCollider> newActual;

        // GIVEN: a set with values1
        newActual = actual.copyPutAll(values1);
        assertNotSame(newActual, actual);
        actual = newActual;

        // WHEN: values1 are in set, then withRetainAll must yield the same map
        newActual = actual.copyRetainAll(values1.keySet());
        assertSame(newActual, actual);

        // WHEN: values2 are not in set, then withRetainAll must yield a new map
        newActual = actual.copyRetainAll(values2.keySet());
        assertNotSame(newActual, actual);
        actual = newActual;

        //
        LinkedHashMap<HashCollider, HashCollider> expected = new LinkedHashMap<>();
        assertEquality(expected, actual);
    }

    @TestFactory
    public @NonNull List<DynamicTest> dynamicTestNullKeyNullValue() {
        return Arrays.asList(
                dynamicTest("null key", () -> testNullKeyNullValue(null, new HashCollider(5, -1))),
                dynamicTest("null value", () -> testNullKeyNullValue(new HashCollider(5, -1), null)),
                dynamicTest("null key, null value", () -> testNullKeyNullValue(null, null))
        );
    }

    public void testNullKeyNullValue(@Nullable HashCollider key, @Nullable HashCollider value) {
        @SuppressWarnings("unchecked") PersistentMap<HashCollider, HashCollider> set = of(ImmutableMaps.entry(key, value));
        LinkedHashMap<HashCollider, HashCollider> expected = new LinkedHashMap<>();
        expected.put(key, value);
        assertTrue(set.containsKey(key));
        assertEquals(expected.toString(), set.toString());
        assertEquals(expected, set.asMap());

        expected.remove(key);
        set = set.copyRemove(key);
        assertEquals(expected, set.asMap());
    }


}