package org.jhotdraw8.immutable_collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.immutable_collection.readonly.ReadOnlyMap;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.*;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

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

    protected <K, V> void assertEqualMap(ReadOnlyMap<K, V> expected, Map<K, V> actual) {
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

    /**
     * Creates a new instance with the specified map.
     */
    abstract <K, V> @NonNull Map<K, V> newInstance(@NonNull Iterable<Map.Entry<K, V>> m);

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void clearShouldBeIdempotent(@NonNull MapData data) {
        Map<Key, Key> instance = newInstance(data.a());
        assertNotEqualMap(Collections.emptyMap(), instance);
        instance.clear();
        assertEqualMap(Collections.emptyMap(), instance);
        instance.clear();
        assertEqualMap(Collections.emptyMap(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void clearShouldYieldEmptyMap(@NonNull MapData data) {
        Map<Key, Key> instance = newInstance(data.a());
        assertNotEqualMap(Collections.emptyMap(), instance);
        instance.clear();
        assertEqualMap(Collections.emptyMap(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void cloneShouldYieldEqualMap(@NonNull MapData data) {
        Map<Key, Key> instance = newInstance(data.a());
        Map<Key, Key> clone = toClonedInstance(instance);
        assertEqualMap(data.a(), clone);
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void containsKeyShouldYieldExpectedValue(@NonNull MapData data) {
        Map<Key, Key> instance = newInstance(data.a());
        for (Key k : data.a().readOnlyKeySet()) {
            assertTrue(instance.containsKey(k));
        }
        for (Key k : data.c().readOnlyKeySet()) {
            assertFalse(instance.containsKey(k));
        }
        assertFalse(instance.containsKey(new Object()));
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void entryIteratorEntrySetValueShouldUpdateMap(@NonNull MapData data) {
        Map<Key, Key> instance = newInstance(data.a());
        Map<Key, Key> expected = new LinkedHashMap<>(data.a().asMap());
        for (Map.Entry<Key, Key> entry : instance.entrySet()) {
            entry.setValue(data.aWithDifferentValues().get(entry.getKey()));
            assertNotEqualMap(instance, expected);
            expected.put(entry.getKey(), data.aWithDifferentValues().get(entry.getKey()));
            assertEqualMap(instance, expected);
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void entryIteratorRemoveShouldRemoveEntryAndRemoveIsNotIdempotent(@NonNull MapData data) {
        Map<Key, Key> instance = newInstance(data.a());
        Map<Key, Key> expected = new LinkedHashMap<>(data.a().asMap());
        List<Map.Entry<Key, Key>> toRemove = new ArrayList<>(new HashSet<>(data.a().readOnlyEntrySet().asSet()));
        for (int countdown = toRemove.size(); countdown > 0; countdown--) {
            for (Iterator<Map.Entry<Key, Key>> i = instance.entrySet().iterator(); i.hasNext(); ) {
                Map.Entry<Key, Key> k = i.next();
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
    public void entryIteratorShouldYieldExpectedEntries(@NonNull MapData data) throws Exception {
        Map<Key, Key> instance = newInstance(data.a);
        List<Map.Entry<Key, Key>> actualList = new ArrayList<>();
        LinkedHashMap<Key, Key> actualMap = new LinkedHashMap<>();
        instance.entrySet().iterator().forEachRemaining(actualList::add);
        instance.entrySet().iterator().forEachRemaining(e -> actualMap.put(e.getKey(), e.getValue()));
        assertEquals(data.a.size(), actualList.size());
        assertEqualMap(data.a, actualMap);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void entrySetContainsExpectedEntries(@NonNull MapData data) throws Exception {
        Map<Key, Key> instance = newInstance(data.a);
        Set<Map.Entry<Key, Key>> entrySet = instance.entrySet();
        for (Map.Entry<Key, Key> e : data.a) {
            assertTrue(entrySet.contains(e));
        }
        for (Map.Entry<Key, Key> e : data.aWithDifferentValues) {
            assertFalse(entrySet.contains(e));
        }
        for (Map.Entry<Key, Key> e : data.c) {
            assertFalse(entrySet.contains(e));
        }
        assertTrue(entrySet.containsAll(data.a.readOnlyEntrySet().asSet()));
        assertFalse(entrySet.containsAll(data.aWithDifferentValues.readOnlyEntrySet().asSet()));
        assertFalse(entrySet.containsAll(data.c.readOnlyEntrySet().asSet()));
        LinkedHashSet<Map.Entry<Key, Key>> abc = new LinkedHashSet<>(data.a.readOnlyEntrySet().asSet());
        abc.addAll(data.aWithDifferentValues.readOnlyEntrySet().asSet());
        abc.addAll(data.c.readOnlyEntrySet().asSet());
        assertFalse(entrySet.containsAll(abc));
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void entrySetContainsShouldYieldExpectedValue(@NonNull MapData data) {
        Map<Key, Key> instance = newInstance(data.a());
        for (Map.Entry<Key, Key> e : data.a().readOnlyEntrySet()) {
            assertTrue(instance.entrySet().contains(e));
        }
        for (Map.Entry<Key, Key> e : data.aWithDifferentValues().readOnlyEntrySet()) {
            assertFalse(instance.entrySet().contains(e));
        }
        for (Map.Entry<Key, Key> e : data.c().readOnlyEntrySet()) {
            assertFalse(instance.entrySet().contains(e));
        }
        assertFalse(instance.entrySet().contains(new Object()));
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void entrySetRemoveAllWithContainedEntryShouldReturnTrue(@NonNull MapData data) throws Exception {
        Map<Key, Key> instance = newInstance(data.a);
        assertTrue(instance.entrySet().removeAll(data.a.readOnlyEntrySet().asSet()));
        assertEqualMap(Collections.emptyMap(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void entrySetRemoveAllWithEntriesThatHaveSameKeyButDifferentValueShouldReturnFalse(@NonNull MapData data) throws Exception {
        Map<Key, Key> instance = newInstance(data.a);
        assertFalse(instance.entrySet().removeAll(data.aWithDifferentValues.readOnlyEntrySet().asSet()));
        assertEqualMap(data.a, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void entrySetRemoveAllWithNewEntryShouldReturnFalse(@NonNull MapData data) throws Exception {
        Map<Key, Key> instance = newInstance(data.a);
        assertFalse(instance.entrySet().removeAll(data.c.readOnlyEntrySet().asSet()));
        assertEqualMap(data.a, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void entrySetRemoveShouldNotRemoveEntryWithDifferentKeyAndDifferentValue(@NonNull MapData data) {
        Map<Key, Key> instance = newInstance(data.a());
        Map<Key, Key> expected = new LinkedHashMap<>(data.a().asMap());
        for (Map.Entry<Key, Key> e : data.c().readOnlyEntrySet()) {
            assertFalse(instance.entrySet().remove(e));
            assertEqualMap(expected, instance);
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void entrySetRemoveShouldNotRemoveEntryWithSameKeyButDifferentValue(@NonNull MapData data) {
        Map<Key, Key> instance = newInstance(data.a());
        Map<Key, Key> expected = new LinkedHashMap<>(data.a().asMap());
        for (Map.Entry<Key, Key> e : data.aWithDifferentValues().readOnlyEntrySet()) {
            assertFalse(instance.entrySet().remove(e));
            assertEqualMap(expected, instance);
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void entrySetRemoveShouldRemoveEntryWithSameKeyAndValue(@NonNull MapData data) {
        Map<Key, Key> instance = newInstance(data.a());
        Map<Key, Key> expected = new LinkedHashMap<>(data.a().asMap());

        for (Map.Entry<Key, Key> e : data.a().readOnlyEntrySet()) {
            assertTrue(instance.entrySet().remove(e));
            expected.entrySet().remove(e);
            assertEqualMap(expected, instance);
        }
        assertEqualMap(Collections.emptyMap(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void equalWithThisShouldYieldTrue(@NonNull MapData data) {
        Map<Key, Key> instance = newInstance(data.a());
        assertEquals(instance, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void equalsWithCloneWithUpdatedEntriesShouldYieldFalse(@NonNull MapData data) throws Exception {
        Map<Key, Key> instance = newInstance(data.a());
        Map<Key, Key> instance2 = toClonedInstance(instance);
        assertEquals(instance, instance2);
        instance2.putAll(data.aWithDifferentValues().asMap());
        assertNotEquals(instance, instance2);
    }

    @SuppressWarnings("SimplifiableAssertion")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void equalsWithNullShouldYieldFalse(@NonNull MapData data) {
        Map<Key, Key> instance = newInstance(data.a());
        assertFalse(instance.equals(null));
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void equalsWithObjectShouldYieldFalse(@NonNull MapData data) throws Exception {
        Map<Key, Key> instance = newInstance(data.a());
        assertNotEquals(instance, new Object());
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void getOrDefaultWithContainedKeyShouldYieldValue(@NonNull MapData data) throws Exception {
        Map<Key, Key> instance = newInstance(data.a());
        Key defaultValue = new Key(7, -1);
        for (Map.Entry<Key, Key> e : data.a()) {
            assertEquals(e.getValue(), instance.getOrDefault(e.getKey(), defaultValue));
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void getOfEntryWithNullValueShouldYieldNull(@NonNull MapData data) throws Exception {
        Map<Key, Key> instance = newInstance(data.a());
        LinkedHashMap<Key, Key> expected = new LinkedHashMap<>(data.a.asMap());
        Key key = new Key(42, -1);
        assertNull(instance.put(key, null));
        expected.put(key, null);
        assertTrue(instance.containsKey(key));
        assertNull(instance.get(key));
        assertEqualMap(expected, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void getOrDefaultWithNonContainedKeyShouldYieldDefault(@NonNull MapData data) throws Exception {
        Map<Key, Key> instance = newInstance(data.a());
        Key defaultValue = new Key(7, -1);
        for (Map.Entry<Key, Key> e : data.c()) {
            assertEquals(defaultValue, instance.getOrDefault(e.getKey(), defaultValue));
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void iteratorRemoveShouldThrowIllegalStateException(@NonNull MapData data) {
        Map<Key, Key> instance = newInstance(data.a());
        Iterator<Key> i = instance.keySet().iterator();
        assertThrows(IllegalStateException.class, i::remove);
        Iterator<Key> k = instance.values().iterator();
        assertThrows(IllegalStateException.class, k::remove);
        Iterator<Map.Entry<Key, Key>> j = instance.entrySet().iterator();
        assertThrows(IllegalStateException.class, j::remove);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void keyIteratorRemoveShouldRemoveEntry(@NonNull MapData data) {
        Map<Key, Key> instance = newInstance(data.a());
        Map<Key, Key> expected = new LinkedHashMap<>(data.a().asMap());
        List<Key> toRemove = new ArrayList<>(new HashSet<>(data.a().readOnlyKeySet().asSet()));
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
    public void keySetRemoveAllOfEmptyMapShouldReturnFalse(@NonNull MapData data) throws Exception {
        Map<Key, Key> instance = newInstance();
        assertFalse(instance.keySet().removeAll(data.a.readOnlyKeySet().asSet()));
        assertEqualMap(Collections.emptyMap(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void keySetRemoveAllWithEmptyMapShouldReturnFalse(@NonNull MapData data) throws Exception {
        Map<Key, Key> instance = newInstance(data.a);
        assertFalse(instance.keySet().removeAll(Collections.<Key>emptySet()));
        assertEqualMap(data.a(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void keySetRemoveAllWithContainedKeyShouldReturnTrue(@NonNull MapData data) throws Exception {
        Map<Key, Key> instance = newInstance(data.a);
        assertTrue(instance.keySet().removeAll(data.a.readOnlyKeySet().asSet()));
        assertEqualMap(Collections.emptyMap(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void keySetRemoveAllWithSomeContainedKeyShouldReturnTrue(@NonNull MapData data) throws Exception {
        Map<Key, Key> instance = newInstance(data.a);
        assertTrue(instance.keySet().removeAll(data.someAPlusSomeB().readOnlyKeySet().asSet()));
        LinkedHashMap<Key, Key> expected = new LinkedHashMap<>(data.a.asMap());
        expected.keySet().removeAll(data.someAPlusSomeB().readOnlyKeySet().asSet());
        assertEqualMap(expected, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void keySetRemoveAllWithNewKeyShouldReturnFalse(@NonNull MapData data) throws Exception {
        Map<Key, Key> instance = newInstance(data.a);
        assertFalse(instance.keySet().removeAll(data.c.readOnlyKeySet().asSet()));
        assertEqualMap(data.a, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void keySetRetainAllOfEmptyMapShouldNotChangeMap(@NonNull MapData data) throws Exception {
        Map<Key, Key> instance = newInstance();
        instance.keySet().retainAll(data.a().asMap().keySet());
        assertEqualMap(Collections.emptyMap(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void keySetRetainAllWithContainedKeysShouldNotChangeMap(@NonNull MapData data) throws Exception {
        Map<Key, Key> instance = newInstance(data.a());
        instance.keySet().retainAll(data.a().asMap().keySet());
        assertEqualMap(data.a().asMap(), instance);
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void keySetRetainAllWithEmptySetShouldClearMap(@NonNull MapData data) throws Exception {
        Map<Key, Key> instance = newInstance(data.a());
        instance.keySet().retainAll(Collections.emptySet());
        assertEqualMap(Collections.emptyMap(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void keySetRetainAllWithSomeContainedKeysShouldChangeMap(@NonNull MapData data) throws Exception {
        Map<Key, Key> instance = newInstance(data.a());
        LinkedHashMap<Key, Key> expected = new LinkedHashMap<>(data.a.asMap());
        instance.keySet().retainAll(data.someAPlusSomeB().asMap().keySet());
        expected.keySet().retainAll(data.someAPlusSomeB().asMap().keySet());
        assertEqualMap(expected, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void keySetRetainAllWithSomeContainedKeysShouldReturnNewInstance(@NonNull MapData data) throws Exception {
        Map<Key, Key> instance = newInstance(data.a());
        assertTrue(instance.keySet().retainAll(data.someAPlusSomeB().asMap().keySet()));
        LinkedHashMap<Key, Key> expected = new LinkedHashMap<>(data.a().asMap());
        expected.keySet().retainAll(data.someAPlusSomeB().asMap().keySet());
        assertEqualMap(expected, instance);
    }

    @Test
    public void newInstanceCapacityArgsShouldBeEmpty() {
        Map<Key, Key> actual = newInstance(24, 0.75f);
        LinkedHashMap<Key, Key> expected = new LinkedHashMap<>(24, 0.75f);
        assertEqualMap(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void newInstanceIterableArgShouldBeEqualToArg(@NonNull MapData data) {
        Map<Key, Key> actual = newInstance(data.a().readOnlyEntrySet());
        assertEqualMap(data.a(), actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void newInstanceMapArgsOfSameTypeShouldBeEqualToArg(@NonNull MapData data) {
        Map<Key, Key> actual1 = newInstance(data.a().asMap());
        Map<Key, Key> actual = newInstance(actual1);
        assertEqualMap(data.a(), actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void newInstanceMapArgsShouldBeEqualToArg(@NonNull MapData data) {
        Map<Key, Key> actual = newInstance(data.a().asMap());
        assertEqualMap(data.a(), actual);
    }

    @Test
    public void newInstanceNoArgsShouldBeEmpty() {
        Map<Key, Key> actual = newInstance();
        LinkedHashMap<Key, Key> expected = new LinkedHashMap<>();
        assertEqualMap(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void newInstanceReadOnlyMapArgShouldBeEqualToARg(@NonNull MapData data) {
        Map<Key, Key> actual = newInstance(data.a());
        assertEqualMap(data.a(), actual);
    }

    @Test
    public void shouldSupportNullKeyNullValue() throws Exception {
        Map<Key, Key> instance = newInstance();
        assertFalse(instance.containsKey(null));
        assertFalse(instance.containsValue(null));
        Key oldValue = instance.put(null, null);
        assertNull(oldValue);
        assertTrue(instance.containsKey(null));
        assertTrue(instance.containsValue(null));
    }

    @Test
    public void spliteratorCharacteristicsShouldHaveNonNull() throws Exception {
        Map<Key, Key> instance = newInstance();
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
    public void putAllWithContainedEntriesShouldNotChangeMap(@NonNull MapData data) throws Exception {
        Map<Key, Key> instance = newInstance(data.a());
        instance.putAll(data.a().asMap());
        assertEqualMap(data.a().asMap(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void putAllWithContainedKeysButNewValuesShouldChangeMap(@NonNull MapData data) throws Exception {
        Map<Key, Key> instance = newInstance(data.a());
        instance.putAll(data.aWithDifferentValues().asMap());
        Map<Key, Key> expected = new LinkedHashMap<>(data.a().asMap());
        expected.putAll(data.aWithDifferentValues().asMap());
        assertEqualMap(expected, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void putAllWithNewEntriesShouldChangeMap(@NonNull MapData data) throws Exception {
        Map<Key, Key> instance = newInstance(data.a());
        instance.putAll(data.c().asMap());
        Map<Key, Key> expected = new LinkedHashMap<>(data.a().asMap());
        expected.putAll(data.c().asMap());
        assertEqualMap(expected, instance);
    }

    @SuppressWarnings({"CollectionAddedToSelf"})
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void putAllWithSelfShouldYieldSameMap(@NonNull MapData data) throws Exception {
        Map<Key, Key> instance = newInstance(data.a());
        instance.putAll(instance);
        assertEqualMap(data.a(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void putAllWithSomeNewKeyShouldAddAll(@NonNull MapData data) throws Exception {
        ArrayList<Map.Entry<Key, Key>> listB = new ArrayList<>(data.aWithDifferentValues.readOnlyEntrySet().asSet());
        ArrayList<Map.Entry<Key, Key>> listC = new ArrayList<>(data.c.readOnlyEntrySet().asSet());
        Map<Key, Key> m = new LinkedHashMap<>(data.a.asMap());
        for (Map.Entry<Key, Key> entry : listB.subList(0, listB.size() / 2)) {
            m.put(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<Key, Key> entry : listC.subList(0, listC.size() / 2)) {
            m.put(entry.getKey(), entry.getValue());
        }
        Map<Key, Key> instance = newInstance(data.a);
        instance.putAll(m);
        LinkedHashMap<Key, Key> expected = new LinkedHashMap<>(data.a.asMap());
        expected.putAll(m);
        assertEqualMap(expected, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void putWithContainedEntryShouldReturnOldValue(@NonNull MapData data) throws Exception {
        Map<Key, Key> instance = newInstance(data.a());
        LinkedHashMap<Key, Key> expected = new LinkedHashMap<>(data.a().asMap());
        for (Map.Entry<Key, Key> e : data.aWithDifferentValues) {
            assertEquals(expected.get(e.getKey()), instance.put(e.getKey(), e.getValue()));
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void putWithContainedKeyButNewValueShouldReturnOldValue(@NonNull MapData data) throws Exception {
        Map<Key, Key> instance = newInstance(data.a());
        LinkedHashMap<Key, Key> expected = new LinkedHashMap<>(data.a().asMap());
        for (Map.Entry<Key, Key> e : data.aWithDifferentValues) {
            assertEquals(expected.get(e.getKey()), instance.put(e.getKey(), e.getValue()));
            expected.put(e.getKey(), e.getValue());
            assertEqualMap(expected, instance);
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void putWithNewKeyShouldReturnNull(@NonNull MapData data) throws Exception {
        Map<Key, Key> instance = newInstance(data.a);
        for (Map.Entry<Key, Key> e : data.c) {
            assertNull(instance.put(e.getKey(), e.getValue()));
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void removeWithContainedKeyShouldReturnOldValue(@NonNull MapData data) throws Exception {
        Map<Key, Key> instance = newInstance(data.a);
        LinkedHashMap<Key, Key> expected = new LinkedHashMap<>(data.a().asMap());
        for (Map.Entry<Key, Key> e : data.aWithDifferentValues) {
            Key expectedRemoved = expected.remove(e.getKey());
            assertEquals(expectedRemoved, instance.remove(e.getKey()));
            assertEqualMap(expected, instance);
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void removeWithNewKeyShouldReturnNull(@NonNull MapData data) throws Exception {
        Map<Key, Key> instance = newInstance(data.a);
        for (Map.Entry<Key, Key> e : data.c) {
            assertNull(instance.remove(e.getKey()));
            assertEqualMap(data.a.asMap(), instance);
        }
    }

    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void serializationShouldYieldSameMap(@NonNull MapData data) throws Exception {
        Map<Key, Key> instance = newInstance(data.a());
        assertEqualMap(data.a(), instance);
        if (instance instanceof Serializable) {
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            try (ObjectOutputStream out = new ObjectOutputStream(buf)) {
                out.writeObject(instance);
            }
            Map<Key, Key> deserialized;
            try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buf.toByteArray()))) {
                deserialized = (Map<Key, Key>) in.readObject();
            }
            assertEqualMap(data.a(), deserialized);
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void toStringShouldContainAllEntries(@NonNull MapData data) {
        Map<Key, Key> instance = newInstance();
        assertEquals("{}", instance.toString());

        instance.putAll(data.a.asMap());
        String str = instance.toString();
        assertEquals('{', str.charAt(0));
        assertEquals('}', str.charAt(str.length() - 1));
        LinkedHashSet<String> actual = new LinkedHashSet<>(Arrays.asList(str.substring(1, str.length() - 1).split(", ")));
        Set<String> expected = new LinkedHashSet<>();
        data.a.iterator().forEachRemaining(e -> expected.add(e.toString()));
        assertEquals(expected, actual);
    }

    protected abstract <K, V> @NonNull Map<K, V> toClonedInstance(@NonNull Map<K, V> m);
}
