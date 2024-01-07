package org.jhotdraw8.icollection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.icollection.immutable.ImmutableMap;
import org.jhotdraw8.icollection.readonly.ReadOnlyMap;
import org.jhotdraw8.icollection.readonly.ReadOnlySet;
import org.jhotdraw8.icollection.readonly.ReadOnlySortedMap;
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
import java.util.Spliterator;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
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

    protected abstract boolean supportsNullKeys();

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
        assertEquals(new LinkedHashSet<>(expected.values()),
                new LinkedHashSet<>(actual.readOnlyValues().asCollection()));
        assertEquals(expected.size(), actual.size());
        assertEquals(expected.isEmpty(), actual.isEmpty());
        assertEquals(expected.hashCode(), actual.hashCode());
        assertEquals(expected, actual.asMap());
        assertEquals(actual.asMap(), expected);
        assertEquals(expected.entrySet(), actual.readOnlyEntrySet().asSet());
        assertEquals(expected.keySet(), actual.readOnlyKeySet().asSet());
    }

    protected <K, V> void assertNotEqualMap(Map<K, V> expected, ImmutableMap<K, V> actual) {
        assertNotEquals(expected, actual);
        assertNotEquals(actual, expected);
        assertNotEquals(expected.entrySet(), actual.readOnlyEntrySet().asSet());
    }

    @Test
    public void newInstanceNoArgsShouldBeEmpty() {
        ImmutableMap<Key, Key> actual = newInstance();
        LinkedHashMap<Key, Key> expected = new LinkedHashMap<>();
        assertEqualMap(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void newInstanceMapArgsShouldBeEqualToArg(MapData data) {
        ImmutableMap<Key, Key> actual = newInstance(data.a().asMap());
        assertEqualMap(data.a(), actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void newInstanceMapArgsOfSameTypeShouldBeEqualToArg(MapData data) {
        ImmutableMap<Key, Key> actual1 = newInstance(data.a().asMap());
        ImmutableMap<Key, Key> actual = newInstance(actual1);
        assertEqualMap(data.a(), actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void newInstanceReadOnlyMapArgShouldBeEqualToARg(MapData data) {
        ImmutableMap<Key, Key> actual = newInstance(data.a());
        assertEqualMap(data.a(), actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void newInstanceIterableArgShouldBeEqualToArg(MapData data) {
        ImmutableMap<Key, Key> actual = newInstance(data.a().readOnlyEntrySet());
        assertEqualMap(data.a(), actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyClearShouldYieldEmptyMap(MapData data) {
        ImmutableMap<Key, Key> instance = newInstance(data.a());
        assertNotEqualMap(Collections.emptyMap(), instance);
        ImmutableMap<Key, Key> instance2 = instance.clear();
        assertNotSame(instance, instance2);
        assertEqualMap(Collections.emptyMap(), instance2);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyClearShouldBeIdempotent(MapData data) {
        ImmutableMap<Key, Key> instance = newInstance(data.a());
        assertNotEqualMap(Collections.emptyMap(), instance);
        instance = instance.clear();
        assertEqualMap(Collections.emptyMap(), instance);
        ImmutableMap<Key, Key> instance2 = instance.clear();
        assertSame(instance, instance2);
        assertEqualMap(Collections.emptyMap(), instance2);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void cloneShouldYieldEqualMap(MapData data) {
        ImmutableMap<Key, Key> instance = newInstance(data.a());
        ImmutableMap<Key, Key> clone = toClonedInstance(instance);
        assertEqualMap(data.a(), clone);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void containsKeyShouldYieldExpectedValue(MapData data) {
        ImmutableMap<Key, Key> instance = newInstance(data.a());
        for (Key k : data.a().readOnlyKeySet()) {
            assertTrue(instance.containsKey(k));
        }
        for (Key k : data.c().readOnlyKeySet()) {
            assertFalse(instance.containsKey(k));
        }
        try {
            assertFalse(instance.containsKey(new Object()));
        } catch (ClassCastException e) {
            assertInstanceOf(ReadOnlySortedMap.class, instance, "only read-only sorted maps may throw ClassCastException");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void readOnlyEntrySetContainsShouldYieldExpectedValue(MapData data) {
        ImmutableMap<Key, Key> instance = newInstance(data.a());
        for (Map.Entry<Key, Key> e : data.a().readOnlyEntrySet()) {
            assertTrue(instance.readOnlyEntrySet().contains(e));
        }
        for (Map.Entry<Key, Key> e : data.b().readOnlyEntrySet()) {
            assertFalse(instance.readOnlyEntrySet().contains(e));
        }
        for (Map.Entry<Key, Key> e : data.c().readOnlyEntrySet()) {
            assertFalse(instance.readOnlyEntrySet().contains(e));
        }
        assertFalse(instance.readOnlyEntrySet().contains(new Object()));
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void iteratorRemoveShouldThrowException(MapData data) {
        ImmutableMap<Key, Key> instance = newInstance(data.a());
        Iterator<Key> i = instance.readOnlyKeySet().iterator();
        assertThrows(Exception.class, i::remove);
        Iterator<Key> k = instance.readOnlyValues().iterator();
        assertThrows(Exception.class, k::remove);
        Iterator<Map.Entry<Key, Key>> j = instance.readOnlyEntrySet().iterator();
        assertThrows(Exception.class, j::remove);
    }

    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void serializationShouldYieldSameMap(MapData data) throws Exception {
        ImmutableMap<Key, Key> instance = newInstance(data.a());
        assertEqualMap(data.a(), instance);
        if (instance instanceof Serializable) {
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            try (ObjectOutputStream out = new ObjectOutputStream(buf)) {
                out.writeObject(instance);
            }
            ImmutableMap<Key, Key> deserialized;
            try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buf.toByteArray()))) {
                deserialized = (ImmutableMap<Key, Key>) in.readObject();
            }
            assertEqualMap(data.a(), deserialized);
        }
    }

    @Test
    public void spliteratorShouldHaveImmutableMapCharacteristics() throws Exception {
        ImmutableMap<Key, Key> instance = newInstance();

        assertEquals(Spliterator.IMMUTABLE | Spliterator.DISTINCT | Spliterator.SIZED,
                (Spliterator.IMMUTABLE | Spliterator.DISTINCT | Spliterator.SIZED) & instance.spliterator().characteristics());
        assertEquals(Spliterator.IMMUTABLE | Spliterator.DISTINCT | Spliterator.SIZED,
                (Spliterator.IMMUTABLE | Spliterator.DISTINCT | Spliterator.SIZED) & instance.readOnlyKeySet().spliterator().characteristics());
        assertEquals(Spliterator.NONNULL | Spliterator.IMMUTABLE | Spliterator.DISTINCT | Spliterator.SIZED,
                (Spliterator.NONNULL | Spliterator.IMMUTABLE | Spliterator.DISTINCT | Spliterator.SIZED) & instance.readOnlyEntrySet().spliterator().characteristics());
        assertEquals(Spliterator.IMMUTABLE | Spliterator.SIZED,
                (Spliterator.IMMUTABLE | Spliterator.SIZED) & instance.readOnlyValues().spliterator().characteristics());
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void equalsWithThisShouldYieldTrue(MapData data) {
        ImmutableMap<Key, Key> instance = newInstance(data.a());
        assertEquals(instance, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void equalsWithCloneWithUpdatedEntriesShouldYieldFalse(MapData data) throws Exception {
        ImmutableMap<Key, Key> instance = newInstance(data.a());
        ImmutableMap<Key, Key> instance2 = toClonedInstance(instance);
        assertEquals(instance, instance2);

        // WHEN instance3 has not the same size as instance2
        ImmutableMap<Key, Key> instance3 = instance2.putAll(data.b().asMap());
        assertNotEquals(instance2.size(), instance3.size());
        assertNotSame(instance2, instance3);
        assertNotEquals(instance, instance3);
        assertNotEquals(instance2, instance3);

        // WHEN instance4 has the same size as instance
        assertEquals(data.a().size(), data.b().size());
        ImmutableMap<Key, Key> instance4 = newInstance(data.b());
        assertNotEquals(instance, instance4);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyPutWithNewKeyShouldReturnNewInstance(MapData data) throws Exception {
        ImmutableMap<Key, Key> instance = newInstance(data.a);
        LinkedHashMap<Key, Key> expected = new LinkedHashMap<>(data.a().asMap());
        for (Map.Entry<Key, Key> e : data.c) {
            ImmutableMap<Key, Key> instance2 = instance.put(e.getKey(), e.getValue());
            assertNotSame(instance, instance2);
            expected.put(e.getKey(), e.getValue());
            assertEqualMap(expected, instance2);
            instance = instance2;
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyPutWithContainedKeyButNewValueShouldReturnNewInstance(MapData data) throws Exception {
        ImmutableMap<Key, Key> instance = newInstance(data.a());
        LinkedHashMap<Key, Key> expected = new LinkedHashMap<>(data.a().asMap());
        for (Map.Entry<Key, Key> e : data.b) {
            ImmutableMap<Key, Key> instance2 = instance.put(e.getKey(), e.getValue());
            assertNotSame(instance, instance2);
            assertEqualMap(expected, instance);
            expected.put(e.getKey(), e.getValue());
            assertEqualMap(expected, instance2);
            instance = instance2;
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyRemoveWithNewKeyShouldReturnThis(MapData data) throws Exception {
        ImmutableMap<Key, Key> instance = newInstance(data.a);
        for (Map.Entry<Key, Key> e : data.c) {
            assertSame(instance, instance.remove(e.getKey()));
            assertEqualMap(data.a.asMap(), instance);
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyRemoveWithContainedKeyShouldReturnNewInstance(MapData data) throws Exception {
        ImmutableMap<Key, Key> instance = newInstance(data.a);
        LinkedHashMap<Key, Key> expected = new LinkedHashMap<>(data.a().asMap());
        for (Map.Entry<Key, Key> e : data.a) {
            ImmutableMap<Key, Key> instance2 = instance.remove(e.getKey());
            assertNotSame(instance, instance2);
            assertEqualMap(expected, instance);
            expected.remove(e.getKey());
            assertEqualMap(expected, instance2);
            instance = instance2;
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void entrySetContainsExpectedEntries(MapData data) throws Exception {
        ImmutableMap<Key, Key> instance = newInstance(data.a);
        ReadOnlySet<Map.Entry<Key, Key>> entrySet = instance.readOnlyEntrySet();
        for (Map.Entry<Key, Key> e : data.a) {
            assertTrue(entrySet.contains(e));
        }
        for (Map.Entry<Key, Key> e : data.b) {
            assertFalse(entrySet.contains(e));
        }
        for (Map.Entry<Key, Key> e : data.c) {
            assertFalse(entrySet.contains(e));
        }
        assertTrue(entrySet.containsAll(data.a.readOnlyEntrySet().asSet()));
        assertFalse(entrySet.containsAll(data.b.readOnlyEntrySet().asSet()));
        assertFalse(entrySet.containsAll(data.c.readOnlyEntrySet().asSet()));
        LinkedHashSet<Map.Entry<Key, Key>> abc = new LinkedHashSet<>(data.a.readOnlyEntrySet().asSet());
        abc.addAll(data.b.readOnlyEntrySet().asSet());
        abc.addAll(data.c.readOnlyEntrySet().asSet());
        assertFalse(entrySet.containsAll(abc));
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void entryIteratorShouldYieldExpectedEntries(MapData data) throws Exception {
        ImmutableMap<Key, Key> instance = newInstance(data.a);
        List<Map.Entry<Key, Key>> actualList = new ArrayList<>();
        LinkedHashMap<Key, Key> actualMap = new LinkedHashMap<>();
        instance.readOnlyEntrySet().iterator().forEachRemaining(actualList::add);
        instance.readOnlyEntrySet().iterator().forEachRemaining(e -> actualMap.put(e.getKey(), e.getValue()));
        assertEquals(data.a.size(), actualList.size());
        assertEqualMap(data.a, newInstance(actualMap));
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyPutWithContainedEntryShouldReturnThis(MapData data) throws Exception {
        ImmutableMap<Key, Key> instance = newInstance(data.a());
        LinkedHashMap<Key, Key> expected = new LinkedHashMap<>(data.a().asMap());
        for (Map.Entry<Key, Key> e : data.a) {
            ImmutableMap<Key, Key> instance2 = instance.put(e.getKey(), e.getValue());
            assertSame(instance, instance2);
            assertEqualMap(expected, instance2);
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyPutAllWithContainedEntriesShouldReturnThis(MapData data) throws Exception {
        ImmutableMap<Key, Key> instance = newInstance(data.a());
        ImmutableMap<Key, Key> instance2 = instance.putAll(data.a().asMap());
        assertSame(instance, instance2);
        assertEqualMap(data.a(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyPutAllWithNewEntriesShouldReturnNewInstance(MapData data) throws Exception {
        ImmutableMap<Key, Key> instance = newInstance(data.a());
        ImmutableMap<Key, Key> instance2 = instance.putAll(data.c().asMap());
        assertNotSame(instance, instance2);
        Map<Key, Key> expected = new LinkedHashMap<>(data.a().asMap());
        assertEqualMap(expected, instance);
        expected.putAll(data.c().asMap());
        assertEqualMap(expected, instance2);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyPutAllWithContainedKeysButNewValuesShouldReturnNewInstance(MapData data) throws Exception {
        ImmutableMap<Key, Key> instance = newInstance(data.a());
        ImmutableMap<Key, Key> instance2 = instance.putAll(data.b().asMap());
        assertNotSame(instance, instance2);
        Map<Key, Key> expected = new LinkedHashMap<>(data.a().asMap());
        assertEqualMap(expected, instance);
        expected.putAll(data.b().asMap());
        assertEqualMap(expected, instance2);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyPutAllWithSelfShouldReturnThis(MapData data) throws Exception {
        ImmutableMap<Key, Key> instance = newInstance(data.a());
        ImmutableMap<Key, Key> instance2 = instance.putAll(instance);
        assertSame(instance, instance2);
        assertEqualMap(data.a, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void equalsWithObjectShouldYieldFalse(MapData data) throws Exception {
        ImmutableMap<Key, Key> instance = newInstance(data.a());
        assertNotEquals(instance, new Object());
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void getOrDefaultWithContainedKeyShouldYieldValue(MapData data) throws Exception {
        ImmutableMap<Key, Key> instance = newInstance(data.a());
        Key defaultValue = new Key(7, -1);
        for (Map.Entry<Key, Key> e : data.a()) {
            assertEquals(e.getValue(), instance.getOrDefault(e.getKey(), defaultValue));
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void getOrDefaultWithNonContainedKeyShouldYieldDefault(MapData data) throws Exception {
        ImmutableMap<Key, Key> instance = newInstance(data.a());
        Key defaultValue = new Key(7, -1);
        for (Map.Entry<Key, Key> e : data.c()) {
            assertEquals(defaultValue, instance.getOrDefault(e.getKey(), defaultValue));
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyPutAllWithSomeNewKeyShouldReturnNewInstance(MapData data) throws Exception {
        ArrayList<Map.Entry<Key, Key>> listB = new ArrayList<>(data.b.readOnlyEntrySet().asSet());
        ArrayList<Map.Entry<Key, Key>> listC = new ArrayList<>(data.c.readOnlyEntrySet().asSet());
        Map<Key, Key> m = new LinkedHashMap<>(data.a.asMap());
        for (Map.Entry<Key, Key> entry : listB.subList(0, listB.size() / 2)) {
            m.put(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<Key, Key> entry : listC.subList(0, listC.size() / 2)) {
            m.put(entry.getKey(), entry.getValue());
        }
        ImmutableMap<Key, Key> instance = newInstance(data.a);
        ImmutableMap<Key, Key> instance2 = instance.putAll(m);
        assertNotSame(instance, instance2);
        LinkedHashMap<Key, Key> expected = new LinkedHashMap<>(data.a.asMap());
        assertEqualMap(expected, instance);
        expected.putAll(m);
        assertEqualMap(expected, instance2);
    }

    @SuppressWarnings("SimplifiableAssertion")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void equalsWithNullShouldYieldFalse(@NonNull MapData data) {
        ImmutableMap<Key, Key> instance = newInstance(data.a());
        assertFalse(instance.equals(null));
    }


    @ParameterizedTest
    @MethodSource("dataProvider")
    public void toStringShouldContainAllEntries(@NonNull MapData data) {
        ImmutableMap<Key, Key> instance = newInstance();
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
    public void copyRetainAllWithContainedKeysShouldReturnThis(@NonNull MapData data) throws Exception {
        ImmutableMap<Key, Key> instance = newInstance(data.a());
        ImmutableMap<Key, Key> instance2 = instance.retainAll(data.a().asMap().keySet());
        assertSame(instance, instance2);
        assertEqualMap(data.a(), instance2);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyRetainAllWithSomeContainedKeysShouldReturnNewInstance(@NonNull MapData data) throws Exception {
        ImmutableMap<Key, Key> instance = newInstance(data.a());
        ImmutableMap<Key, Key> instance2 = instance.retainAll(data.someAPlusSomeB().asMap().keySet());
        assertNotSame(instance, instance2);
        LinkedHashMap<Key, Key> expected = new LinkedHashMap<>(data.a().asMap());
        expected.keySet().retainAll(data.someAPlusSomeB().asMap().keySet());
        assertEqualMap(expected, instance2);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyRetainAllWithEmptySetShouldReturnNewInstance(@NonNull MapData data) throws Exception {
        ImmutableMap<Key, Key> instance = newInstance(data.a());
        ImmutableMap<Key, Key> instance2 = instance.retainAll(Collections.emptySet());
        assertNotSame(instance, instance2);
        assertEqualMap(data.a(), instance);
        assertEqualMap(Collections.emptyMap(), instance2);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyRetainAllOfEmptyMapShouldReturnThis(@NonNull MapData data) throws Exception {
        ImmutableMap<Key, Key> instance = newInstance();
        ImmutableMap<Key, Key> instance2 = instance.retainAll(data.a().asMap().keySet());
        assertSame(instance, instance2);
        assertEqualMap(Collections.emptyMap(), instance2);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyRemoveAllOfEmptyMapShouldReturnThis(@NonNull MapData data) throws Exception {
        ImmutableMap<Key, Key> instance = newInstance();
        assertSame(instance, instance.removeAll(data.a.readOnlyKeySet().asSet()));
        assertEqualMap(Collections.emptyMap(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyRemoveAllWithEmptyMapShouldReturnThis(@NonNull MapData data) throws Exception {
        ImmutableMap<Key, Key> instance = newInstance(data.a);
        assertSame(instance, instance.removeAll(Collections.emptySet()));
        assertEqualMap(data.a(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyRemoveAllWithContainedKeyShouldReturnNewInstance(@NonNull MapData data) throws Exception {
        ImmutableMap<Key, Key> instance = newInstance(data.a);
        ImmutableMap<Key, Key> instance2 = instance.removeAll(data.a.readOnlyKeySet().asSet());
        assertNotSame(instance, instance2);
        assertEqualMap(data.a, instance);
        assertEqualMap(Collections.emptyMap(), instance2);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyRemoveAllWithSomeContainedKeyShouldReturnNewInstance(@NonNull MapData data) throws Exception {
        ImmutableMap<Key, Key> instance = newInstance(data.a);
        ImmutableMap<Key, Key> instance2 = instance.removeAll(data.someAPlusSomeB().readOnlyKeySet().asSet());
        assertNotSame(instance, instance2);
        LinkedHashMap<Key, Key> expected = new LinkedHashMap<>(data.a.asMap());
        assertEqualMap(expected, instance);
        expected.keySet().removeAll(data.someAPlusSomeB().readOnlyKeySet().asSet());
        assertEqualMap(expected, instance2);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void getOfEntryWithNullValueShouldYieldNull(@NonNull MapData data) throws Exception {
        ImmutableMap<Key, Key> instance = newInstance(data.a());
        LinkedHashMap<Key, Key> expected = new LinkedHashMap<>(data.a.asMap());
        Key key = new Key(42, -1);
        ImmutableMap<Key, Key> instance2 = instance.put(key, null);
        assertNotSame(instance, instance2);
        expected.put(key, null);
        assertTrue(instance2.containsKey(key));
        assertNull(instance2.get(key));
        assertEqualMap(expected, instance2);
    }

    @Test
    public void spliteratorShouldSupportNullKeyNullValue() throws Exception {
        ImmutableMap<Key, Key> instance = newInstance();
        assertEquals(instance.readOnlyEntrySet().spliterator().characteristics() & Spliterator.NONNULL, Spliterator.NONNULL, "entrySet should be non-null");
        if (supportsNullKeys()) {
            assertEquals(instance.readOnlyKeySet().spliterator().characteristics() & Spliterator.NONNULL, 0, "keySet should be nullable");
        } else {
            assertEquals(instance.readOnlyKeySet().spliterator().characteristics() & Spliterator.NONNULL, Spliterator.NONNULL, "keySet should not be nullable");
        }
        assertEquals(instance.readOnlyValues().spliterator().characteristics() & Spliterator.NONNULL, 0, "valueSet should be nullable");
    }
}
