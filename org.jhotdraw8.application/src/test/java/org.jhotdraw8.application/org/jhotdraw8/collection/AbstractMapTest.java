/*
 * @(#)AbstractMapTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public abstract class AbstractMapTest {
    protected abstract @NonNull Map<HashCollider, HashCollider> of();

    protected abstract @NonNull Map<HashCollider, HashCollider> copyOf(@NonNull Map<HashCollider, HashCollider> map);

    protected abstract @NonNull Map<HashCollider, HashCollider> copyOf(@NonNull Collection<Map.Entry<HashCollider, HashCollider>> map);


    private void assertMapEquality(Map<HashCollider, HashCollider> expected, Map<HashCollider, HashCollider> actual) {
        //noinspection EqualsBetweenInconvertibleTypes
        assertTrue(actual.equals(expected));
        assertEquals(actual, actual);
        //noinspection ConstantConditions
        assertFalse(actual.equals(null));

        assertEquals(expected, actual);
        assertEquals(expected.hashCode(), actual.hashCode());
        assertEquals(expected.size(), actual.size());
        assertEquals(expected.isEmpty(), actual.isEmpty());
    }

    @TestFactory
    public @NonNull List<DynamicTest> dynamicTests() {
        return Arrays.asList(
                dynamicTest("32-bits hash", () -> testMap(-1)),
                dynamicTest("3-bits hash", () -> testMap(7)),
                dynamicTest("0-bits hash", () -> testMap(0))
        );
    }

    private void testContains(LinkedHashMap<HashCollider, HashCollider> values1, LinkedHashMap<HashCollider, HashCollider> values2) {
        Map<HashCollider, HashCollider> actual = of();
        Map.Entry<HashCollider, HashCollider> firstValue1 = values1.entrySet().iterator().next();
        Map.Entry<HashCollider, HashCollider> firstValue2 = values2.entrySet().iterator().next();

        // GIVEN: a set with values1
        actual.putAll(values1);

        // WHEN: value1 is in set, then contains must be true
        assertTrue(actual.entrySet().contains(firstValue1));
        // WHEN: value2 is not in set, then contains must be false
        assertFalse(actual.entrySet().contains(firstValue2));

    }

    private void testCopyOf(LinkedHashMap<HashCollider, HashCollider> values1, LinkedHashMap<HashCollider, HashCollider> values2) {
        Map<HashCollider, HashCollider> actual;
        Map<HashCollider, HashCollider> newActual;

        // WHEN: a set is created with copyOf a java.util.Map
        actual = copyOf(values1);
        LinkedHashMap<HashCollider, HashCollider> expected = new LinkedHashMap<>(values1);
        assertMapEquality(expected, actual);

        // WHEN: a set is created with copyOf from itself
        newActual = copyOf(actual);
        assertMapEquality(newActual, actual);
        actual = newActual;
        //
        expected = new LinkedHashMap<>(values1);
        assertMapEquality(expected, actual);
    }

    private void testEqualsHashCode(LinkedHashMap<HashCollider, HashCollider> values1, LinkedHashMap<HashCollider, HashCollider> values2, int hashBitMask) {
        assertTrue(values1.size() > 0);
        assertEquals(values1.size(), values2.size());
        Map.Entry<HashCollider, HashCollider> firstValue1 = values1.entrySet().iterator().next();
        Map.Entry<HashCollider, HashCollider> firstValue2 = values2.entrySet().iterator().next();

        Map<HashCollider, HashCollider> actual1a = copyOf(values1);
        assertEquals(actual1a, actual1a);// equals of itself

        Map<HashCollider, HashCollider> actual1b = copyOf(values1);
        assertEquals(actual1a, actual1b);// equals of a new map that does not share trie nodes

        Map<HashCollider, HashCollider> actual1c = actual1a;
        actual1c.remove(firstValue1.getKey());
        actual1c.put(firstValue1.getKey(), firstValue1.getValue());
        assertEquals(actual1a, actual1c);// equals of a new map that shares many trie nodes

        Map<HashCollider, HashCollider> actual2a = copyOf(values2);
        actual2a.remove(firstValue2.getKey());
        HashCollider zero = new HashCollider(0, 0);
        LinkedHashMap<HashCollider, HashCollider> expected1 = new LinkedHashMap<>(values1);
        LinkedHashMap<HashCollider, HashCollider> expected1plusZero = new LinkedHashMap<>(values1);
        expected1plusZero.put(zero, null);

        // some assertions may not make sense, but they are needed for test coverage

        //noinspection AssertBetweenInconvertibleTypes
        assertEquals(actual1a, expected1);
        assertEquals(expected1, actual1a);
        assertNotEquals(actual1a, actual2a);

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
        Map<HashCollider, HashCollider> actual1WithDifferentValues = copyOf(values1WithDifferentValues);
        assertNotEquals(actual1a, actual1WithDifferentValues);
        assertNotEquals(actual1a, values1WithDifferentValues);
        assertNotEquals(actual1a, values2WithDifferentValues);
    }

    @SuppressWarnings("unchecked")
    private void testOfEntries(LinkedHashMap<HashCollider, HashCollider> values1, LinkedHashMap<HashCollider, HashCollider> values2) {
        Map.Entry<HashCollider, HashCollider> firstValue1 = values1.entrySet().iterator().next();
        Map<HashCollider, HashCollider> actual;

        // WHEN: a set is created with identical values
        actual = copyOf(
                MapEntries.linkedHashMap(MapEntries.of(firstValue1.getKey(), firstValue1.getValue(),
                        firstValue1.getKey(), firstValue1.getValue(),
                        firstValue1.getKey(), firstValue1.getValue())));
        //
        LinkedHashMap<HashCollider, HashCollider> expected = new LinkedHashMap<>();
        expected.put(firstValue1.getKey(), firstValue1.getValue());
        assertMapEquality(expected, actual);

        // WHEN: a set is created with distinct values
        actual = copyOf(values1.entrySet());
        //
        expected = new LinkedHashMap<>(values1);
        assertMapEquality(expected, actual);
    }

    void testMap(int hashBitMask) {
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

            testPut(values1, values2, hashBitMask);
            testRemove(values1, values2);
            testPutAll(values1, values2, hashBitMask);
            testRemoveAll(values1, values2);
            testRetainAll(values1, values2);
            testContains(values1, values2);
            testOfEntries(values1, values2);
            testCopyOf(values1, values2);
            testEqualsHashCode(values1, values2, hashBitMask);
            testSerialization(values1, values2, hashBitMask);
        }
    }

    private void testSerialization(LinkedHashMap<HashCollider, HashCollider> values1, LinkedHashMap<HashCollider, HashCollider> values2, int hashBitMask) {
        Map<HashCollider, HashCollider> actual = of();
        LinkedHashMap<HashCollider, HashCollider> expected = new LinkedHashMap<>();

        // WHEN: empty map is re-serialized
        actual = reserialize(actual);


        // WHEN: map with entries is re-serialized
        actual.putAll(values1);
        expected.putAll(values1);
        assertMapEquality(expected, actual);
        actual = reserialize(actual);
        assertMapEquality(expected, actual);

        // WHEN: map with more entries is re-serialized
        actual.putAll(values2);
        expected.putAll(values2);
        assertMapEquality(expected, actual);

        actual = reserialize(actual);
        assertMapEquality(expected, actual);

    }

    public static <O> O reserialize(O object) {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();

        try (ObjectOutputStream out = new ObjectOutputStream(buf)) {
            out.writeObject(object);
        } catch (IOException e) {
            throw new AssertionError(e);
        }

        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buf.toByteArray()))) {
            @SuppressWarnings("unchecked") O o = (O) in.readObject();
            return o;
        } catch (IOException | ClassNotFoundException e) {
            throw new AssertionError(e);
        }
    }

    private void testPut(LinkedHashMap<HashCollider, HashCollider> values1, LinkedHashMap<HashCollider, HashCollider> values2, int hashBitMask) {
        Map<HashCollider, HashCollider> actual = of();
        LinkedHashMap<HashCollider, HashCollider> expected = new LinkedHashMap<>();

        // GIVEN: a set with values1
        for (Map.Entry<HashCollider, HashCollider> entry : values1.entrySet()) {
            actual.put(entry.getKey(), entry.getValue());
            expected.put(entry.getKey(), entry.getValue());
            assertMapEquality(expected, actual);
        }
        int id = 1000;
        for (Map.Entry<HashCollider, HashCollider> entry : values1.entrySet()) {
            HashCollider newValue = new HashCollider(id++, -1);
            // WHEN: key is already in set, then put must yield the old value
            assertEquals(entry.getValue(), actual.put(entry.getKey(), newValue));

        }

        id = 2000;
        for (Map.Entry<HashCollider, HashCollider> entry : values2.entrySet()) {
            // WHEN: key is new in set, then put must yield null
            // THEN: then put must yield the old value
            assertNull(actual.put(entry.getKey(), entry.getValue()));
            assertEquals(entry.getValue(), actual.put(entry.getKey(), entry.getValue()));
        }
    }

    private void testPutAll(LinkedHashMap<HashCollider, HashCollider> values1, LinkedHashMap<HashCollider, HashCollider> values2, int hashBitMask) {
        Map<HashCollider, HashCollider> actual = of();
        LinkedHashMap<HashCollider, HashCollider> expected = new LinkedHashMap<>();

        // WHEN: putAll(values1) on an empty set, then the set must be equal to values1
        actual.putAll(values1);
        expected.putAll(values1);
        assertEquals(expected, actual);

        // WHEN: putAll(values2) on set with values1, then the set must be equal to union of values1 and values2
        actual.putAll(values2);
        expected.putAll(values2);
        assertEquals(expected, actual);

        // WHEN: putAll(values1) on set with values1 and values2, then the set must be equal to union of values1 and values2
        actual.putAll(values1);
        expected.putAll(values1);
        assertEquals(expected, actual);

    }

    private void testRemove(LinkedHashMap<HashCollider, HashCollider> values1, LinkedHashMap<HashCollider, HashCollider> values2) {
        Map<HashCollider, HashCollider> actual = of();
        LinkedHashMap<HashCollider, HashCollider> expected = new LinkedHashMap<>();

        // GIVEN: a set with values1
        for (Map.Entry<HashCollider, HashCollider> entry : values1.entrySet()) {
            actual.put(entry.getKey(), entry.getValue());
            expected.put(entry.getKey(), entry.getValue());
            assertMapEquality(expected, actual);
        }

        int id = 1000;
        for (Map.Entry<HashCollider, HashCollider> entry : values1.entrySet()) {
            HashCollider newValue = new HashCollider(id++, -1);

            // WHEN: key is already in set, then put must yield the old value
            assertEquals(entry.getValue(), actual.put(entry.getKey(), newValue));


            // WHEN: key is removed, then must yield removed value
            assertEquals(newValue, actual.remove(entry.getKey()));
            assertFalse(actual.containsKey(entry.getKey()));

            // WHEN: key is put again, then put must yield null
            assertNull(actual.put(entry.getKey(), entry.getValue()));
            assertTrue(actual.containsKey(entry.getKey()));

            // WHEN: key is removed, then must yield old value
            assertEquals(entry.getValue(), actual.remove(entry.getKey()));

            // WHEN: non-existent key is removed, then must yield null
            assertNull(actual.remove(entry.getKey()));
        }
    }

    private void testRemoveAll(LinkedHashMap<HashCollider, HashCollider> values1, LinkedHashMap<HashCollider, HashCollider> values2) {
        Map<HashCollider, HashCollider> actual = of();
        LinkedHashMap<HashCollider, HashCollider> expected = new LinkedHashMap<>();

        // GIVEN: a set with values1 and values2
        actual.putAll(values1);
        actual.putAll(values2);
        expected.putAll(values1);
        expected.putAll(values2);
        assertEquals(expected, actual);

        // WHEN: removeAll values1
        assertTrue(actual.entrySet().removeAll(values1.entrySet()));
        expected.entrySet().removeAll(values1.entrySet());
        assertEquals(expected, actual);

        // WHEN: removeAll values1 which are not in set
        assertFalse(actual.entrySet().removeAll(values1.entrySet()));
        expected.entrySet().removeAll(values1.entrySet());
        assertEquals(expected, actual);
    }

    private void testRetainAll(LinkedHashMap<HashCollider, HashCollider> values1, LinkedHashMap<HashCollider, HashCollider> values2) {
        Map<HashCollider, HashCollider> actual = of();
        LinkedHashMap<HashCollider, HashCollider> expected = new LinkedHashMap<>();

        // GIVEN: a set with values1 and values2
        actual.putAll(values1);
        actual.putAll(values2);
        expected.putAll(values1);
        expected.putAll(values2);
        assertEquals(expected, actual);

        // WHEN: retainAll with entry set of values1
        actual.putAll(values2);
        expected.putAll(values2);
        actual.entrySet().retainAll(values1.entrySet());
        expected.entrySet().retainAll(values1.entrySet());
        assertEquals(expected, actual);

        // WHEN: retainAll with key set of values1
        actual.keySet().retainAll(values1.keySet());
        expected.keySet().retainAll(values1.keySet());
        assertEquals(expected, actual);

    }

    @TestFactory
    public @NonNull List<DynamicTest> dynamicTestNullKeyNullValue() {
        return Arrays.asList(
                dynamicTest("null key", () -> testNullKeyNullValue(null, new HashCollider(5, -1))),
                dynamicTest("null value", () -> testNullKeyNullValue(new HashCollider(5, -1), null)),
                dynamicTest("null key, null value", () -> testNullKeyNullValue(null, null))
        );
    }

    public void testNullKeyValueMap(@Nullable HashCollider key, @Nullable HashCollider value) {
        testNullKeyNullValue(key, value);

    }

    public void testNullKeyNullValue(@Nullable HashCollider key, @Nullable HashCollider value) {
        Map<HashCollider, HashCollider> set = copyOf(List.of(new AbstractMap.SimpleImmutableEntry<>(key, value)));
        LinkedHashMap<HashCollider, HashCollider> expected = new LinkedHashMap<>();
        expected.put(key, value);
        assertTrue(set.containsKey(key));
        assertEquals(expected.toString(), set.toString());
        assertEquals(expected.hashCode(), set.hashCode());
        assertEquals(expected, set);

        expected.remove(key);
        set.remove(key);
        assertEquals(expected, set);
    }


}