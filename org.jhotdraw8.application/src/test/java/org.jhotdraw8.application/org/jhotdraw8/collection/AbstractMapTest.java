package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class AbstractMapTest {
    /**
     * The test data.
     */
    static final class Data {
        private final String name;
        private final ReadOnlyMap<HashCollider, HashCollider> a;
        private final ReadOnlyMap<HashCollider, HashCollider> b;
        private final ReadOnlyMap<HashCollider, HashCollider> c;

        /**
         * Creates a new instance with 3 maps of the same non-empty size.
         *
         * @param name the name of the data
         * @param a    a non-empty map, all values are distinct
         * @param b    a map with identical keys but different values from a,
         *             all values are distinct from the values in a
         *             and from other values in b
         * @param c    a map with different keys and values from a and b,
         *             all values are distinct from the values in a and b
         *             and from other values in c,
         */
        Data(String name, ReadOnlyMap<HashCollider, HashCollider> a,
             ReadOnlyMap<HashCollider, HashCollider> b,
             ReadOnlyMap<HashCollider, HashCollider> c) {
            this.name = name;
            this.a = a;
            this.b = b;
            this.c = c;
        }

        @Override
        public String toString() {
            return name;
        }

        public String name() {
            return name;
        }

        public ReadOnlyMap<HashCollider, HashCollider> a() {
            return a;
        }

        public ReadOnlyMap<HashCollider, HashCollider> b() {
            return b;
        }

        public ReadOnlyMap<HashCollider, HashCollider> c() {
            return c;
        }
    }

    /**
     * Creates a new empty instance.
     */
    protected abstract <K, V> @NonNull Map<K, V> newInstance();

    /**
     * Creates a new instance with the specified expected number of elements
     * and load factor.
     */
    protected abstract <K, V> @NonNull Map<K, V> newInstance(int numElements, float loadFactor);

    /**
     * Creates a new instance with the specified map.
     */
    protected abstract <K, V> @NonNull Map<K, V> newInstance(@NonNull Map<K, V> m);


    protected abstract <K, V> @NonNull Map<K, V> toClonedInstance(@NonNull Map<K, V> m);

    /**
     * Creates a new instance with the specified map.
     */
    abstract <K, V> @NonNull Map<K, V> newInstance(@NonNull Iterable<Map.Entry<K, V>> m);

    public static Stream<Data> dataProvider() {
        return Stream.of(
                NO_COLLISION, ALL_COLLISION, SOME_COLLISION
        );
    }

    private final static Data NO_COLLISION = newData("no collisions", -1, 32, 100_000);
    private final static Data ALL_COLLISION = newData("all collisions", 0, 32, 100_000);
    private final static Data SOME_COLLISION = newData("some collisions", 0x55555555, 32, 100_000);

    private static int createNewValue(Random rng, Set<Integer> usedValues, int bound) {
        int value;
        int count = 0;
        do {
            value = rng.nextInt(bound);
            count++;
            if (count >= bound) {
                throw new RuntimeException("error in rng");
            }
        } while (!usedValues.add(value));
        return value;
    }

    private static Data newData(String name, int hashBitMask, int size, int bound) {
        Random rng = new Random(0);
        LinkedHashMap<HashCollider, HashCollider> a = new LinkedHashMap<>(size * 2);
        LinkedHashMap<HashCollider, HashCollider> b = new LinkedHashMap<>(size * 2);
        LinkedHashMap<HashCollider, HashCollider> c = new LinkedHashMap<>(size * 2);
        LinkedHashSet<Integer> usedValues = new LinkedHashSet<>();
        for (int i = 0; i < size; i++) {
            int keyAB = createNewValue(rng, usedValues, bound);
            int keyC = createNewValue(rng, usedValues, bound);
            int valueA = createNewValue(rng, usedValues, bound);
            int valueB = createNewValue(rng, usedValues, bound);
            int valueC = createNewValue(rng, usedValues, bound);
            a.put(new HashCollider(keyAB, hashBitMask), new HashCollider(valueA, hashBitMask));
            b.put(new HashCollider(keyAB, hashBitMask), new HashCollider(valueB, hashBitMask));
            c.put(new HashCollider(keyC, hashBitMask), new HashCollider(valueC, hashBitMask));
        }
        return new Data(name,
                new WrappedReadOnlyMap<>(a),
                new WrappedReadOnlyMap<>(b),
                new WrappedReadOnlyMap<>(c));
    }

    protected void assertEqualMap(ReadOnlyMap<HashCollider, HashCollider> expected, Map<HashCollider, HashCollider> actual) {
        assertEqualMap(expected.asMap(), actual);
    }

    protected void assertEqualMap(Map<HashCollider, HashCollider> expected, Map<HashCollider, HashCollider> actual) {
        assertEquals(expected.size(), actual.size());
        assertEquals(expected.isEmpty(), actual.isEmpty());
        assertEquals(expected.hashCode(), actual.hashCode());
        assertEquals(expected, actual);
        assertEquals(actual, expected);
        assertEquals(expected.entrySet(), actual.entrySet());
        assertEquals(expected.keySet(), actual.keySet());

        ArrayList<HashCollider> expectedValues = new ArrayList<>(expected.values());
        ArrayList<HashCollider> actualValues = new ArrayList<>(actual.values());
        expectedValues.sort(Comparator.comparing(HashCollider::getValue));
        actualValues.sort(Comparator.comparing(HashCollider::getValue));
        assertEquals(expectedValues, actualValues);
    }

    protected void assertNotEqualMap(Map<HashCollider, HashCollider> expected, Map<HashCollider, HashCollider> actual) {
        assertNotEquals(expected, actual);
        assertNotEquals(actual, expected);
        assertNotEquals(expected.entrySet(), actual.entrySet());
    }

    @Test
    public void testNewInstanceNoArgsShouldBeEmpty() {
        Map<HashCollider, HashCollider> actual = newInstance();
        LinkedHashMap<HashCollider, HashCollider> expected = new LinkedHashMap<>();
        assertEqualMap(expected, actual);
    }

    @Test
    public void testNewInstanceCapacityArgsShouldBeEmpty() {
        Map<HashCollider, HashCollider> actual = newInstance(24, 0.75f);
        LinkedHashMap<HashCollider, HashCollider> expected = new LinkedHashMap<>(24, 0.75f);
        assertEqualMap(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testNewInstanceMapArgsShouldBeEqualToArg(Data data) {
        Map<HashCollider, HashCollider> actual = newInstance(data.a().asMap());
        assertEqualMap(data.a(), actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testNewInstanceMapArgsOfSameTypeShouldBeEqualToArg(Data data) {
        Map<HashCollider, HashCollider> actual1 = newInstance(data.a().asMap());
        Map<HashCollider, HashCollider> actual = newInstance(actual1);
        assertEqualMap(data.a(), actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testNewInstanceReadOnlyMapArgShouldBeEqualToARg(Data data) {
        Map<HashCollider, HashCollider> actual = newInstance(data.a());
        assertEqualMap(data.a(), actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testNewInstanceIterableArgShouldBeEqualToArg(Data data) {
        Map<HashCollider, HashCollider> actual = newInstance(data.a().readOnlyEntrySet());
        assertEqualMap(data.a(), actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testClearShouldYieldEmptyMap(Data data) {
        Map<HashCollider, HashCollider> actual = newInstance(data.a());
        assertNotEqualMap(Collections.emptyMap(), actual);
        actual.clear();
        assertEqualMap(Collections.emptyMap(), actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testClearShouldBeIdempotent(Data data) {
        Map<HashCollider, HashCollider> actual = newInstance(data.a());
        assertNotEqualMap(Collections.emptyMap(), actual);
        actual.clear();
        assertEqualMap(Collections.emptyMap(), actual);
        actual.clear();
        assertEqualMap(Collections.emptyMap(), actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testCloneShouldYieldEqualMap(Data data) {
        Map<HashCollider, HashCollider> actual = newInstance(data.a());
        Map<HashCollider, HashCollider> clone = toClonedInstance(actual);
        assertEqualMap(data.a(), clone);
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testContainsKeyShouldYieldExpectedValue(Data data) {
        Map<HashCollider, HashCollider> actual = newInstance(data.a());
        for (HashCollider k : data.a().readOnlyKeySet()) {
            assertTrue(actual.containsKey(k));
        }
        for (HashCollider k : data.c().readOnlyKeySet()) {
            assertFalse(actual.containsKey(k));
        }
        assertFalse(actual.containsKey(new Object()));
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testContainsEntryShouldYieldExpectedValue(Data data) {
        Map<HashCollider, HashCollider> actual = newInstance(data.a());
        for (Map.Entry<HashCollider, HashCollider> e : data.a().readOnlyEntrySet()) {
            assertTrue(actual.entrySet().contains(e));
        }
        for (Map.Entry<HashCollider, HashCollider> e : data.b().readOnlyEntrySet()) {
            assertFalse(actual.entrySet().contains(e));
        }
        for (Map.Entry<HashCollider, HashCollider> e : data.c().readOnlyEntrySet()) {
            assertFalse(actual.entrySet().contains(e));
        }
        assertFalse(actual.entrySet().contains(new Object()));
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testKeyIteratorRemoveShouldRemoveEntry(Data data) {
        Map<HashCollider, HashCollider> actual = newInstance(data.a());
        Map<HashCollider, HashCollider> expected = new LinkedHashMap<>(data.a().asMap());
        List<HashCollider> toRemove = new ArrayList<>(new HashSet<>(data.a().readOnlyKeySet().asSet()));
        while (!toRemove.isEmpty() && !expected.isEmpty()) {
            for (Iterator<HashCollider> i = actual.keySet().iterator(); i.hasNext(); ) {
                HashCollider k = i.next();
                if (k.equals(toRemove.get(0))) {
                    i.remove();
                    toRemove.remove(0);
                    expected.remove(k);
                    assertEqualMap(expected, actual);
                }
            }
        }
        assertEqualMap(Collections.emptyMap(), actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testEntryIteratorRemoveShouldRemoveEntryAndRemoveIsNotIdempotent(Data data) {
        Map<HashCollider, HashCollider> actual = newInstance(data.a());
        Map<HashCollider, HashCollider> expected = new LinkedHashMap<>(data.a().asMap());
        List<Map.Entry<HashCollider, HashCollider>> toRemove = new ArrayList<>(new HashSet<>(data.a().readOnlyEntrySet().asSet()));
        for (int countdown = toRemove.size(); countdown > 0; countdown--) {
            for (Iterator<Map.Entry<HashCollider, HashCollider>> i = actual.entrySet().iterator(); i.hasNext(); ) {
                Map.Entry<HashCollider, HashCollider> k = i.next();
                if (k.equals(toRemove.get(0))) {
                    i.remove();
                    toRemove.remove(0);
                    expected.remove(k.getKey());
                    assertEqualMap(expected, actual);

                    assertThrows(IllegalStateException.class, i::remove);
                }
            }
        }
        assertEqualMap(Collections.emptyMap(), actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testEntrySetRemoveShouldRemoveEntryWithSameKeyAndValue(Data data) {
        Map<HashCollider, HashCollider> actual = newInstance(data.a());
        Map<HashCollider, HashCollider> expected = new LinkedHashMap<>(data.a().asMap());

        for (Map.Entry<HashCollider, HashCollider> e : data.a().readOnlyEntrySet()) {
            assertTrue(actual.entrySet().remove(e));
            expected.entrySet().remove(e);
            assertEqualMap(expected, actual);
        }
        assertEqualMap(Collections.emptyMap(), actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testEntrySetRemoveShouldNotRemoveEntryWithSameKeyButDifferentValue(Data data) {
        Map<HashCollider, HashCollider> instance = newInstance(data.a());
        Map<HashCollider, HashCollider> expected = new LinkedHashMap<>(data.a().asMap());
        for (Map.Entry<HashCollider, HashCollider> e : data.b().readOnlyEntrySet()) {
            assertFalse(instance.entrySet().remove(e));
            assertEqualMap(expected, instance);
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testEntrySetRemoveShouldNotRemoveEntryWithDifferentKeyAndDifferentValue(Data data) {
        Map<HashCollider, HashCollider> instance = newInstance(data.a());
        Map<HashCollider, HashCollider> expected = new LinkedHashMap<>(data.a().asMap());
        for (Map.Entry<HashCollider, HashCollider> e : data.c().readOnlyEntrySet()) {
            assertFalse(instance.entrySet().remove(e));
            assertEqualMap(expected, instance);
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testIteratorRemoveShouldThrowIllegalStateException(Data data) {
        Map<HashCollider, HashCollider> instance = newInstance(data.a());
        Iterator<HashCollider> i = instance.keySet().iterator();
        assertThrows(IllegalStateException.class, i::remove);
        Iterator<HashCollider> k = instance.values().iterator();
        assertThrows(IllegalStateException.class, k::remove);
        Iterator<Map.Entry<HashCollider, HashCollider>> j = instance.entrySet().iterator();
        assertThrows(IllegalStateException.class, j::remove);
    }

    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testSerializationShouldYieldSameMap(Data data) throws Exception {
        Map<HashCollider, HashCollider> instance = newInstance(data.a());
        assertEqualMap(data.a(), instance);
        if (instance instanceof Serializable) {
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            try (ObjectOutputStream out = new ObjectOutputStream(buf)) {
                out.writeObject(instance);
            }
            Map<HashCollider, HashCollider> deserialized;
            try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buf.toByteArray()))) {
                deserialized = (Map<HashCollider, HashCollider>) in.readObject();
            }
            assertEqualMap(data.a(), deserialized);
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testEntryIteratorShouldUpdateMap(Data data) {
        Map<HashCollider, HashCollider> instance = newInstance(data.a());
        Map<HashCollider, HashCollider> expected = new LinkedHashMap<>(data.a().asMap());
        for (Map.Entry<HashCollider, HashCollider> entry : instance.entrySet()) {
            entry.setValue(data.b().get(entry.getKey()));
            assertNotEqualMap(instance, expected);
            expected.put(entry.getKey(), data.b().get(entry.getKey()));
            assertEqualMap(instance, expected);
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testEqualWithThisShouldYieldTrue(Data data) {
        Map<HashCollider, HashCollider> instance = newInstance(data.a());
        assertEquals(instance, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testEqualsWithCloneWithUpdatedEntriesShouldYieldFalse(Data data) throws Exception {
        Map<HashCollider, HashCollider> instance = newInstance(data.a());
        Map<HashCollider, HashCollider> clone = toClonedInstance(instance);
        assertEquals(instance, clone);
        clone.putAll(data.b().asMap());
        assertNotEquals(instance, clone);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testPutWithNewKeyShouldReturnNull(Data data) throws Exception {
        Map<HashCollider, HashCollider> instance = newInstance(data.a);
        for (Map.Entry<HashCollider, HashCollider> e : data.c) {
            assertNull(instance.put(e.getKey(), e.getValue()));
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testPutWithContainedKeyButNewValueShouldReturnOldValue(Data data) throws Exception {
        Map<HashCollider, HashCollider> instance = newInstance(data.a());
        LinkedHashMap<HashCollider, HashCollider> expected = new LinkedHashMap<>(data.a().asMap());
        for (Map.Entry<HashCollider, HashCollider> e : data.a) {
            assertEquals(expected.get(e.getKey()), instance.put(e.getKey(), e.getValue()));
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testRemoveWithNewKeyShouldReturnNull(Data data) throws Exception {
        Map<HashCollider, HashCollider> instance = newInstance(data.a);
        for (Map.Entry<HashCollider, HashCollider> e : data.c) {
            assertNull(instance.remove(e.getKey()));
            assertEqualMap(data.a.asMap(), instance);
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testRemoveWithContainedKeyShouldReturnOldValue(Data data) throws Exception {
        Map<HashCollider, HashCollider> instance = newInstance(data.a);
        LinkedHashMap<HashCollider, HashCollider> expected = new LinkedHashMap<>(data.a().asMap());
        for (Map.Entry<HashCollider, HashCollider> e : data.b) {
            HashCollider expectedRemoved = expected.remove(e.getKey());
            assertEquals(expectedRemoved, instance.remove(e.getKey()));
            assertEqualMap(expected, instance);
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testKeySetRemoveAllWithNewKeyShouldReturnFalse(Data data) throws Exception {
        Map<HashCollider, HashCollider> instance = newInstance(data.a);
        assertFalse(instance.keySet().removeAll(data.c.readOnlyKeySet().asSet()));
        assertEqualMap(data.a, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testKeySetRemoveAllWithContainedKeyShouldReturnTrue(Data data) throws Exception {
        Map<HashCollider, HashCollider> instance = newInstance(data.a);
        assertTrue(instance.keySet().removeAll(data.a.readOnlyKeySet().asSet()));
        assertEqualMap(Collections.emptyMap(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testEntrySetRemoveAllWithNewEntryShouldReturnFalse(Data data) throws Exception {
        Map<HashCollider, HashCollider> instance = newInstance(data.a);
        assertFalse(instance.entrySet().removeAll(data.c.readOnlyEntrySet().asSet()));
        assertEqualMap(data.a, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testEntrySetRemoveAllWithEntriesThatHaveSameKeyButDifferentValueShouldReturnFalse(Data data) throws Exception {
        Map<HashCollider, HashCollider> instance = newInstance(data.a);
        assertFalse(instance.entrySet().removeAll(data.b.readOnlyEntrySet().asSet()));
        assertEqualMap(data.a, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testEntrySetRemoveAllWithContainedEntryShouldReturnTrue(Data data) throws Exception {
        Map<HashCollider, HashCollider> instance = newInstance(data.a);
        assertTrue(instance.entrySet().removeAll(data.a.readOnlyEntrySet().asSet()));
        assertEqualMap(Collections.emptyMap(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testEntrySetContainsExpectedEntries(Data data) throws Exception {
        Map<HashCollider, HashCollider> instance = newInstance(data.a);
        Set<Map.Entry<HashCollider, HashCollider>> entrySet = instance.entrySet();
        for (Map.Entry<HashCollider, HashCollider> e : data.a) {
            assertTrue(entrySet.contains(e));
        }
        for (Map.Entry<HashCollider, HashCollider> e : data.b) {
            assertFalse(entrySet.contains(e));
        }
        for (Map.Entry<HashCollider, HashCollider> e : data.c) {
            assertFalse(entrySet.contains(e));
        }
        assertTrue(entrySet.containsAll(data.a.readOnlyEntrySet().asSet()));
        assertFalse(entrySet.containsAll(data.b.readOnlyEntrySet().asSet()));
        assertFalse(entrySet.containsAll(data.c.readOnlyEntrySet().asSet()));
        LinkedHashSet<Map.Entry<HashCollider, HashCollider>> abc = new LinkedHashSet<>(data.a.readOnlyEntrySet().asSet());
        abc.addAll(data.b.readOnlyEntrySet().asSet());
        abc.addAll(data.c.readOnlyEntrySet().asSet());
        assertFalse(entrySet.containsAll(abc));
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testEntryIteratorShouldYieldExpectedEntries(Data data) throws Exception {
        Map<HashCollider, HashCollider> instance = newInstance(data.a);
        Set<Map.Entry<HashCollider, HashCollider>> entrySet = instance.entrySet();
        LinkedHashSet<Map.Entry<HashCollider, HashCollider>> actual = new LinkedHashSet<>(entrySet);
        assertEquals(data.a.readOnlyEntrySet().asSet(), actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testPutWithContainedEntryShouldReturnOldValue(Data data) throws Exception {
        Map<HashCollider, HashCollider> instance = newInstance(data.a());
        LinkedHashMap<HashCollider, HashCollider> expected = new LinkedHashMap<>(data.a().asMap());
        for (Map.Entry<HashCollider, HashCollider> e : data.b) {
            assertEquals(expected.get(e.getKey()), instance.put(e.getKey(), e.getValue()));
        }
    }


    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testPutAllWithContainedEntries(Data data) throws Exception {
        Map<HashCollider, HashCollider> instance = newInstance(data.a());
        instance.putAll(data.a().asMap());
        assertEquals(data.a().asMap(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testPutAllWithNewEntries(Data data) throws Exception {
        Map<HashCollider, HashCollider> instance = newInstance(data.a());
        instance.putAll(data.c().asMap());
        Map<HashCollider, HashCollider> expected = new LinkedHashMap<>(data.a().asMap());
        expected.putAll(data.c().asMap());
        assertEquals(expected, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testPutAllWithContainedKeysButNewValues(Data data) throws Exception {
        Map<HashCollider, HashCollider> instance = newInstance(data.a());
        instance.putAll(data.b().asMap());
        Map<HashCollider, HashCollider> expected = new LinkedHashMap<>(data.a().asMap());
        expected.putAll(data.b().asMap());
        assertEquals(expected, instance);
    }

    @SuppressWarnings({"CollectionAddedToSelf"})
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testPutAllWithSelfShouldYieldSameMap(Data data) throws Exception {
        Map<HashCollider, HashCollider> instance = newInstance(data.a());
        instance.putAll(instance);
        assertEquals(data.a().asMap(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testEqualsWithObjectShouldYieldFalse(Data data) throws Exception {
        Map<HashCollider, HashCollider> instance = newInstance(data.a());
        assertNotEquals(instance, new Object());
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testGetOrDefaultWithContainedKeyShouldYieldValue(Data data) throws Exception {
        Map<HashCollider, HashCollider> instance = newInstance(data.a());
        HashCollider defaultValue = new HashCollider(7, -1);
        for (Map.Entry<HashCollider, HashCollider> e : data.a()) {
            assertEquals(e.getValue(), instance.getOrDefault(e.getKey(), defaultValue));
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testGetOrDefaultWithNonContainedKeyShouldYieldDefault(Data data) throws Exception {
        Map<HashCollider, HashCollider> instance = newInstance(data.a());
        HashCollider defaultValue = new HashCollider(7, -1);
        for (Map.Entry<HashCollider, HashCollider> e : data.c()) {
            assertEquals(defaultValue, instance.getOrDefault(e.getKey(), defaultValue));
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testPutAllWithSomeNewKeyShouldAddAll(Data data) throws Exception {
        ArrayList<Map.Entry<HashCollider, HashCollider>> listB = new ArrayList<>(data.b.readOnlyEntrySet().asSet());
        ArrayList<Map.Entry<HashCollider, HashCollider>> listC = new ArrayList<>(data.c.readOnlyEntrySet().asSet());
        Map<HashCollider, HashCollider> m = new LinkedHashMap<>(data.a.asMap());
        for (Map.Entry<HashCollider, HashCollider> entry : listB.subList(0, listB.size() / 2)) {
            m.put(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<HashCollider, HashCollider> entry : listC.subList(0, listC.size() / 2)) {
            m.put(entry.getKey(), entry.getValue());
        }
        Map<HashCollider, HashCollider> instance = newInstance(data.a);
        instance.putAll(m);
        LinkedHashMap<HashCollider, HashCollider> expected = new LinkedHashMap<>(data.a.asMap());
        expected.putAll(m);
        assertEqualMap(expected, instance);
    }
}
