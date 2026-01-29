package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.readable.ReadableMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.SequencedMap;
import java.util.SequencedSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.Spliterator;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class AbstractMapTest {

    private static final MapData NO_COLLISION = MapData.newData("no collisions", -1, 32, 100_000);
    private static final MapData ALL_COLLISION = MapData.newData("all collisions", 0, 32, 100_000);
    private static final MapData SOME_COLLISION = MapData.newData("some collisions", 0x55555555, 32, 100_000);

    public static Stream<MapData> dataProvider() {
        return Stream.of(
                NO_COLLISION, ALL_COLLISION, SOME_COLLISION
        );
    }

    protected abstract boolean supportsNullKeys();

    protected boolean supportsNullEntries() {
        return false;
    }

    protected <K, V> void assertEqualMap(ReadableMap<K, V> expected, Map<K, V> actual) {
        assertEqualMap(expected.asMap(), actual);
    }

    protected <K, V> void assertEqualMap(Map<K, V> expected, Map<K, V> actual) {
        assertEquals(expected.size(), actual.size());
        assertEquals(expected.isEmpty(), actual.isEmpty());
        assertEquals(expected.hashCode(), actual.hashCode());
        assertEquals(expected, actual);
        assertEquals(actual, expected);
        assertEquals(expected.entrySet(), actual.entrySet());
        assertEquals(expected.keySet(), actual.keySet());
        assertEquals(new LinkedHashSet<>(expected.values()),
                new LinkedHashSet<>(actual.values()));
    }

    protected <K, V> void assertNotEqualMap(Map<K, V> expected, Map<K, V> actual) {
        assertNotEquals(expected, actual);
        assertNotEquals(actual, expected);
        assertNotEquals(expected.entrySet(), actual.entrySet());
    }

    /**
     * Creates a new empty instance.
     */
    protected abstract <K, V> Map<K, V> newInstance();

    /**
     * Creates a new instance with the specified expected number of elements
     * and load factor.
     */
    protected abstract <K, V> Map<K, V> newInstance(int numElements, float loadFactor);

    /**
     * Creates a new instance with the specified map.
     */
    protected abstract <K, V> Map<K, V> newInstance(Map<K, V> m);

    /**
     * Creates a new instance with the specified map.
     */
    abstract <K, V> Map<K, V> newInstance(Iterable<Map.Entry<K, V>> m);

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void clearShouldBeIdempotent(MapData data) {
        Map<Key, Value> instance = newInstance(data.a());
        assertNotEqualMap(Collections.emptyMap(), instance);
        instance.clear();
        assertEqualMap(Collections.emptyMap(), instance);
        instance.clear();
        assertEqualMap(Collections.emptyMap(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void clearShouldYieldEmptyMap(MapData data) {
        Map<Key, Value> instance = newInstance(data.a());
        assertNotEqualMap(Collections.emptyMap(), instance);
        instance.clear();
        assertEqualMap(Collections.emptyMap(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void cloneShouldYieldEqualMap(MapData data) {
        Map<Key, Value> instance = newInstance(data.a());
        Map<Key, Value> clone = toClonedInstance(instance);
        assertEqualMap(data.a(), clone);
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void containsKeyShouldYieldExpectedValue(MapData data) {
        Map<Key, Value> instance = newInstance(data.a());
        for (Key k : data.a().readableKeySet()) {
            assertTrue(instance.containsKey(k));
        }
        for (Key k : data.c().readableKeySet()) {
            assertFalse(instance.containsKey(k));
        }
        try {
            assertFalse(instance.containsKey(new Object()));
        } catch (ClassCastException e) {
            assertInstanceOf(SortedMap.class, instance, "only SortedMap may throw a ClassCastException");
        }
    }

    @Test
    public void spliteratorShouldHaveMapCharacteristics() throws Exception {
        Map<Key, Value> instance = newInstance();

        assertEquals(Spliterator.DISTINCT | Spliterator.SIZED, (Spliterator.DISTINCT | Spliterator.SIZED) & instance.keySet().spliterator().characteristics());
        assertEquals(Spliterator.DISTINCT | Spliterator.SIZED, (Spliterator.DISTINCT | Spliterator.SIZED) & instance.entrySet().spliterator().characteristics());
        assertEquals(Spliterator.SIZED, (Spliterator.SIZED) & instance.values().spliterator().characteristics());
    }
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void entryIteratorEntrySetValueShouldUpdateMap(MapData data) {
        Map<Key, Value> instance = newInstance(data.a());
        SequencedMap<Key, Value> expected = new LinkedHashMap<>(data.a().asMap());
        for (Map.Entry<Key, Value> entry : instance.entrySet()) {
            entry.setValue(data.aWithDifferentValues().get(entry.getKey()));
            assertNotEqualMap(instance, expected);
            expected.put(entry.getKey(), data.aWithDifferentValues().get(entry.getKey()));
            assertEqualMap(instance, expected);
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void entryIteratorRemoveShouldRemoveEntryAndRemoveIsNotIdempotent(MapData data) {
        Map<Key, Value> instance = newInstance(data.a());
        SequencedMap<Key, Value> expected = new LinkedHashMap<>(data.a().asMap());
        List<Map.Entry<Key, Value>> toRemove = new ArrayList<>(new HashSet<>(data.a().readableEntrySet().asSet()));
        for (int countdown = toRemove.size(); countdown > 0; countdown--) {
            for (Iterator<Map.Entry<Key, Value>> i = instance.entrySet().iterator(); i.hasNext(); ) {
                Map.Entry<Key, Value> k = i.next();
                if (k.equals(toRemove.get(0))) {
                    i.remove();
                    toRemove.remove(0);
                    expected.remove(k.getKey());
                    assertEqualMap(expected, instance);

                    assertThrows(IllegalStateException.class, i::remove);
                }
            }
        }
        assertEqualMap(Collections.emptyMap(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void entryIteratorShouldYieldExpectedEntries(MapData data) throws Exception {
        Map<Key, Value> instance = newInstance(data.a);
        List<Map.Entry<Key, Value>> actualList = new ArrayList<>();
        LinkedHashMap<Key, Value> actualMap = new LinkedHashMap<>();
        instance.entrySet().iterator().forEachRemaining(actualList::add);
        instance.entrySet().iterator().forEachRemaining(e -> actualMap.put(e.getKey(), e.getValue()));
        assertEquals(data.a.size(), actualList.size());
        assertEqualMap(data.a, actualMap);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void entrySetContainsExpectedEntries(MapData data) throws Exception {
        Map<Key, Value> instance = newInstance(data.a);
        Set<Map.Entry<Key, Value>> entrySet = instance.entrySet();
        for (Map.Entry<Key, Value> e : data.a) {
            assertTrue(entrySet.contains(e));
        }
        for (Map.Entry<Key, Value> e : data.aWithDifferentValues) {
            assertFalse(entrySet.contains(e));
        }
        for (Map.Entry<Key, Value> e : data.c) {
            assertFalse(entrySet.contains(e));
        }
        assertTrue(entrySet.containsAll(data.a.readableEntrySet().asSet()));
        assertFalse(entrySet.containsAll(data.aWithDifferentValues.readableEntrySet().asSet()));
        assertFalse(entrySet.containsAll(data.c.readableEntrySet().asSet()));
        LinkedHashSet<Map.Entry<Key, Value>> abc = new LinkedHashSet<>(data.a.readableEntrySet().asSet());
        abc.addAll(data.aWithDifferentValues.readableEntrySet().asSet());
        abc.addAll(data.c.readableEntrySet().asSet());
        assertFalse(entrySet.containsAll(abc));
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void entrySetContainsShouldYieldExpectedValue(MapData data) {
        Map<Key, Value> instance = newInstance(data.a());
        for (Map.Entry<Key, Value> e : data.a().readableEntrySet()) {
            assertTrue(instance.entrySet().contains(e));
        }
        for (Map.Entry<Key, Value> e : data.aWithDifferentValues().readableEntrySet()) {
            assertFalse(instance.entrySet().contains(e));
        }
        for (Map.Entry<Key, Value> e : data.c().readableEntrySet()) {
            assertFalse(instance.entrySet().contains(e));
        }
        assertFalse(instance.entrySet().contains(new Object()));
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void entrySetRemoveAllWithContainedEntryShouldReturnTrue(MapData data) throws Exception {
        Map<Key, Value> instance = newInstance(data.a);
        assertTrue(instance.entrySet().removeAll(data.a.readableEntrySet().asSet()));
        assertEqualMap(Collections.emptyMap(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void entrySetRemoveAllWithEntriesThatHaveSameKeyButDifferentValueShouldReturnFalse(MapData data) throws Exception {
        Map<Key, Value> instance = newInstance(data.a);
        assertFalse(instance.entrySet().removeAll(data.aWithDifferentValues.readableEntrySet().asSet()));
        assertEqualMap(data.a, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void entrySetRemoveAllWithNewEntryShouldReturnFalse(MapData data) throws Exception {
        Map<Key, Value> instance = newInstance(data.a);
        assertFalse(instance.entrySet().removeAll(data.c.readableEntrySet().asSet()));
        assertEqualMap(data.a, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void entrySetRemoveShouldNotRemoveEntryWithDifferentKeyAndDifferentValue(MapData data) {
        Map<Key, Value> instance = newInstance(data.a());
        SequencedMap<Key, Value> expected = new LinkedHashMap<>(data.a().asMap());
        for (Map.Entry<Key, Value> e : data.c().readableEntrySet()) {
            assertFalse(instance.entrySet().remove(e));
            assertEqualMap(expected, instance);
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void entrySetRemoveShouldNotRemoveEntryWithSameKeyButDifferentValue(MapData data) {
        Map<Key, Value> instance = newInstance(data.a());
        SequencedMap<Key, Value> expected = new LinkedHashMap<>(data.a().asMap());
        for (Map.Entry<Key, Value> e : data.aWithDifferentValues().readableEntrySet()) {
            assertFalse(instance.entrySet().remove(e));
            assertEqualMap(expected, instance);
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void entrySetRemoveShouldRemoveEntryWithSameKeyAndValue(MapData data) {
        Map<Key, Value> instance = newInstance(data.a());
        SequencedMap<Key, Value> expected = new LinkedHashMap<>(data.a().asMap());

        for (Map.Entry<Key, Value> e : data.a().readableEntrySet()) {
            assertTrue(instance.entrySet().remove(e));
            expected.entrySet().remove(e);
            assertEqualMap(expected, instance);
        }
        assertEqualMap(Collections.emptyMap(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void equalWithThisShouldYieldTrue(MapData data) {
        Map<Key, Value> instance = newInstance(data.a());
        assertEquals(instance, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void equalsWithCloneWithUpdatedEntriesShouldYieldFalse(MapData data) throws Exception {
        Map<Key, Value> instance = newInstance(data.a());
        Map<Key, Value> instance2 = toClonedInstance(instance);
        assertEquals(instance, instance2);
        instance2.putAll(data.aWithDifferentValues().asMap());
        assertNotEquals(instance, instance2);
    }

    @SuppressWarnings("SimplifiableAssertion")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void equalsWithNullShouldYieldFalse(MapData data) {
        Map<Key, Value> instance = newInstance(data.a());
        assertFalse(instance.equals(null));
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void equalsWithObjectShouldYieldFalse(MapData data) throws Exception {
        Map<Key, Value> instance = newInstance(data.a());
        assertNotEquals(instance, new Object());
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void getOrDefaultWithContainedKeyShouldYieldValue(MapData data) throws Exception {
        Map<Key, Value> instance = newInstance(data.a());
        Value defaultValue = new Value(7, -1);
        for (Map.Entry<Key, Value> e : data.a()) {
            assertEquals(e.getValue(), instance.getOrDefault(e.getKey(), defaultValue));
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void getOfEntryWithNullValueShouldYieldNull(MapData data) throws Exception {
        Map<Key, Value> instance = newInstance(data.a());
        LinkedHashMap<Key, Value> expected = new LinkedHashMap<>(data.a.asMap());
        Key key = new Key(42, -1);
        assertNull(instance.put(key, null));
        expected.put(key, null);
        assertTrue(instance.containsKey(key));
        assertNull(instance.get(key));
        assertEqualMap(expected, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void getOrDefaultWithNonContainedKeyShouldYieldDefault(MapData data) throws Exception {
        Map<Key, Value> instance = newInstance(data.a());
        Value defaultValue = new Value(7, -1);
        for (Map.Entry<Key, Value> e : data.c()) {
            assertEquals(defaultValue, instance.getOrDefault(e.getKey(), defaultValue));
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void iteratorRemoveShouldThrowIllegalStateException(MapData data) {
        Map<Key, Value> instance = newInstance(data.a());
        Iterator<Key> i = instance.keySet().iterator();
        assertThrows(IllegalStateException.class, i::remove);
        Iterator<Value> k = instance.values().iterator();
        assertThrows(IllegalStateException.class, k::remove);
        Iterator<Map.Entry<Key, Value>> j = instance.entrySet().iterator();
        assertThrows(IllegalStateException.class, j::remove);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void keyIteratorRemoveShouldRemoveEntry(MapData data) {
        Map<Key, Value> instance = newInstance(data.a());
        SequencedMap<Key, Value> expected = new LinkedHashMap<>(data.a().asMap());
        List<Key> toRemove = new ArrayList<>(new HashSet<>(data.a().readableKeySet().asSet()));
        while (!toRemove.isEmpty() && !expected.isEmpty()) {
            for (Iterator<Key> i = instance.keySet().iterator(); i.hasNext(); ) {
                Key k = i.next();
                if (k.equals(toRemove.get(0))) {
                    i.remove();
                    toRemove.remove(0);
                    expected.remove(k);
                    assertEqualMap(expected, instance);
                }
            }
        }
        assertEqualMap(Collections.emptyMap(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void keySetRemoveAllOfEmptyMapShouldReturnFalse(MapData data) throws Exception {
        Map<Key, Value> instance = newInstance();
        assertFalse(instance.keySet().removeAll(data.a.readableKeySet().asSet()));
        assertEqualMap(Collections.emptyMap(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void keySetRemoveAllWithEmptyMapShouldReturnFalse(MapData data) throws Exception {
        Map<Key, Value> instance = newInstance(data.a);
        assertFalse(instance.keySet().removeAll(Collections.<Key>emptySet()));
        assertEqualMap(data.a(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void keySetRemoveAllWithContainedKeyShouldReturnTrue(MapData data) throws Exception {
        Map<Key, Value> instance = newInstance(data.a);
        assertTrue(instance.keySet().removeAll(data.a.readableKeySet().asSet()));
        assertEqualMap(Collections.emptyMap(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void keySetRemoveAllWithSomeContainedKeyShouldReturnTrue(MapData data) throws Exception {
        Map<Key, Value> instance = newInstance(data.a);
        assertTrue(instance.keySet().removeAll(data.someAPlusSomeB().readableKeySet().asSet()));
        LinkedHashMap<Key, Value> expected = new LinkedHashMap<>(data.a.asMap());
        expected.keySet().removeAll(data.someAPlusSomeB().readableKeySet().asSet());
        assertEqualMap(expected, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void keySetRemoveAllWithNewKeyShouldReturnFalse(MapData data) throws Exception {
        Map<Key, Value> instance = newInstance(data.a);
        assertFalse(instance.keySet().removeAll(data.c.readableKeySet().asSet()));
        assertEqualMap(data.a, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void keySetRetainAllOfEmptyMapShouldNotChangeMap(MapData data) throws Exception {
        Map<Key, Value> instance = newInstance();
        instance.keySet().retainAll(data.a().asMap().keySet());
        assertEqualMap(Collections.emptyMap(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void keySetRetainAllWithContainedKeysShouldNotChangeMap(MapData data) throws Exception {
        Map<Key, Value> instance = newInstance(data.a());
        instance.keySet().retainAll(data.a().asMap().keySet());
        assertEqualMap(data.a().asMap(), instance);
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void keySetRetainAllWithEmptySetShouldClearMap(MapData data) throws Exception {
        Map<Key, Value> instance = newInstance(data.a());
        instance.keySet().retainAll(Collections.emptySet());
        assertEqualMap(Collections.emptyMap(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void keySetRetainAllWithSomeContainedKeysShouldChangeMap(MapData data) throws Exception {
        Map<Key, Value> instance = newInstance(data.a());
        LinkedHashMap<Key, Value> expected = new LinkedHashMap<>(data.a.asMap());
        instance.keySet().retainAll(data.someAPlusSomeB().asMap().keySet());
        expected.keySet().retainAll(data.someAPlusSomeB().asMap().keySet());
        assertEqualMap(expected, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void keySetRetainAllWithSomeContainedKeysShouldReturnNewInstance(MapData data) throws Exception {
        Map<Key, Value> instance = newInstance(data.a());
        assertTrue(instance.keySet().retainAll(data.someAPlusSomeB().asMap().keySet()));
        LinkedHashMap<Key, Value> expected = new LinkedHashMap<>(data.a().asMap());
        expected.keySet().retainAll(data.someAPlusSomeB().asMap().keySet());
        assertEqualMap(expected, instance);
    }

    @Test
    public void newInstanceCapacityArgsShouldBeEmpty() {
        Map<Key, Value> actual = newInstance(24, 0.75f);
        LinkedHashMap<Key, Value> expected = new LinkedHashMap<>(24, 0.75f);
        assertEqualMap(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void newInstanceIterableArgShouldBeEqualToArg(MapData data) {
        Map<Key, Value> actual = newInstance(data.a().readableEntrySet());
        assertEqualMap(data.a(), actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void newInstanceMapArgsOfSameTypeShouldBeEqualToArg(MapData data) {
        Map<Key, Value> actual1 = newInstance(data.a().asMap());
        Map<Key, Value> actual = newInstance(actual1);
        assertEqualMap(data.a(), actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void newInstanceMapArgsShouldBeEqualToArg(MapData data) {
        Map<Key, Value> actual = newInstance(data.a().asMap());
        assertEqualMap(data.a(), actual);
    }

    @Test
    public void newInstanceNoArgsShouldBeEmpty() {
        Map<Key, Value> actual = newInstance();
        LinkedHashMap<Key, Value> expected = new LinkedHashMap<>();
        assertEqualMap(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void newInstanceReadOnlyMapArgShouldBeEqualToARg(MapData data) {
        Map<Key, Value> actual = newInstance(data.a());
        assertEqualMap(data.a(), actual);
    }

    @Test
    public void shouldSupportNullKeyNullValue() throws Exception {
        Map<Key, Value> instance = newInstance();
        assertFalse(instance.containsKey(null));
        assertFalse(instance.containsValue(null));
        Value oldValue = instance.put(null, null);
        assertNull(oldValue);
        assertTrue(instance.containsKey(null));
        assertTrue(instance.containsValue(null));
    }

    @Test
    public void spliteratorCharacteristicsShouldHaveNonNull() throws Exception {
        Map<Key, Value> instance = newInstance();
        if (supportsNullEntries()) {
            assertFalse(instance.entrySet().spliterator().hasCharacteristics(Spliterator.NONNULL), "entrySet.spliterator should be nullable");
        } else {
            assertTrue(instance.entrySet().spliterator().hasCharacteristics(Spliterator.NONNULL), "entrySet.spliterator should be non-null");
        }
        if (supportsNullKeys()) {
            assertFalse(instance.keySet().spliterator().hasCharacteristics(Spliterator.NONNULL), "keySet.spliterator should be nullable");
        } else {
            assertTrue(instance.keySet().spliterator().hasCharacteristics(Spliterator.NONNULL), "keySet.spliterator should not be nullable");
        }
        assertFalse(instance.values().spliterator().hasCharacteristics(Spliterator.NONNULL), "valueSet.spliterator should be nullable");
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void putAllWithContainedEntriesShouldNotChangeMap(MapData data) throws Exception {
        Map<Key, Value> instance = newInstance(data.a());
        instance.putAll(data.a().asMap());
        assertEqualMap(data.a().asMap(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void putAllWithContainedKeysButNewValuesShouldChangeMap(MapData data) throws Exception {
        Map<Key, Value> instance = newInstance(data.a());
        instance.putAll(data.aWithDifferentValues().asMap());
        SequencedMap<Key, Value> expected = new LinkedHashMap<>(data.a().asMap());
        expected.putAll(data.aWithDifferentValues().asMap());
        assertEqualMap(expected, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void putAllWithNewEntriesShouldChangeMap(MapData data) throws Exception {
        Map<Key, Value> instance = newInstance(data.a());
        instance.putAll(data.c().asMap());
        SequencedMap<Key, Value> expected = new LinkedHashMap<>(data.a().asMap());
        expected.putAll(data.c().asMap());
        assertEqualMap(expected, instance);
    }

    @SuppressWarnings({"CollectionAddedToSelf"})
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void putAllWithSelfShouldYieldSameMap(MapData data) throws Exception {
        Map<Key, Value> instance = newInstance(data.a());
        instance.putAll(instance);
        assertEqualMap(data.a(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void putAllWithSomeNewKeyShouldAddAll(MapData data) throws Exception {
        ArrayList<Map.Entry<Key, Value>> listB = new ArrayList<>(data.aWithDifferentValues.readableEntrySet().asSet());
        ArrayList<Map.Entry<Key, Value>> listC = new ArrayList<>(data.c.readableEntrySet().asSet());
        SequencedMap<Key, Value> m = new LinkedHashMap<>(data.a.asMap());
        for (Map.Entry<Key, Value> entry : listB.subList(0, listB.size() / 2)) {
            m.put(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<Key, Value> entry : listC.subList(0, listC.size() / 2)) {
            m.put(entry.getKey(), entry.getValue());
        }
        Map<Key, Value> instance = newInstance(data.a);
        instance.putAll(m);
        LinkedHashMap<Key, Value> expected = new LinkedHashMap<>(data.a.asMap());
        expected.putAll(m);
        assertEqualMap(expected, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void putWithContainedEntryShouldReturnOldValue(MapData data) throws Exception {
        Map<Key, Value> instance = newInstance(data.a());
        LinkedHashMap<Key, Value> expected = new LinkedHashMap<>(data.a().asMap());
        for (Map.Entry<Key, Value> e : data.aWithDifferentValues) {
            assertEquals(expected.get(e.getKey()), instance.put(e.getKey(), e.getValue()));
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void putWithContainedKeyButNewValueShouldReturnOldValue(MapData data) throws Exception {
        Map<Key, Value> instance = newInstance(data.a());
        LinkedHashMap<Key, Value> expected = new LinkedHashMap<>(data.a().asMap());
        for (Map.Entry<Key, Value> e : data.aWithDifferentValues) {
            assertEquals(expected.get(e.getKey()), instance.put(e.getKey(), e.getValue()));
            expected.put(e.getKey(), e.getValue());
            assertEqualMap(expected, instance);
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void putWithNewKeyShouldReturnNull(MapData data) throws Exception {
        Map<Key, Value> instance = newInstance(data.a);
        for (Map.Entry<Key, Value> e : data.c) {
            assertNull(instance.put(e.getKey(), e.getValue()));
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void removeWithContainedKeyShouldReturnOldValue(MapData data) throws Exception {
        Map<Key, Value> instance = newInstance(data.a);
        LinkedHashMap<Key, Value> expected = new LinkedHashMap<>(data.a().asMap());
        for (Map.Entry<Key, Value> e : data.aWithDifferentValues) {
            Value expectedRemoved = expected.remove(e.getKey());
            assertEquals(expectedRemoved, instance.remove(e.getKey()));
            assertEqualMap(expected, instance);
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void removeWithNewKeyShouldReturnNull(MapData data) throws Exception {
        Map<Key, Value> instance = newInstance(data.a);
        for (Map.Entry<Key, Value> e : data.c) {
            assertNull(instance.remove(e.getKey()));
            assertEqualMap(data.a.asMap(), instance);
        }
    }

    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void serializationShouldYieldSameMap(MapData data) throws Exception {
        Map<Key, Value> instance = newInstance(data.a());
        assertEqualMap(data.a(), instance);
        if (instance instanceof Serializable) {
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            try (ObjectOutputStream out = new ObjectOutputStream(buf)) {
                out.writeObject(instance);
            }
            Map<Key, Value> deserialized;
            try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buf.toByteArray()))) {
                deserialized = (Map<Key, Value>) in.readObject();
            }
            assertEqualMap(data.a(), deserialized);
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void toStringShouldContainAllEntries(MapData data) {
        Map<Key, Value> instance = newInstance();
        assertEquals("{}", instance.toString());

        instance.putAll(data.a.asMap());
        String str = instance.toString();
        assertEquals('{', str.charAt(0));
        assertEquals('}', str.charAt(str.length() - 1));
        LinkedHashSet<String> actual = new LinkedHashSet<>(Arrays.asList(str.substring(1, str.length() - 1).split(", ")));
        SequencedSet<String> expected = new LinkedHashSet<>();
        data.a.iterator().forEachRemaining(e -> expected.add(e.toString()));
        assertEquals(expected, actual);
    }

    protected abstract <K, V> Map<K, V> toClonedInstance(Map<K, V> m);
}
