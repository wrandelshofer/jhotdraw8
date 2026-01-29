package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.persistent.PersistentMap;
import org.jhotdraw8.icollection.readable.ReadableMap;
import org.jhotdraw8.icollection.readable.ReadableSet;
import org.jhotdraw8.icollection.readable.ReadableSortedMap;
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
import java.util.SequencedMap;
import java.util.SequencedSet;
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

public abstract class AbstractPersistentMapTest {

    /**
     * Creates a new empty instance.
     */
    protected abstract <K, V> PersistentMap<K, V> newInstance();


    /**
     * Creates a new instance with the specified map.
     */
    protected abstract <K, V> PersistentMap<K, V> newInstance(Map<K, V> m);

    protected abstract <K, V> PersistentMap<K, V> newInstance(ReadableMap<K, V> m);


    protected abstract <K, V> PersistentMap<K, V> toClonedInstance(PersistentMap<K, V> m);

    /**
     * Creates a new instance with the specified map.
     */
    protected abstract <K, V> PersistentMap<K, V> newInstance(Iterable<Map.Entry<K, V>> m);

    protected abstract boolean supportsNullKeys();

    public static Stream<MapData> dataProvider() {
        return Stream.of(
                NO_COLLISION, ALL_COLLISION, SOME_COLLISION
        );
    }

    private static final MapData NO_COLLISION = MapData.newData("no collisions", -1, 32, 100_000);
    private static final MapData ALL_COLLISION = MapData.newData("all collisions", 0, 32, 100_000);
    private static final MapData SOME_COLLISION = MapData.newData("some collisions", 0x55555555, 32, 100_000);


    protected <K, V> void assertEqualMap(ReadableMap<K, V> expected, PersistentMap<K, V> actual) {
        assertEqualMap(expected.asMap(), actual);
    }

    protected <K, V> void assertEqualMap(Map<K, V> expected, PersistentMap<K, V> actual) {
        assertEquals(new LinkedHashSet<>(expected.values()),
                new LinkedHashSet<>(actual.readableValues().asCollection()));
        assertEquals(expected.size(), actual.size());
        assertEquals(expected.isEmpty(), actual.isEmpty());
        assertEquals(expected.hashCode(), actual.hashCode());
        assertEquals(expected, actual.asMap());
        assertEquals(actual.asMap(), expected);
        assertEquals(expected.entrySet(), actual.readableEntrySet().asSet());
        assertEquals(expected.keySet(), actual.readableKeySet().asSet());
    }

    protected <K, V> void assertNotEqualMap(Map<K, V> expected, PersistentMap<K, V> actual) {
        assertNotEquals(expected, actual);
        assertNotEquals(actual, expected);
        assertNotEquals(expected.entrySet(), actual.readableEntrySet().asSet());
    }

    @Test
    public void newInstanceNoArgsShouldBeEmpty() {
        PersistentMap<Key, Value> actual = newInstance();
        LinkedHashMap<Key, Value> expected = new LinkedHashMap<>();
        assertEqualMap(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void newInstanceMapArgsShouldBeEqualToArg(MapData data) {
        PersistentMap<Key, Value> actual = newInstance(data.a().asMap());
        assertEqualMap(data.a(), actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void newInstanceMapArgsOfSameTypeShouldBeEqualToArg(MapData data) {
        PersistentMap<Key, Value> actual1 = newInstance(data.a().asMap());
        PersistentMap<Key, Value> actual = newInstance(actual1);
        assertEqualMap(data.a(), actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void newInstanceReadOnlyMapArgShouldBeEqualToARg(MapData data) {
        PersistentMap<Key, Value> actual = newInstance(data.a());
        assertEqualMap(data.a(), actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void newInstanceIterableArgShouldBeEqualToArg(MapData data) {
        PersistentMap<Key, Value> actual = newInstance(data.a().readableEntrySet());
        assertEqualMap(data.a(), actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyClearShouldYieldEmptyMap(MapData data) {
        PersistentMap<Key, Value> instance = newInstance(data.a());
        assertNotEqualMap(Collections.emptyMap(), instance);
        PersistentMap<Key, Value> instance2 = instance.clear();
        assertNotSame(instance, instance2);
        assertEqualMap(Collections.emptyMap(), instance2);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyClearShouldBeIdempotent(MapData data) {
        PersistentMap<Key, Value> instance = newInstance(data.a());
        assertNotEqualMap(Collections.emptyMap(), instance);
        instance = instance.clear();
        assertEqualMap(Collections.emptyMap(), instance);
        PersistentMap<Key, Value> instance2 = instance.clear();
        assertSame(instance, instance2);
        assertEqualMap(Collections.emptyMap(), instance2);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void cloneShouldYieldEqualMap(MapData data) {
        PersistentMap<Key, Value> instance = newInstance(data.a());
        PersistentMap<Key, Value> clone = toClonedInstance(instance);
        assertEqualMap(data.a(), clone);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void containsKeyShouldYieldExpectedValue(MapData data) {
        PersistentMap<Key, Value> instance = newInstance(data.a());
        for (Key k : data.a().readableKeySet()) {
            assertTrue(instance.containsKey(k));
        }
        for (Key k : data.c().readableKeySet()) {
            assertFalse(instance.containsKey(k));
        }
        try {
            assertFalse(instance.containsKey(new Object()));
        } catch (ClassCastException e) {
            assertInstanceOf(ReadableSortedMap.class, instance, "only read-only sorted maps may throw ClassCastException");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void readOnlyEntrySetContainsShouldYieldExpectedValue(MapData data) {
        PersistentMap<Key, Value> instance = newInstance(data.a());
        for (Map.Entry<Key, Value> e : data.a().readableEntrySet()) {
            assertTrue(instance.readableEntrySet().contains(e));
        }
        for (Map.Entry<Key, Value> e : data.b().readableEntrySet()) {
            assertFalse(instance.readableEntrySet().contains(e));
        }
        for (Map.Entry<Key, Value> e : data.c().readableEntrySet()) {
            assertFalse(instance.readableEntrySet().contains(e));
        }
        assertFalse(instance.readableEntrySet().contains(new Object()));
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void iteratorRemoveShouldThrowException(MapData data) {
        PersistentMap<Key, Value> instance = newInstance(data.a());
        Iterator<Key> i = instance.readableKeySet().iterator();
        assertThrows(Exception.class, i::remove);
        Iterator<Value> k = instance.readableValues().iterator();
        assertThrows(Exception.class, k::remove);
        Iterator<Map.Entry<Key, Value>> j = instance.readableEntrySet().iterator();
        assertThrows(Exception.class, j::remove);
    }

    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void serializationShouldYieldSameMap(MapData data) throws Exception {
        PersistentMap<Key, Value> instance = newInstance(data.a());
        assertEqualMap(data.a(), instance);
        if (instance instanceof Serializable) {
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            try (ObjectOutputStream out = new ObjectOutputStream(buf)) {
                out.writeObject(instance);
            }
            PersistentMap<Key, Value> deserialized;
            try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buf.toByteArray()))) {
                deserialized = (PersistentMap<Key, Value>) in.readObject();
            }
            assertEqualMap(data.a(), deserialized);
        }
    }

    @Test
    public void spliteratorShouldHaveImmutableMapCharacteristics() throws Exception {
        PersistentMap<Key, Value> instance = newInstance();

        assertEquals(Spliterator.IMMUTABLE | Spliterator.DISTINCT | Spliterator.SIZED,
                (Spliterator.IMMUTABLE | Spliterator.DISTINCT | Spliterator.SIZED) & instance.spliterator().characteristics());
        assertEquals(Spliterator.IMMUTABLE | Spliterator.DISTINCT | Spliterator.SIZED,
                (Spliterator.IMMUTABLE | Spliterator.DISTINCT | Spliterator.SIZED) & instance.readableKeySet().spliterator().characteristics());
        assertEquals(Spliterator.NONNULL | Spliterator.IMMUTABLE | Spliterator.DISTINCT | Spliterator.SIZED,
                (Spliterator.NONNULL | Spliterator.IMMUTABLE | Spliterator.DISTINCT | Spliterator.SIZED) & instance.readableEntrySet().spliterator().characteristics());
        assertEquals(Spliterator.IMMUTABLE | Spliterator.SIZED,
                (Spliterator.IMMUTABLE | Spliterator.SIZED) & instance.readableValues().spliterator().characteristics());
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void equalsWithThisShouldYieldTrue(MapData data) {
        PersistentMap<Key, Value> instance = newInstance(data.a());
        assertEquals(instance, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void equalsWithCloneWithUpdatedEntriesShouldYieldFalse(MapData data) throws Exception {
        PersistentMap<Key, Value> instance = newInstance(data.a());
        PersistentMap<Key, Value> instance2 = toClonedInstance(instance);
        assertEquals(instance, instance2);

        // WHEN instance3 has not the same size as instance2
        PersistentMap<Key, Value> instance3 = instance2.putAll(data.b().asMap());
        assertNotEquals(instance2.size(), instance3.size());
        assertNotSame(instance2, instance3);
        assertNotEquals(instance, instance3);
        assertNotEquals(instance2, instance3);

        // WHEN instance4 has the same size as instance
        assertEquals(data.a().size(), data.b().size());
        PersistentMap<Key, Value> instance4 = newInstance(data.b());
        assertNotEquals(instance, instance4);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyPutWithNewKeyShouldReturnNewInstance(MapData data) throws Exception {
        PersistentMap<Key, Value> instance = newInstance(data.a);
        LinkedHashMap<Key, Value> expected = new LinkedHashMap<>(data.a().asMap());
        for (Map.Entry<Key, Value> e : data.c) {
            PersistentMap<Key, Value> instance2 = instance.put(e.getKey(), e.getValue());
            assertNotSame(instance, instance2);
            expected.put(e.getKey(), e.getValue());
            assertEqualMap(expected, instance2);
            instance = instance2;
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyPutWithContainedKeyButNewValueShouldReturnNewInstance(MapData data) throws Exception {
        PersistentMap<Key, Value> instance = newInstance(data.a());
        LinkedHashMap<Key, Value> expected = new LinkedHashMap<>(data.a().asMap());
        for (Map.Entry<Key, Value> e : data.b) {
            PersistentMap<Key, Value> instance2 = instance.put(e.getKey(), e.getValue());
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
        PersistentMap<Key, Value> instance = newInstance(data.a);
        for (Map.Entry<Key, Value> e : data.c) {
            assertSame(instance, instance.remove(e.getKey()));
            assertEqualMap(data.a.asMap(), instance);
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyRemoveWithContainedKeyShouldReturnNewInstance(MapData data) throws Exception {
        PersistentMap<Key, Value> instance = newInstance(data.a);
        LinkedHashMap<Key, Value> expected = new LinkedHashMap<>(data.a().asMap());
        for (Map.Entry<Key, Value> e : data.a) {
            PersistentMap<Key, Value> instance2 = instance.remove(e.getKey());
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
        PersistentMap<Key, Value> instance = newInstance(data.a);
        ReadableSet<Map.Entry<Key, Value>> entrySet = instance.readableEntrySet();
        for (Map.Entry<Key, Value> e : data.a) {
            assertTrue(entrySet.contains(e));
        }
        for (Map.Entry<Key, Value> e : data.b) {
            assertFalse(entrySet.contains(e));
        }
        for (Map.Entry<Key, Value> e : data.c) {
            assertFalse(entrySet.contains(e));
        }
        assertTrue(entrySet.containsAll(data.a.readableEntrySet().asSet()));
        assertFalse(entrySet.containsAll(data.b.readableEntrySet().asSet()));
        assertFalse(entrySet.containsAll(data.c.readableEntrySet().asSet()));
        LinkedHashSet<Map.Entry<Key, Value>> abc = new LinkedHashSet<>(data.a.readableEntrySet().asSet());
        abc.addAll(data.b.readableEntrySet().asSet());
        abc.addAll(data.c.readableEntrySet().asSet());
        assertFalse(entrySet.containsAll(abc));
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void entryIteratorShouldYieldExpectedEntries(MapData data) throws Exception {
        PersistentMap<Key, Value> instance = newInstance(data.a);
        List<Map.Entry<Key, Value>> actualList = new ArrayList<>();
        LinkedHashMap<Key, Value> actualMap = new LinkedHashMap<>();
        instance.readableEntrySet().iterator().forEachRemaining(actualList::add);
        instance.readableEntrySet().iterator().forEachRemaining(e -> actualMap.put(e.getKey(), e.getValue()));
        assertEquals(data.a.size(), actualList.size());
        assertEqualMap(data.a, newInstance(actualMap));
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyPutWithContainedEntryShouldReturnThis(MapData data) throws Exception {
        PersistentMap<Key, Value> instance = newInstance(data.a());
        LinkedHashMap<Key, Value> expected = new LinkedHashMap<>(data.a().asMap());
        for (Map.Entry<Key, Value> e : data.a) {
            PersistentMap<Key, Value> instance2 = instance.put(e.getKey(), e.getValue());
            assertSame(instance, instance2);
            assertEqualMap(expected, instance2);
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyPutAllWithContainedEntriesShouldReturnThis(MapData data) throws Exception {
        PersistentMap<Key, Value> instance = newInstance(data.a());
        PersistentMap<Key, Value> instance2 = instance.putAll(data.a().asMap());
        assertSame(instance, instance2);
        assertEqualMap(data.a(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyPutAllWithNewEntriesShouldReturnNewInstance(MapData data) throws Exception {
        PersistentMap<Key, Value> instance = newInstance(data.a());
        PersistentMap<Key, Value> instance2 = instance.putAll(data.c().asMap());
        assertNotSame(instance, instance2);
        SequencedMap<Key, Value> expected = new LinkedHashMap<>(data.a().asMap());
        assertEqualMap(expected, instance);
        expected.putAll(data.c().asMap());
        assertEqualMap(expected, instance2);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyPutAllWithContainedKeysButNewValuesShouldReturnNewInstance(MapData data) throws Exception {
        PersistentMap<Key, Value> instance = newInstance(data.a());
        PersistentMap<Key, Value> instance2 = instance.putAll(data.b().asMap());
        assertNotSame(instance, instance2);
        SequencedMap<Key, Value> expected = new LinkedHashMap<>(data.a().asMap());
        assertEqualMap(expected, instance);
        expected.putAll(data.b().asMap());
        assertEqualMap(expected, instance2);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyPutAllWithSelfShouldReturnThis(MapData data) throws Exception {
        PersistentMap<Key, Value> instance = newInstance(data.a());
        PersistentMap<Key, Value> instance2 = instance.putAll(instance);
        assertSame(instance, instance2);
        assertEqualMap(data.a, instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void equalsWithObjectShouldYieldFalse(MapData data) throws Exception {
        PersistentMap<Key, Value> instance = newInstance(data.a());
        assertNotEquals(instance, new Object());
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void getOrDefaultWithContainedKeyShouldYieldValue(MapData data) throws Exception {
        PersistentMap<Key, Value> instance = newInstance(data.a());
        Value defaultValue = new Value(7, -1);
        for (Map.Entry<Key, Value> e : data.a()) {
            assertEquals(e.getValue(), instance.getOrDefault(e.getKey(), defaultValue));
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void getOrDefaultWithNonContainedKeyShouldYieldDefault(MapData data) throws Exception {
        PersistentMap<Key, Value> instance = newInstance(data.a());
        Value defaultValue = new Value(7, -1);
        for (Map.Entry<Key, Value> e : data.c()) {
            assertEquals(defaultValue, instance.getOrDefault(e.getKey(), defaultValue));
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyPutAllWithSomeNewKeyShouldReturnNewInstance(MapData data) throws Exception {
        ArrayList<Map.Entry<Key, Value>> listB = new ArrayList<>(data.b.readableEntrySet().asSet());
        ArrayList<Map.Entry<Key, Value>> listC = new ArrayList<>(data.c.readableEntrySet().asSet());
        SequencedMap<Key, Value> m = new LinkedHashMap<>(data.a.asMap());
        for (Map.Entry<Key, Value> entry : listB.subList(0, listB.size() / 2)) {
            m.put(entry.getKey(), entry.getValue());
        }
        for (Map.Entry<Key, Value> entry : listC.subList(0, listC.size() / 2)) {
            m.put(entry.getKey(), entry.getValue());
        }
        PersistentMap<Key, Value> instance = newInstance(data.a);
        PersistentMap<Key, Value> instance2 = instance.putAll(m);
        assertNotSame(instance, instance2);
        LinkedHashMap<Key, Value> expected = new LinkedHashMap<>(data.a.asMap());
        assertEqualMap(expected, instance);
        expected.putAll(m);
        assertEqualMap(expected, instance2);
    }

    @SuppressWarnings("SimplifiableAssertion")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void equalsWithNullShouldYieldFalse(MapData data) {
        PersistentMap<Key, Value> instance = newInstance(data.a());
        assertFalse(instance.equals(null));
    }


    @ParameterizedTest
    @MethodSource("dataProvider")
    public void toStringShouldContainAllEntries(MapData data) {
        PersistentMap<Key, Value> instance = newInstance();
        assertEquals("{}", instance.toString());

        instance = instance.putAll(data.a.asMap());
        String str = instance.toString();
        assertEquals('{', str.charAt(0));
        assertEquals('}', str.charAt(str.length() - 1));
        LinkedHashSet<String> actual = new LinkedHashSet<>(Arrays.asList(str.substring(1, str.length() - 1).split(", ")));
        SequencedSet<String> expected = new LinkedHashSet<>();
        data.a.iterator().forEachRemaining(e -> expected.add(e.toString()));
        assertEquals(expected, actual);
    }


    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyRetainAllWithContainedKeysShouldReturnThis(MapData data) throws Exception {
        PersistentMap<Key, Value> instance = newInstance(data.a());
        PersistentMap<Key, Value> instance2 = instance.retainAll(data.a().asMap().keySet());
        assertSame(instance, instance2);
        assertEqualMap(data.a(), instance2);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyRetainAllWithSomeContainedKeysShouldReturnNewInstance(MapData data) throws Exception {
        PersistentMap<Key, Value> instance = newInstance(data.a());
        PersistentMap<Key, Value> instance2 = instance.retainAll(data.someAPlusSomeB().asMap().keySet());
        assertNotSame(instance, instance2);
        LinkedHashMap<Key, Value> expected = new LinkedHashMap<>(data.a().asMap());
        expected.keySet().retainAll(data.someAPlusSomeB().asMap().keySet());
        assertEqualMap(expected, instance2);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyRetainAllWithEmptySetShouldReturnNewInstance(MapData data) throws Exception {
        PersistentMap<Key, Value> instance = newInstance(data.a());
        PersistentMap<Key, Value> instance2 = instance.retainAll(Collections.emptySet());
        assertNotSame(instance, instance2);
        assertEqualMap(data.a(), instance);
        assertEqualMap(Collections.emptyMap(), instance2);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyRetainAllOfEmptyMapShouldReturnThis(MapData data) throws Exception {
        PersistentMap<Key, Value> instance = newInstance();
        PersistentMap<Key, Value> instance2 = instance.retainAll(data.a().asMap().keySet());
        assertSame(instance, instance2);
        assertEqualMap(Collections.emptyMap(), instance2);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyRemoveAllOfEmptyMapShouldReturnThis(MapData data) throws Exception {
        PersistentMap<Key, Value> instance = newInstance();
        assertSame(instance, instance.removeAll(data.a.readableKeySet().asSet()));
        assertEqualMap(Collections.emptyMap(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyRemoveAllWithEmptyMapShouldReturnThis(MapData data) throws Exception {
        PersistentMap<Key, Value> instance = newInstance(data.a);
        assertSame(instance, instance.removeAll(Collections.emptySet()));
        assertEqualMap(data.a(), instance);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyRemoveAllWithContainedKeyShouldReturnNewInstance(MapData data) throws Exception {
        PersistentMap<Key, Value> instance = newInstance(data.a);
        PersistentMap<Key, Value> instance2 = instance.removeAll(data.a.readableKeySet().asSet());
        assertNotSame(instance, instance2);
        assertEqualMap(data.a, instance);
        assertEqualMap(Collections.emptyMap(), instance2);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyRemoveAllWithSomeContainedKeyShouldReturnNewInstance(MapData data) throws Exception {
        PersistentMap<Key, Value> instance = newInstance(data.a);
        PersistentMap<Key, Value> instance2 = instance.removeAll(data.someAPlusSomeB().readableKeySet().asSet());
        assertNotSame(instance, instance2);
        LinkedHashMap<Key, Value> expected = new LinkedHashMap<>(data.a.asMap());
        assertEqualMap(expected, instance);
        expected.keySet().removeAll(data.someAPlusSomeB().readableKeySet().asSet());
        assertEqualMap(expected, instance2);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void getOfEntryWithNullValueShouldYieldNull(MapData data) throws Exception {
        PersistentMap<Key, Value> instance = newInstance(data.a());
        LinkedHashMap<Key, Value> expected = new LinkedHashMap<>(data.a.asMap());
        Key key = new Key(42, -1);
        PersistentMap<Key, Value> instance2 = instance.put(key, null);
        assertNotSame(instance, instance2);
        expected.put(key, null);
        assertTrue(instance2.containsKey(key));
        assertNull(instance2.get(key));
        assertEqualMap(expected, instance2);
    }

    @Test
    public void spliteratorShouldSupportNullKeyNullValue() throws Exception {
        PersistentMap<Key, Value> instance = newInstance();
        assertEquals(instance.readableEntrySet().spliterator().characteristics() & Spliterator.NONNULL, Spliterator.NONNULL, "entrySet should be non-null");
        if (supportsNullKeys()) {
            assertEquals(instance.readableKeySet().spliterator().characteristics() & Spliterator.NONNULL, 0, "keySet should be nullable");
        } else {
            assertEquals(instance.readableKeySet().spliterator().characteristics() & Spliterator.NONNULL, Spliterator.NONNULL, "keySet should not be nullable");
        }
        assertEquals(instance.readableValues().spliterator().characteristics() & Spliterator.NONNULL, 0, "valueSet should be nullable");
    }
}
