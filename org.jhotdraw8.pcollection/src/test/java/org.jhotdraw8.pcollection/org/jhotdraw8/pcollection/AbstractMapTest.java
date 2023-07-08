package org.jhotdraw8.pcollection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.pcollection.readonly.ReadOnlyMap;
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
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
        Map<HashCollider, HashCollider> instance = newInstance(data.a());
        assertNotEqualMap(Collections.emptyMap(), instance);
        instance.clear();
        assertEqualMap(Collections.emptyMap(), instance);
        instance.clear();
        assertEqualMap(Collections.emptyMap(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void clearShouldYieldEmptyMap(@NonNull MapData data) {
        Map<HashCollider, HashCollider> instance = newInstance(data.a());
        assertNotEqualMap(Collections.emptyMap(), instance);
        instance.clear();
        assertEqualMap(Collections.emptyMap(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void cloneShouldYieldEqualMap(@NonNull MapData data) {
        Map<HashCollider, HashCollider> instance = newInstance(data.a());
        Map<HashCollider, HashCollider> clone = toClonedInstance(instance);
        assertEqualMap(data.a(), clone);
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void containsKeyShouldYieldExpectedValue(@NonNull MapData data) {
        Map<HashCollider, HashCollider> instance = newInstance(data.a());
        for (HashCollider k : data.a().readOnlyKeySet()) {
            assertTrue(instance.containsKey(k));
        }
        for (HashCollider k : data.c().readOnlyKeySet()) {
            assertFalse(instance.containsKey(k));
        }
        assertFalse(instance.containsKey(new Object()));
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void entryIteratorEntrySetValueShouldUpdateMap(@NonNull MapData data) {
        Map<HashCollider, HashCollider> instance = newInstance(data.a());
        Map<HashCollider, HashCollider> expected = new LinkedHashMap<>(data.a().asMap());
        for (Map.Entry<HashCollider, HashCollider> entry : instance.entrySet()) {
            entry.setValue(data.aWithDifferentValues().get(entry.getKey()));
            assertNotEqualMap(instance, expected);
            expected.put(entry.getKey(), data.aWithDifferentValues().get(entry.getKey()));
            assertEqualMap(instance, expected);
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void entryIteratorRemoveShouldRemoveEntryAndRemoveIsNotIdempotent(@NonNull MapData data) {
        Map<HashCollider, HashCollider> instance = newInstance(data.a());
        Map<HashCollider, HashCollider> expected = new LinkedHashMap<>(data.a().asMap());
        List<Map.Entry<HashCollider, HashCollider>> toRemove = new ArrayList<>(new HashSet<>(data.a().readOnlyEntrySet().asSet()));
        for (int countdown = toRemove.size(); countdown > 0; countdown--) {
            for (Iterator<Map.Entry<HashCollider, HashCollider>> i = instance.entrySet().iterator(); i.hasNext(); ) {
                Map.Entry<HashCollider, HashCollider> k = i.next();
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
        Map<HashCollider, HashCollider> instance = newInstance(data.a);
        List<Map.Entry<HashCollider, HashCollider>> actualList = new ArrayList<>();
        LinkedHashMap<HashCollider, HashCollider> actualMap = new LinkedHashMap<>();
        instance.entrySet().iterator().forEachRemaining(actualList::add);
        instance.entrySet().iterator().forEachRemaining(e -> actualMap.put(e.getKey(), e.getValue()));
        assertEquals(data.a.size(), actualList.size());
        assertEqualMap(data.a, actualMap);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void entrySetContainsExpectedEntries(@NonNull MapData data) throws Exception {
        Map<HashCollider, HashCollider> instance = newInstance(data.a);
        Set<Map.Entry<HashCollider, HashCollider>> entrySet = instance.entrySet();
        for (Map.Entry<HashCollider, HashCollider> e : data.a) {
            assertTrue(entrySet.contains(e));
        }
        for (Map.Entry<HashCollider, HashCollider> e : data.aWithDifferentValues) {
            assertFalse(entrySet.contains(e));
        }
        for (Map.Entry<HashCollider, HashCollider> e : data.c) {
            assertFalse(entrySet.contains(e));
        }
        assertTrue(entrySet.containsAll(data.a.readOnlyEntrySet().asSet()));
        assertFalse(entrySet.containsAll(data.aWithDifferentValues.readOnlyEntrySet().asSet()));
        assertFalse(entrySet.containsAll(data.c.readOnlyEntrySet().asSet()));
        LinkedHashSet<Map.Entry<HashCollider, HashCollider>> abc = new LinkedHashSet<>(data.a.readOnlyEntrySet().asSet());
        abc.addAll(data.aWithDifferentValues.readOnlyEntrySet().asSet());
        abc.addAll(data.c.readOnlyEntrySet().asSet());
        assertFalse(entrySet.containsAll(abc));
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void entrySetContainsShouldYieldExpectedValue(@NonNull MapData data) {
        Map<HashCollider, HashCollider> instance = newInstance(data.a());
        for (Map.Entry<HashCollider, HashCollider> e : data.a().readOnlyEntrySet()) {
            assertTrue(instance.entrySet().contains(e));
        }
        for (Map.Entry<HashCollider, HashCollider> e : data.aWithDifferentValues().readOnlyEntrySet()) {
            assertFalse(instance.entrySet().contains(e));
        }
        for (Map.Entry<HashCollider, HashCollider> e : data.c().readOnlyEntrySet()) {
            assertFalse(instance.entrySet().contains(e));
        }
        assertFalse(instance.entrySet().contains(new Object()));
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void entrySetRemoveAllWithContainedEntryShouldReturnTrue(@NonNull MapData data) throws Exception {
        Map<HashCollider, HashCollider> instance = newInstance(data.a);
        assertTrue(instance.entrySet().removeAll(data.a.readOnlyEntrySet().asSet()));
        assertEqualMap(Collections.emptyMap(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void entrySetRemoveAllWithEntriesThatHaveSameKeyButDifferentValueShouldReturnFalse(@NonNull MapData data) throws Exception {
        Map<HashCollider, HashCollider> instance = newInstance(data.a);
        assertFalse(instance.entrySet().removeAll(data.aWithDifferentValues.readOnlyEntrySet().asSet()));
        assertEqualMap(data.a, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void entrySetRemoveAllWithNewEntryShouldReturnFalse(@NonNull MapData data) throws Exception {
        Map<HashCollider, HashCollider> instance = newInstance(data.a);
        assertFalse(instance.entrySet().removeAll(data.c.readOnlyEntrySet().asSet()));
        assertEqualMap(data.a, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void entrySetRemoveShouldNotRemoveEntryWithDifferentKeyAndDifferentValue(@NonNull MapData data) {
        Map<HashCollider, HashCollider> instance = newInstance(data.a());
        Map<HashCollider, HashCollider> expected = new LinkedHashMap<>(data.a().asMap());
        for (Map.Entry<HashCollider, HashCollider> e : data.c().readOnlyEntrySet()) {
            assertFalse(instance.entrySet().remove(e));
            assertEqualMap(expected, instance);
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void entrySetRemoveShouldNotRemoveEntryWithSameKeyButDifferentValue(@NonNull MapData data) {
        Map<HashCollider, HashCollider> instance = newInstance(data.a());
        Map<HashCollider, HashCollider> expected = new LinkedHashMap<>(data.a().asMap());
        for (Map.Entry<HashCollider, HashCollider> e : data.aWithDifferentValues().readOnlyEntrySet()) {
            assertFalse(instance.entrySet().remove(e));
            assertEqualMap(expected, instance);
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void entrySetRemoveShouldRemoveEntryWithSameKeyAndValue(@NonNull MapData data) {
        Map<HashCollider, HashCollider> instance = newInstance(data.a());
        Map<HashCollider, HashCollider> expected = new LinkedHashMap<>(data.a().asMap());

        for (Map.Entry<HashCollider, HashCollider> e : data.a().readOnlyEntrySet()) {
            assertTrue(instance.entrySet().remove(e));
            expected.entrySet().remove(e);
            assertEqualMap(expected, instance);
        }
        assertEqualMap(Collections.emptyMap(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void equalWithThisShouldYieldTrue(@NonNull MapData data) {
        Map<HashCollider, HashCollider> instance = newInstance(data.a());
        assertEquals(instance, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void equalsWithCloneWithUpdatedEntriesShouldYieldFalse(@NonNull MapData data) throws Exception {
        Map<HashCollider, HashCollider> instance = newInstance(data.a());
        Map<HashCollider, HashCollider> instance2 = toClonedInstance(instance);
        assertEquals(instance, instance2);
        instance2.putAll(data.aWithDifferentValues().asMap());
        assertNotEquals(instance, instance2);
    }

    @SuppressWarnings("SimplifiableAssertion")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void equalsWithNullShouldYieldFalse(@NonNull MapData data) {
        Map<HashCollider, HashCollider> instance = newInstance(data.a());
        assertFalse(instance.equals(null));
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void equalsWithObjectShouldYieldFalse(@NonNull MapData data) throws Exception {
        Map<HashCollider, HashCollider> instance = newInstance(data.a());
        assertNotEquals(instance, new Object());
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void getOrDefaultWithContainedKeyShouldYieldValue(@NonNull MapData data) throws Exception {
        Map<HashCollider, HashCollider> instance = newInstance(data.a());
        HashCollider defaultValue = new HashCollider(7, -1);
        for (Map.Entry<HashCollider, HashCollider> e : data.a()) {
            assertEquals(e.getValue(), instance.getOrDefault(e.getKey(), defaultValue));
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void getOfEntryWithNullValueShouldYieldNull(@NonNull MapData data) throws Exception {
        Map<HashCollider, HashCollider> instance = newInstance(data.a());
        LinkedHashMap<HashCollider, HashCollider> expected = new LinkedHashMap<>(data.a.asMap());
        HashCollider key = new HashCollider(42, -1);
        assertNull(instance.put(key, null));
        expected.put(key, null);
        assertTrue(instance.containsKey(key));
        assertNull(instance.get(key));
        assertEqualMap(expected, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void getOrDefaultWithNonContainedKeyShouldYieldDefault(@NonNull MapData data) throws Exception {
        Map<HashCollider, HashCollider> instance = newInstance(data.a());
        HashCollider defaultValue = new HashCollider(7, -1);
        for (Map.Entry<HashCollider, HashCollider> e : data.c()) {
            assertEquals(defaultValue, instance.getOrDefault(e.getKey(), defaultValue));
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void iteratorRemoveShouldThrowIllegalStateException(@NonNull MapData data) {
        Map<HashCollider, HashCollider> instance = newInstance(data.a());
        Iterator<HashCollider> i = instance.keySet().iterator();
        assertThrows(IllegalStateException.class, i::remove);
        Iterator<HashCollider> k = instance.values().iterator();
        assertThrows(IllegalStateException.class, k::remove);
        Iterator<Map.Entry<HashCollider, HashCollider>> j = instance.entrySet().iterator();
        assertThrows(IllegalStateException.class, j::remove);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void keyIteratorRemoveShouldRemoveEntry(@NonNull MapData data) {
        Map<HashCollider, HashCollider> instance = newInstance(data.a());
        Map<HashCollider, HashCollider> expected = new LinkedHashMap<>(data.a().asMap());
        List<HashCollider> toRemove = new ArrayList<>(new HashSet<>(data.a().readOnlyKeySet().asSet()));
        while (!toRemove.isEmpty() && !expected.isEmpty()) {
            for (Iterator<HashCollider> i = instance.keySet().iterator(); i.hasNext(); ) {
                HashCollider k = i.next();
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
        Map<HashCollider, HashCollider> instance = newInstance();
        assertFalse(instance.keySet().removeAll(data.a.readOnlyKeySet().asSet()));
        assertEqualMap(Collections.emptyMap(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void keySetRemoveAllWithEmptyMapShouldReturnFalse(@NonNull MapData data) throws Exception {
        Map<HashCollider, HashCollider> instance = newInstance(data.a);
        assertFalse(instance.keySet().removeAll(Collections.<HashCollider>emptySet()));
        assertEqualMap(data.a(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void keySetRemoveAllWithContainedKeyShouldReturnTrue(@NonNull MapData data) throws Exception {
        Map<HashCollider, HashCollider> instance = newInstance(data.a);
        assertTrue(instance.keySet().removeAll(data.a.readOnlyKeySet().asSet()));
        assertEqualMap(Collections.emptyMap(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void keySetRemoveAllWithSomeContainedKeyShouldReturnTrue(@NonNull MapData data) throws Exception {
        Map<HashCollider, HashCollider> instance = newInstance(data.a);
        assertTrue(instance.keySet().removeAll(data.someAPlusSomeB().readOnlyKeySet().asSet()));
        LinkedHashMap<HashCollider, HashCollider> expected = new LinkedHashMap<>(data.a.asMap());
        expected.keySet().removeAll(data.someAPlusSomeB().readOnlyKeySet().asSet());
        assertEqualMap(expected, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void keySetRemoveAllWithNewKeyShouldReturnFalse(@NonNull MapData data) throws Exception {
        Map<HashCollider, HashCollider> instance = newInstance(data.a);
        assertFalse(instance.keySet().removeAll(data.c.readOnlyKeySet().asSet()));
        assertEqualMap(data.a, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void keySetRetainAllOfEmptyMapShouldNotChangeMap(@NonNull MapData data) throws Exception {
        Map<HashCollider, HashCollider> instance = newInstance();
        instance.keySet().retainAll(data.a().asMap().keySet());
        assertEqualMap(Collections.emptyMap(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void keySetRetainAllWithContainedKeysShouldNotChangeMap(@NonNull MapData data) throws Exception {
        Map<HashCollider, HashCollider> instance = newInstance(data.a());
        instance.keySet().retainAll(data.a().asMap().keySet());
        assertEqualMap(data.a().asMap(), instance);
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void keySetRetainAllWithEmptySetShouldClearMap(@NonNull MapData data) throws Exception {
        Map<HashCollider, HashCollider> instance = newInstance(data.a());
        instance.keySet().retainAll(Collections.emptySet());
        assertEqualMap(Collections.emptyMap(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void keySetRetainAllWithSomeContainedKeysShouldChangeMap(@NonNull MapData data) throws Exception {
        Map<HashCollider, HashCollider> instance = newInstance(data.a());
        LinkedHashMap<HashCollider, HashCollider> expected = new LinkedHashMap<>(data.a.asMap());
        instance.keySet().retainAll(data.someAPlusSomeB().asMap().keySet());
        expected.keySet().retainAll(data.someAPlusSomeB().asMap().keySet());
        assertEqualMap(expected, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void keySetRetainAllWithSomeContainedKeysShouldReturnNewInstance(@NonNull MapData data) throws Exception {
        Map<HashCollider, HashCollider> instance = newInstance(data.a());
        assertTrue(instance.keySet().retainAll(data.someAPlusSomeB().asMap().keySet()));
        LinkedHashMap<HashCollider, HashCollider> expected = new LinkedHashMap<>(data.a().asMap());
        expected.keySet().retainAll(data.someAPlusSomeB().asMap().keySet());
        assertEqualMap(expected, instance);
    }

    @Test
    public void newInstanceCapacityArgsShouldBeEmpty() {
        Map<HashCollider, HashCollider> actual = newInstance(24, 0.75f);
        LinkedHashMap<HashCollider, HashCollider> expected = new LinkedHashMap<>(24, 0.75f);
        assertEqualMap(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void newInstanceIterableArgShouldBeEqualToArg(@NonNull MapData data) {
        Map<HashCollider, HashCollider> actual = newInstance(data.a().readOnlyEntrySet());
        assertEqualMap(data.a(), actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void newInstanceMapArgsOfSameTypeShouldBeEqualToArg(@NonNull MapData data) {
        Map<HashCollider, HashCollider> actual1 = newInstance(data.a().asMap());
        Map<HashCollider, HashCollider> actual = newInstance(actual1);
        assertEqualMap(data.a(), actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void newInstanceMapArgsShouldBeEqualToArg(@NonNull MapData data) {
        Map<HashCollider, HashCollider> actual = newInstance(data.a().asMap());
        assertEqualMap(data.a(), actual);
    }

    @Test
    public void newInstanceNoArgsShouldBeEmpty() {
        Map<HashCollider, HashCollider> actual = newInstance();
        LinkedHashMap<HashCollider, HashCollider> expected = new LinkedHashMap<>();
        assertEqualMap(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void newInstanceReadOnlyMapArgShouldBeEqualToARg(@NonNull MapData data) {
        Map<HashCollider, HashCollider> actual = newInstance(data.a());
        assertEqualMap(data.a(), actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void putAllWithContainedEntriesShouldNotChangeMap(@NonNull MapData data) throws Exception {
        Map<HashCollider, HashCollider> instance = newInstance(data.a());
        instance.putAll(data.a().asMap());
        assertEqualMap(data.a().asMap(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void putAllWithContainedKeysButNewValuesShouldChangeMap(@NonNull MapData data) throws Exception {
        Map<HashCollider, HashCollider> instance = newInstance(data.a());
        instance.putAll(data.aWithDifferentValues().asMap());
        Map<HashCollider, HashCollider> expected = new LinkedHashMap<>(data.a().asMap());
        expected.putAll(data.aWithDifferentValues().asMap());
        assertEqualMap(expected, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void putAllWithNewEntriesShouldChangeMap(@NonNull MapData data) throws Exception {
        Map<HashCollider, HashCollider> instance = newInstance(data.a());
        instance.putAll(data.c().asMap());
        Map<HashCollider, HashCollider> expected = new LinkedHashMap<>(data.a().asMap());
        expected.putAll(data.c().asMap());
        assertEqualMap(expected, instance);
    }

    @SuppressWarnings({"CollectionAddedToSelf"})
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void putAllWithSelfShouldYieldSameMap(@NonNull MapData data) throws Exception {
        Map<HashCollider, HashCollider> instance = newInstance(data.a());
        instance.putAll(instance);
        assertEqualMap(data.a(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void putAllWithSomeNewKeyShouldAddAll(@NonNull MapData data) throws Exception {
        ArrayList<Map.Entry<HashCollider, HashCollider>> listB = new ArrayList<>(data.aWithDifferentValues.readOnlyEntrySet().asSet());
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

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void putWithContainedEntryShouldReturnOldValue(@NonNull MapData data) throws Exception {
        Map<HashCollider, HashCollider> instance = newInstance(data.a());
        LinkedHashMap<HashCollider, HashCollider> expected = new LinkedHashMap<>(data.a().asMap());
        for (Map.Entry<HashCollider, HashCollider> e : data.aWithDifferentValues) {
            assertEquals(expected.get(e.getKey()), instance.put(e.getKey(), e.getValue()));
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void putWithContainedKeyButNewValueShouldReturnOldValue(@NonNull MapData data) throws Exception {
        Map<HashCollider, HashCollider> instance = newInstance(data.a());
        LinkedHashMap<HashCollider, HashCollider> expected = new LinkedHashMap<>(data.a().asMap());
        for (Map.Entry<HashCollider, HashCollider> e : data.aWithDifferentValues) {
            assertEquals(expected.get(e.getKey()), instance.put(e.getKey(), e.getValue()));
            expected.put(e.getKey(), e.getValue());
            assertEqualMap(expected, instance);
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void putWithNewKeyShouldReturnNull(@NonNull MapData data) throws Exception {
        Map<HashCollider, HashCollider> instance = newInstance(data.a);
        for (Map.Entry<HashCollider, HashCollider> e : data.c) {
            assertNull(instance.put(e.getKey(), e.getValue()));
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void removeWithContainedKeyShouldReturnOldValue(@NonNull MapData data) throws Exception {
        Map<HashCollider, HashCollider> instance = newInstance(data.a);
        LinkedHashMap<HashCollider, HashCollider> expected = new LinkedHashMap<>(data.a().asMap());
        for (Map.Entry<HashCollider, HashCollider> e : data.aWithDifferentValues) {
            HashCollider expectedRemoved = expected.remove(e.getKey());
            assertEquals(expectedRemoved, instance.remove(e.getKey()));
            assertEqualMap(expected, instance);
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void removeWithNewKeyShouldReturnNull(@NonNull MapData data) throws Exception {
        Map<HashCollider, HashCollider> instance = newInstance(data.a);
        for (Map.Entry<HashCollider, HashCollider> e : data.c) {
            assertNull(instance.remove(e.getKey()));
            assertEqualMap(data.a.asMap(), instance);
        }
    }

    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void serializationShouldYieldSameMap(@NonNull MapData data) throws Exception {
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
    public void toStringShouldContainAllEntries(@NonNull MapData data) {
        Map<HashCollider, HashCollider> instance = newInstance();
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
