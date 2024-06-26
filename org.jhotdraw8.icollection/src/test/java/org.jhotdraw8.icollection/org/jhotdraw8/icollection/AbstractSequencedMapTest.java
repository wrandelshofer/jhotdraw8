package org.jhotdraw8.icollection;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SequencedMap;
import java.util.Spliterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public abstract class AbstractSequencedMapTest extends AbstractMapTest {
    /**
     * Creates a new empty instance.
     */
    protected abstract <K, V> SequencedMap<K, V> newInstance();

    /**
     * Creates a new instance with the specified expected number of elements
     * and load factor.
     */
    protected abstract <K, V> SequencedMap<K, V> newInstance(int numElements, float loadFactor);

    /**
     * Creates a new instance with the specified map.
     */
    protected abstract <K, V> SequencedMap<K, V> newInstance(Map<K, V> m);


    protected abstract <K, V> SequencedMap<K, V> toClonedInstance(Map<K, V> m);

    protected abstract <K, V> SequencedMap<K, V> newInstance(Iterable<Map.Entry<K, V>> m);


    @ParameterizedTest
    @MethodSource("dataProvider")
    public void putFirstWithContainedEntryShouldMoveEntryToFirst(MapData data) throws Exception {
        SequencedMap<Key, Value> instance = newInstance(data.a());
        List<Map.Entry<Key, Value>> expected = new ArrayList<>(data.a().asMap().entrySet());
        for (Map.Entry<Key, Value> e : data.a()) {
            instance.putFirst(e.getKey(), e.getValue());
            assertEquals(e, instance.firstEntry());
            expected.remove(e);
            expected.add(0, e);
            assertEqualSequence(expected, instance, "putFirst");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void putFirstWithNewElementShouldMoveElementToFirst(MapData data) throws Exception {
        SequencedMap<Key, Value> instance = newInstance(data.a());
        List<Map.Entry<Key, Value>> expected = new ArrayList<>(data.a().asMap().entrySet());
        for (Map.Entry<Key, Value> e : data.c()) {
            instance.putFirst(e.getKey(), e.getValue());
            Map.Entry<Key, Value> firstEntry = instance.firstEntry();
            assertEquals(e, firstEntry);
            assertNotNull(firstEntry);
            assertEquals(e.getKey(), firstEntry.getKey());
            expected.remove(e);
            expected.add(0, e);
            assertEqualSequence(expected, instance, "putFirst");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void putLastWithContainedElementShouldMoveElementToLast(MapData data) throws Exception {
        SequencedMap<Key, Value> instance = newInstance(data.a());
        List<Map.Entry<Key, Value>> expected = new ArrayList<>(data.a().asMap().entrySet());
        for (Map.Entry<Key, Value> e : data.a()) {
            instance.putLast(e.getKey(), e.getValue());
            Map.Entry<Key, Value> lastEntry = instance.lastEntry();
            assertEquals(e, lastEntry);
            assertNotNull(lastEntry);
            assertEquals(e.getKey(), lastEntry.getKey());
            expected.remove(e);
            expected.add(e);
            assertEqualSequence(expected, instance, "putLast");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void putLastWithNewElementShouldMoveElementToLast(MapData data) throws Exception {
        SequencedMap<Key, Value> instance = newInstance(data.a());
        List<Map.Entry<Key, Value>> expected = new ArrayList<>(data.a().asMap().entrySet());
        for (Map.Entry<Key, Value> e : data.c()) {
            instance.putLast(e.getKey(), e.getValue());
            assertEquals(e, instance.lastEntry());
            expected.remove(e);
            expected.add(e);
            assertEqualSequence(expected, instance, "putLast");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void putWithContainedElementShouldNotMoveElementToLast(MapData data) throws Exception {
        SequencedMap<Key, Value> instance = newInstance(data.a());
        List<Map.Entry<Key, Value>> expected = new ArrayList<>(data.a().asMap().entrySet());
        for (Map.Entry<Key, Value> e : data.a()) {
            instance.put(e.getKey(), e.getValue());
            assertEquals(expected.get(expected.size() - 1), instance.lastEntry());
            assertEqualSequence(expected, instance, "put");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void removeWithLastElementShouldNotChangeSequence(MapData data) throws Exception {
        SequencedMap<Key, Value> instance = newInstance(data.a());
        List<Map.Entry<Key, Value>> expected = new ArrayList<>(data.a().asMap().entrySet());
        while (!expected.isEmpty()) {
            Map.Entry<Key, Value> e = expected.remove(expected.size() - 1);
            assertEquals(e.getValue(), instance.remove(e.getKey()));
            assertEqualSequence(expected, instance, "remove(lastElement)");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void pollFirstShouldNotChangeSequence(MapData data) throws Exception {
        SequencedMap<Key, Value> instance = newInstance(data.a());
        List<Map.Entry<Key, Value>> expected = new ArrayList<>(data.a().asMap().entrySet());
        while (!expected.isEmpty()) {
            Map.Entry<Key, Value> e = expected.remove(0);
            assertEquals(instance.pollFirstEntry(), e);
            assertEqualSequence(expected, instance, "pollFirstEntry");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void removeFirstWithEmptySetShouldReturnNull(MapData data) throws Exception {
        SequencedMap<Key, Value> instance = newInstance(data.a());
        for (Map.Entry<Key, Value> e : data.a()) {
            instance.remove(e.getKey());
        }
        assertNull(instance.pollFirstEntry());
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void pollLastWithEmptySetShouldReturnNull(MapData data) throws Exception {
        SequencedMap<Key, Value> instance = newInstance(data.a());
        for (Map.Entry<Key, Value> e : data.a()) {
            instance.remove(e.getKey());
        }
        assertNull(instance.pollLastEntry());
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void removeLastShouldNotChangeSequence(MapData data) throws Exception {
        SequencedMap<Key, Value> instance = newInstance(data.a());
        List<Map.Entry<Key, Value>> expected = new ArrayList<>(data.a().asMap().entrySet());
        while (!expected.isEmpty()) {
            assertEquals(instance.pollLastEntry(), expected.remove(expected.size() - 1));
            assertEqualSequence(expected, instance, "pollLastEntry");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void removeWithFirstElementShouldNotChangeSequence(MapData data) throws Exception {
        SequencedMap<Key, Value> instance = newInstance(data.a());
        List<Map.Entry<Key, Value>> expected = new ArrayList<>(data.a().asMap().entrySet());
        while (!expected.isEmpty()) {
            Map.Entry<Key, Value> e = expected.remove(0);
            assertEquals(e.getValue(), instance.remove(e.getKey()));
            assertEqualSequence(expected, instance, "remove(firstElement)");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void removeWithMiddleElementShouldNotChangeSequence(MapData data) throws Exception {
        SequencedMap<Key, Value> instance = newInstance(data.a());
        List<Map.Entry<Key, Value>> expected = new ArrayList<>(data.a().asMap().entrySet());
        while (!expected.isEmpty()) {
            Map.Entry<Key, Value> e = expected.remove(expected.size() / 2);
            assertEquals(e.getValue(), instance.remove(e.getKey()));
            assertEqualSequence(expected, instance, "removeMiddle");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void putWithNewElementShouldMoveElementToLast(MapData data) throws Exception {
        SequencedMap<Key, Value> instance = newInstance(data.a());
        List<Map.Entry<Key, Value>> expected = new ArrayList<>(data.a().asMap().entrySet());
        for (Map.Entry<Key, Value> e : data.c()) {
            instance.put(e.getKey(), e.getValue());
            assertEquals(e, instance.lastEntry());
            expected.remove(e);
            expected.add(e);
            assertEqualSequence(expected, instance, "add");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void putNewValueForExistingElementShouldNotChangeSequence(MapData data) throws Exception {
        SequencedMap<Key, Value> instance = newInstance(data.a());
        List<Map.Entry<Key, Value>> expected = new ArrayList<>(data.a().asMap().entrySet());
        ArrayList<Integer> indices = new ArrayList<>(data.a().size());
        for (int i = 0; i < data.a().size(); i++) {
            indices.add(i);
        }
        Collections.shuffle(indices);
        for (int i : indices) {
            Value newValue = new Value(i, -1);
            Map.Entry<Key, Value> oldEntry = expected.get(i);
            var newEntry = (AbstractMap.SimpleImmutableEntry<Key, Value>) new AbstractMap.SimpleImmutableEntry<>(oldEntry.getKey(), newValue);

            instance.put(oldEntry.getKey(), newValue);
            expected.set(i, newEntry);
            assertEqualSequence(expected, instance, "put " + i + " oldValue: " + oldEntry + " newValue: " + newEntry);
        }
    }

    protected <K, V> void assertEqualSequence(Collection<Map.Entry<K, V>> expected, SequencedMap<K, V> actual, String message) {
        ArrayList<Map.Entry<K, V>> expectedList = new ArrayList<>(expected);
        assertEquals(expectedList, new ArrayList<>(actual.sequencedEntrySet()), message);

        if (!expected.isEmpty()) {
            assertEquals(expectedList.get(0), actual.firstEntry(), message);
            assertEquals(expectedList.get(0), actual.sequencedEntrySet().iterator().next(), message);
            assertEquals(expectedList.get(expectedList.size() - 1), actual.lastEntry(), message);
            assertEquals(expectedList.get(expectedList.size() - 1), actual.reversed().sequencedEntrySet().iterator().next(), message);
        }

        LinkedHashMap<Object, Object> x = new LinkedHashMap<>();
        for (Map.Entry<K, V> e : expected) {
            x.put(e.getKey(), e.getValue());
        }
        assertEquals(x.toString(), actual.toString(), message);
    }

    @Test
    public void testSpliteratorShouldHaveSequencedMapCharacteristics() throws Exception {
        SequencedMap<Key, Value> instance = newInstance();

        assertEquals(Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.SIZED, (Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.SIZED) & instance.keySet().spliterator().characteristics());
        assertEquals(Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.SIZED, (Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.SIZED) & instance.entrySet().spliterator().characteristics());
        assertEquals(Spliterator.ORDERED | Spliterator.SIZED, (Spliterator.ORDERED | Spliterator.SIZED) & instance.values().spliterator().characteristics());
    }
}
