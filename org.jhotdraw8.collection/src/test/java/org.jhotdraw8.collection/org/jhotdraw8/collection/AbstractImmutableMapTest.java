package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.immutable.ImmutableMap;
import org.jhotdraw8.collection.readonly.ReadOnlyMap;
import org.jhotdraw8.collection.readonly.ReadOnlySet;
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
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class AbstractImmutableMapTest {

    /**
     * Creates a new empty instance.
     */
    protected abstract <K, V> @NonNull ImmutableMap<K, V> newInstance();


    /**
     * Creates a new instance with the specified map.
     */
    protected abstract <K, V> @NonNull ImmutableMap<K, V> newInstance(@NonNull Map<K, V> m);

    protected abstract <K, V> @NonNull ImmutableMap<K, V> newInstance(@NonNull ReadOnlyMap<K, V> m);


    protected abstract <K, V> @NonNull ImmutableMap<K, V> toClonedInstance(@NonNull ImmutableMap<K, V> m);

    /**
     * Creates a new instance with the specified map.
     */
    protected abstract <K, V> @NonNull ImmutableMap<K, V> newInstance(@NonNull Iterable<Map.Entry<K, V>> m);

    public static Stream<MapData> dataProvider() {
        return Stream.of(
                NO_COLLISION, ALL_COLLISION, SOME_COLLISION
        );
    }

    private static final MapData NO_COLLISION = MapData.newData("no collisions", -1, 32, 100_000);
    private static final MapData ALL_COLLISION = MapData.newData("all collisions", 0, 32, 100_000);
    private static final MapData SOME_COLLISION = MapData.newData("some collisions", 0x55555555, 32, 100_000);


    protected <K, V> void assertEqualMap(ReadOnlyMap<K, V> expected, ImmutableMap<K, V> actual) {
        assertEqualMap(expected.asMap(), actual);
    }

    protected <K, V> void assertEqualMap(Map<K, V> expected, ImmutableMap<K, V> actual) {
        assertEquals(expected.size(), actual.size());
        assertEquals(expected.isEmpty(), actual.isEmpty());
        assertEquals(expected.hashCode(), actual.hashCode());
        assertEquals(expected, actual.asMap());
        assertEquals(actual.asMap(), expected);
        assertEquals(expected.entrySet(), actual.readOnlyEntrySet().asSet());
        assertEquals(expected.keySet(), actual.readOnlyKeySet().asSet());
        assertEquals(new LinkedHashSet<>(expected.values()),
                new LinkedHashSet<>(actual.readOnlyValues().asCollection()));
    }

    protected <K, V> void assertNotEqualMap(Map<K, V> expected, ImmutableMap<K, V> actual) {
        assertNotEquals(expected, actual);
        assertNotEquals(actual, expected);
        assertNotEquals(expected.entrySet(), actual.readOnlyEntrySet().asSet());
    }

    @Test
    public void testNewInstanceNoArgsShouldBeEmpty() {
        ImmutableMap<HashCollider, HashCollider> actual = newInstance();
        LinkedHashMap<HashCollider, HashCollider> expected = new LinkedHashMap<>();
        assertEqualMap(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testNewInstanceMapArgsShouldBeEqualToArg(MapData data) {
        ImmutableMap<HashCollider, HashCollider> actual = newInstance(data.a().asMap());
        assertEqualMap(data.a(), actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testNewInstanceMapArgsOfSameTypeShouldBeEqualToArg(MapData data) {
        ImmutableMap<HashCollider, HashCollider> actual1 = newInstance(data.a().asMap());
        ImmutableMap<HashCollider, HashCollider> actual = newInstance(actual1);
        assertEqualMap(data.a(), actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testNewInstanceReadOnlyMapArgShouldBeEqualToARg(MapData data) {
        ImmutableMap<HashCollider, HashCollider> actual = newInstance(data.a());
        assertEqualMap(data.a(), actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testNewInstanceIterableArgShouldBeEqualToArg(MapData data) {
        ImmutableMap<HashCollider, HashCollider> actual = newInstance(data.a().readOnlyEntrySet());
        assertEqualMap(data.a(), actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testCopyClearShouldYieldEmptyMap(MapData data) {
        ImmutableMap<HashCollider, HashCollider> instance = newInstance(data.a());
        assertNotEqualMap(Collections.emptyMap(), instance);
        ImmutableMap<HashCollider, HashCollider> instance2 = instance.clear();
        assertNotSame(instance, instance2);
        assertEqualMap(Collections.emptyMap(), instance2);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testCopyClearShouldBeIdempotent(MapData data) {
        ImmutableMap<HashCollider, HashCollider> instance = newInstance(data.a());
        assertNotEqualMap(Collections.emptyMap(), instance);
        instance = instance.clear();
        assertEqualMap(Collections.emptyMap(), instance);
        ImmutableMap<HashCollider, HashCollider> instance2 = instance.clear();
        assertSame(instance, instance2);
        assertEqualMap(Collections.emptyMap(), instance2);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testCloneShouldYieldEqualMap(MapData data) {
        ImmutableMap<HashCollider, HashCollider> instance = newInstance(data.a());
        ImmutableMap<HashCollider, HashCollider> clone = toClonedInstance(instance);
        assertEqualMap(data.a(), clone);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testContainsKeyShouldYieldExpectedValue(MapData data) {
        ImmutableMap<HashCollider, HashCollider> instance = newInstance(data.a());
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
    public void testReadOnlyEntrySetContainsShouldYieldExpectedValue(MapData data) {
        ImmutableMap<HashCollider, HashCollider> instance = newInstance(data.a());
        for (Map.Entry<HashCollider, HashCollider> e : data.a().readOnlyEntrySet()) {
            assertTrue(instance.readOnlyEntrySet().contains(e));
        }
        for (Map.Entry<HashCollider, HashCollider> e : data.b().readOnlyEntrySet()) {
            assertFalse(instance.readOnlyEntrySet().contains(e));
        }
        for (Map.Entry<HashCollider, HashCollider> e : data.c().readOnlyEntrySet()) {
            assertFalse(instance.readOnlyEntrySet().contains(e));
        }
        assertFalse(instance.readOnlyEntrySet().contains(new Object()));
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testIteratorRemoveShouldThrowException(MapData data) {
        ImmutableMap<HashCollider, HashCollider> instance = newInstance(data.a());
        Iterator<HashCollider> i = instance.readOnlyKeySet().iterator();
        assertThrows(Exception.class, i::remove);
        Iterator<HashCollider> k = instance.readOnlyValues().iterator();
        assertThrows(Exception.class, k::remove);
        Iterator<Map.Entry<HashCollider, HashCollider>> j = instance.readOnlyEntrySet().iterator();
        assertThrows(Exception.class, j::remove);
    }

    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testSerializationShouldYieldSameMap(MapData data) throws Exception {
        ImmutableMap<HashCollider, HashCollider> instance = newInstance(data.a());
        assertEqualMap(data.a(), instance);
        if (instance instanceof Serializable) {
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            try (ObjectOutputStream out = new ObjectOutputStream(buf)) {
                out.writeObject(instance);
            }
            ImmutableMap<HashCollider, HashCollider> deserialized;
            try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buf.toByteArray()))) {
                deserialized = (ImmutableMap<HashCollider, HashCollider>) in.readObject();
            }
            assertEqualMap(data.a(), deserialized);
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testEqualWithThisShouldYieldTrue(MapData data) {
        ImmutableMap<HashCollider, HashCollider> instance = newInstance(data.a());
        assertEquals(instance, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testEqualsWithCloneWithUpdatedEntriesShouldYieldFalse(MapData data) throws Exception {
        ImmutableMap<HashCollider, HashCollider> instance = newInstance(data.a());
        ImmutableMap<HashCollider, HashCollider> instance2 = toClonedInstance(instance);
        assertEquals(instance, instance2);

        // WHEN instance3 has not the same size as instance2
        ImmutableMap<HashCollider, HashCollider> instance3 = instance2.putAll(data.b().asMap());
        assertNotEquals(instance2.size(), instance3.size());
        assertNotSame(instance2, instance3);
        assertNotEquals(instance, instance3);
        assertNotEquals(instance2, instance3);

        // WHEN instance4 has the same size as instance
        assertEquals(data.a().size(), data.b().size());
        ImmutableMap<HashCollider, HashCollider> instance4 = newInstance(data.b());
        assertNotEquals(instance, instance4);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testCopyPutWithNewKeyShouldReturnNewInstance(MapData data) throws Exception {
        ImmutableMap<HashCollider, HashCollider> instance = newInstance(data.a);
        LinkedHashMap<HashCollider, HashCollider> expected = new LinkedHashMap<>(data.a().asMap());
        for (Map.Entry<HashCollider, HashCollider> e : data.c) {
            ImmutableMap<HashCollider, HashCollider> instance2 = instance.put(e.getKey(), e.getValue());
            assertNotSame(instance, instance2);
            expected.put(e.getKey(), e.getValue());
            assertEqualMap(expected, instance2);
            instance = instance2;
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testCopyPutWithContainedKeyButNewValueShouldReturnNewInstance(MapData data) throws Exception {
        ImmutableMap<HashCollider, HashCollider> instance = newInstance(data.a());
        LinkedHashMap<HashCollider, HashCollider> expected = new LinkedHashMap<>(data.a().asMap());
        for (Map.Entry<HashCollider, HashCollider> e : data.b) {
            ImmutableMap<HashCollider, HashCollider> instance2 = instance.put(e.getKey(), e.getValue());
            assertNotSame(instance, instance2);
            assertEqualMap(expected, instance);
            expected.put(e.getKey(), e.getValue());
            assertEqualMap(expected, instance2);
            instance = instance2;
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testCopyRemoveWithNewKeyShouldReturnThis(MapData data) throws Exception {
        ImmutableMap<HashCollider, HashCollider> instance = newInstance(data.a);
        for (Map.Entry<HashCollider, HashCollider> e : data.c) {
            assertSame(instance, instance.remove(e.getKey()));
            assertEqualMap(data.a.asMap(), instance);
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testCopyRemoveWithContainedKeyShouldReturnNewInstance(MapData data) throws Exception {
        ImmutableMap<HashCollider, HashCollider> instance = newInstance(data.a);
        LinkedHashMap<HashCollider, HashCollider> expected = new LinkedHashMap<>(data.a().asMap());
        for (Map.Entry<HashCollider, HashCollider> e : data.a) {
            ImmutableMap<HashCollider, HashCollider> instance2 = instance.remove(e.getKey());
            assertNotSame(instance, instance2);
            assertEqualMap(expected, instance);
            expected.remove(e.getKey());
            assertEqualMap(expected, instance2);
            instance = instance2;
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testEntrySetContainsExpectedEntries(MapData data) throws Exception {
        ImmutableMap<HashCollider, HashCollider> instance = newInstance(data.a);
        ReadOnlySet<Map.Entry<HashCollider, HashCollider>> entrySet = instance.readOnlyEntrySet();
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
    public void testEntryIteratorShouldYieldExpectedEntries(MapData data) throws Exception {
        ImmutableMap<HashCollider, HashCollider> instance = newInstance(data.a);
        List<Map.Entry<HashCollider, HashCollider>> actualList = new ArrayList<>();
        LinkedHashMap<HashCollider, HashCollider> actualMap = new LinkedHashMap<>();
        instance.readOnlyEntrySet().iterator().forEachRemaining(actualList::add);
        instance.readOnlyEntrySet().iterator().forEachRemaining(e -> actualMap.put(e.getKey(), e.getValue()));
        assertEquals(data.a.size(), actualList.size());
        assertEqualMap(data.a, newInstance(actualMap));
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testCopyPutWithContainedEntryShouldReturnThis(MapData data) throws Exception {
        ImmutableMap<HashCollider, HashCollider> instance = newInstance(data.a());
        LinkedHashMap<HashCollider, HashCollider> expected = new LinkedHashMap<>(data.a().asMap());
        for (Map.Entry<HashCollider, HashCollider> e : data.a) {
            ImmutableMap<HashCollider, HashCollider> instance2 = instance.put(e.getKey(), e.getValue());
            assertSame(instance, instance2);
            assertEqualMap(expected, instance2);
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testCopyPutAllWithContainedEntriesShouldReturnThis(MapData data) throws Exception {
        ImmutableMap<HashCollider, HashCollider> instance = newInstance(data.a());
        ImmutableMap<HashCollider, HashCollider> instance2 = instance.putAll(data.a().asMap());
        assertSame(instance, instance2);
        assertEqualMap(data.a(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testCopyPutAllWithNewEntriesShouldReturnNewInstance(MapData data) throws Exception {
        ImmutableMap<HashCollider, HashCollider> instance = newInstance(data.a());
        ImmutableMap<HashCollider, HashCollider> instance2 = instance.putAll(data.c().asMap());
        assertNotSame(instance, instance2);
        Map<HashCollider, HashCollider> expected = new LinkedHashMap<>(data.a().asMap());
        assertEqualMap(expected, instance);
        expected.putAll(data.c().asMap());
        assertEqualMap(expected, instance2);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testCopyPutAllWithContainedKeysButNewValuesShouldReturnNewInstance(MapData data) throws Exception {
        ImmutableMap<HashCollider, HashCollider> instance = newInstance(data.a());
        ImmutableMap<HashCollider, HashCollider> instance2 = instance.putAll(data.b().asMap());
        assertNotSame(instance, instance2);
        Map<HashCollider, HashCollider> expected = new LinkedHashMap<>(data.a().asMap());
        assertEqualMap(expected, instance);
        expected.putAll(data.b().asMap());
        assertEqualMap(expected, instance2);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testCopyPutAllWithSelfShouldReturnThis(MapData data) throws Exception {
        ImmutableMap<HashCollider, HashCollider> instance = newInstance(data.a());
        ImmutableMap<HashCollider, HashCollider> instance2 = instance.putAll(instance);
        assertSame(instance, instance2);
        assertEqualMap(data.a, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testEqualsWithObjectShouldYieldFalse(MapData data) throws Exception {
        ImmutableMap<HashCollider, HashCollider> instance = newInstance(data.a());
        assertNotEquals(instance, new Object());
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testGetOrDefaultWithContainedKeyShouldYieldValue(MapData data) throws Exception {
        ImmutableMap<HashCollider, HashCollider> instance = newInstance(data.a());
        HashCollider defaultValue = new HashCollider(7, -1);
        for (Map.Entry<HashCollider, HashCollider> e : data.a()) {
            assertEquals(e.getValue(), instance.getOrDefault(e.getKey(), defaultValue));
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testGetOrDefaultWithNonContainedKeyShouldYieldDefault(MapData data) throws Exception {
        ImmutableMap<HashCollider, HashCollider> instance = newInstance(data.a());
        HashCollider defaultValue = new HashCollider(7, -1);
        for (Map.Entry<HashCollider, HashCollider> e : data.c()) {
            assertEquals(defaultValue, instance.getOrDefault(e.getKey(), defaultValue));
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testCopyPutAllWithSomeNewKeyShouldReturnNewInstance(MapData data) throws Exception {
        ArrayList<Map.Entry<HashCollider, HashCollider>> listB = new ArrayList<>(data.b.readOnlyEntrySet().asSet());
        ArrayList<Map.Entry<HashCollider, HashCollider>> listC = new ArrayList<>(data.c.readOnlyEntrySet().asSet());
        Map<HashCollider, HashCollider> m = new LinkedHashMap<>(data.a.asMap());
        for (Map.Entry<HashCollider, HashCollider> entry : listB.subList(0, listB.size() / 2)) {
            m.put(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<HashCollider, HashCollider> entry : listC.subList(0, listC.size() / 2)) {
            m.put(entry.getKey(), entry.getValue());
        }
        ImmutableMap<HashCollider, HashCollider> instance = newInstance(data.a);
        ImmutableMap<HashCollider, HashCollider> instance2 = instance.putAll(m);
        assertNotSame(instance, instance2);
        LinkedHashMap<HashCollider, HashCollider> expected = new LinkedHashMap<>(data.a.asMap());
        assertEqualMap(expected, instance);
        expected.putAll(m);
        assertEqualMap(expected, instance2);
    }

    @SuppressWarnings("SimplifiableAssertion")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testEqualsWithNullShouldYieldFalse(@NonNull MapData data) {
        ImmutableMap<HashCollider, HashCollider> instance = newInstance(data.a());
        assertFalse(instance.equals(null));
    }


    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testToStringShouldContainAllEntries(@NonNull MapData data) {
        ImmutableMap<HashCollider, HashCollider> instance = newInstance();
        assertEquals("{}", instance.toString());

        instance = instance.putAll(data.a.asMap());
        String str = instance.toString();
        assertEquals('{', str.charAt(0));
        assertEquals('}', str.charAt(str.length() - 1));
        LinkedHashSet<String> actual = new LinkedHashSet<>(Arrays.asList(str.substring(1, str.length() - 1).split(", ")));
        Set<String> expected = new LinkedHashSet<>();
        data.a.iterator().forEachRemaining(e -> expected.add(e.toString()));
        assertEquals(expected, actual);
    }


    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testCopyRetainAllWithContainedKeysShouldReturnThis(@NonNull MapData data) throws Exception {
        ImmutableMap<HashCollider, HashCollider> instance = newInstance(data.a());
        ImmutableMap<HashCollider, HashCollider> instance2 = instance.retainAll(data.a().asMap().keySet());
        assertSame(instance, instance2);
        assertEqualMap(data.a(), instance2);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testCopyRetainAllWithSomeContainedKeysShouldReturnNewInstance(@NonNull MapData data) throws Exception {
        ImmutableMap<HashCollider, HashCollider> instance = newInstance(data.a());
        ImmutableMap<HashCollider, HashCollider> instance2 = instance.retainAll(data.someAPlusSomeB().asMap().keySet());
        assertNotSame(instance, instance2);
        LinkedHashMap<HashCollider, HashCollider> expected = new LinkedHashMap<>(data.a().asMap());
        expected.keySet().retainAll(data.someAPlusSomeB().asMap().keySet());
        assertEqualMap(expected, instance2);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testCopyRetainAllWithEmptySetShouldReturnNewInstance(@NonNull MapData data) throws Exception {
        ImmutableMap<HashCollider, HashCollider> instance = newInstance(data.a());
        ImmutableMap<HashCollider, HashCollider> instance2 = instance.retainAll(Collections.emptySet());
        assertNotSame(instance, instance2);
        assertEqualMap(data.a(), instance);
        assertEqualMap(Collections.emptyMap(), instance2);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testCopyRetainAllOfEmptyMapShouldReturnThis(@NonNull MapData data) throws Exception {
        ImmutableMap<HashCollider, HashCollider> instance = newInstance();
        ImmutableMap<HashCollider, HashCollider> instance2 = instance.retainAll(data.a().asMap().keySet());
        assertSame(instance, instance2);
        assertEqualMap(Collections.emptyMap(), instance2);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testCopyRemoveAllOfEmptyMapShouldReturnThis(@NonNull MapData data) throws Exception {
        ImmutableMap<HashCollider, HashCollider> instance = newInstance();
        assertSame(instance, instance.removeAll(data.a.readOnlyKeySet().asSet()));
        assertEqualMap(Collections.emptyMap(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testCopyRemoveAllWithEmptyMapShouldReturnThis(@NonNull MapData data) throws Exception {
        ImmutableMap<HashCollider, HashCollider> instance = newInstance(data.a);
        assertSame(instance, instance.removeAll(Collections.emptySet()));
        assertEqualMap(data.a(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testCopyRemoveAllWithContainedKeyShouldReturnNewInstance(@NonNull MapData data) throws Exception {
        ImmutableMap<HashCollider, HashCollider> instance = newInstance(data.a);
        ImmutableMap<HashCollider, HashCollider> instance2 = instance.removeAll(data.a.readOnlyKeySet().asSet());
        assertNotSame(instance, instance2);
        assertEqualMap(data.a, instance);
        assertEqualMap(Collections.emptyMap(), instance2);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testCopyRemoveAllWithSomeContainedKeyShouldReturnNewInstance(@NonNull MapData data) throws Exception {
        ImmutableMap<HashCollider, HashCollider> instance = newInstance(data.a);
        ImmutableMap<HashCollider, HashCollider> instance2 = instance.removeAll(data.someAPlusSomeB().readOnlyKeySet().asSet());
        assertNotSame(instance, instance2);
        LinkedHashMap<HashCollider, HashCollider> expected = new LinkedHashMap<>(data.a.asMap());
        assertEqualMap(expected, instance);
        expected.keySet().removeAll(data.someAPlusSomeB().readOnlyKeySet().asSet());
        assertEqualMap(expected, instance2);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testGetOfEntryWithNullValueShouldYieldNull(@NonNull MapData data) throws Exception {
        ImmutableMap<HashCollider, HashCollider> instance = newInstance(data.a());
        LinkedHashMap<HashCollider, HashCollider> expected = new LinkedHashMap<>(data.a.asMap());
        HashCollider key = new HashCollider(42, -1);
        ImmutableMap<HashCollider, HashCollider> instance2 = instance.put(key, null);
        assertNotSame(instance, instance2);
        expected.put(key, null);
        assertTrue(instance2.containsKey(key));
        assertNull(instance2.get(key));
        assertEqualMap(expected, instance2);
    }

}
