package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.immutable.ImmutableMap;
import org.jhotdraw8.icollection.immutable.ImmutableSequencedMap;
import org.jhotdraw8.icollection.readonly.ReadOnlyMap;
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
import java.util.Spliterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public abstract class AbstractImmutableSequencedMapTest extends AbstractImmutableMapTest {
    @Override
    protected abstract <K, V> ImmutableSequencedMap<K, V> newInstance();

    @Override
    protected abstract <K, V> ImmutableSequencedMap<K, V> newInstance(Map<K, V> m);

    @Override
    protected abstract <K, V> ImmutableSequencedMap<K, V> newInstance(ReadOnlyMap<K, V> m);

    @Override
    protected abstract <K, V> ImmutableSequencedMap<K, V> toClonedInstance(ImmutableMap<K, V> m);

    @Override
    protected abstract <K, V> ImmutableSequencedMap<K, V> newInstance(Iterable<Map.Entry<K, V>> m);


    @ParameterizedTest
    @MethodSource("dataProvider")
    public void putFirstWithContainedEntryShouldMoveEntryToFirst(MapData data) throws Exception {
        ImmutableSequencedMap<Key, Value> instance = newInstance(data.a());
        List<Map.Entry<Key, Value>> expected = new ArrayList<>(data.a().asMap().entrySet());
        assertEqualSequence(expected, instance, "initial");
        for (Map.Entry<Key, Value> e : data.a()) {
            instance = instance.putFirst(e.getKey(), e.getValue());
            expected.remove(e);
            expected.add(0, e);
            assertEqualSequence(expected, instance, "putFirst");
            assertEquals(e, instance.firstEntry());
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void putFirstWithNewElementShouldMoveElementToFirst(MapData data) throws Exception {
        ImmutableSequencedMap<Key, Value> instance = newInstance(data.a());
        List<Map.Entry<Key, Value>> expected = new ArrayList<>(data.a().asMap().entrySet());
        for (Map.Entry<Key, Value> e : data.c()) {
            instance = instance.putFirst(e.getKey(), e.getValue());
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
        ImmutableSequencedMap<Key, Value> instance = newInstance(data.a());
        List<Map.Entry<Key, Value>> expected = new ArrayList<>(data.a().asMap().entrySet());
        for (Map.Entry<Key, Value> e : data.a()) {
            instance = instance.putLast(e.getKey(), e.getValue());
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
        ImmutableSequencedMap<Key, Value> instance = newInstance(data.a());
        List<Map.Entry<Key, Value>> expected = new ArrayList<>(data.a().asMap().entrySet());
        for (Map.Entry<Key, Value> e : data.c()) {
            instance = instance.putLast(e.getKey(), e.getValue());
            assertEquals(e, instance.lastEntry());
            expected.remove(e);
            expected.add(e);
            assertEqualSequence(expected, instance, "putLast");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void putWithContainedElementShouldNotMoveElementToLast(MapData data) throws Exception {
        ImmutableSequencedMap<Key, Value> instance = newInstance(data.a());
        List<Map.Entry<Key, Value>> expected = new ArrayList<>(data.a().asMap().entrySet());
        for (Map.Entry<Key, Value> e : data.a()) {
            instance = instance.put(e.getKey(), e.getValue());
            assertEquals(expected.get(expected.size() - 1), instance.lastEntry());
            assertEqualSequence(expected, instance, "put");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void removeWithLastElementShouldNotChangeSequence(MapData data) throws Exception {
        ImmutableSequencedMap<Key, Value> instance = newInstance(data.a());
        List<Map.Entry<Key, Value>> expected = new ArrayList<>(data.a().asMap().entrySet());
        while (!expected.isEmpty()) {
            Map.Entry<Key, Value> e = expected.remove(expected.size() - 1);
            instance = instance.remove(e.getKey());
            assertEqualSequence(expected, instance, "remove(lastElement)");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void removeFirstShouldNotChangeSequence(MapData data) throws Exception {
        ImmutableSequencedMap<Key, Value> instance = newInstance(data.a());
        List<Map.Entry<Key, Value>> expected = new ArrayList<>(data.a().asMap().entrySet());
        while (!expected.isEmpty()) {
            Map.Entry<Key, Value> e = expected.remove(0);
            instance = instance.removeFirst();
            assertEqualSequence(expected, instance, "removeFirst");
        }
    }


    @ParameterizedTest
    @MethodSource("dataProvider")
    public void removeLastShouldNotChangeSequence(MapData data) throws Exception {
        ImmutableSequencedMap<Key, Value> instance = newInstance(data.a());
        List<Map.Entry<Key, Value>> expected = new ArrayList<>(data.a().asMap().entrySet());
        while (!expected.isEmpty()) {
            expected.remove(expected.size() - 1);
            instance = instance.removeLast();
            assertEqualSequence(expected, instance, "removeLast");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void removeWithFirstElementShouldNotChangeSequence(MapData data) throws Exception {
        ImmutableSequencedMap<Key, Value> instance = newInstance(data.a());
        List<Map.Entry<Key, Value>> expected = new ArrayList<>(data.a().asMap().entrySet());
        while (!expected.isEmpty()) {
            Map.Entry<Key, Value> e = expected.remove(0);
            instance = instance.remove(e.getKey());
            assertEqualSequence(expected, instance, "remove(firstElement)");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void removeWithMiddleElementShouldNotChangeSequence(MapData data) throws Exception {
        ImmutableSequencedMap<Key, Value> instance = newInstance(data.a());
        List<Map.Entry<Key, Value>> expected = new ArrayList<>(data.a().asMap().entrySet());
        while (!expected.isEmpty()) {
            Map.Entry<Key, Value> e = expected.remove(expected.size() / 2);
            instance = instance.remove(e.getKey());
            assertEqualSequence(expected, instance, "removeMiddle");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void putWithNewElementShouldMoveElementToLast(MapData data) throws Exception {
        ImmutableSequencedMap<Key, Value> instance = newInstance(data.a());
        List<Map.Entry<Key, Value>> expected = new ArrayList<>(data.a().asMap().entrySet());
        for (Map.Entry<Key, Value> e : data.c()) {
            instance = instance.put(e.getKey(), e.getValue());
            assertEquals(e, instance.lastEntry());
            expected.remove(e);
            expected.add(e);
            assertEqualSequence(expected, instance, "add");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void putNewValueForExistingElementShouldNotChangeSequence(MapData data) throws Exception {
        ImmutableSequencedMap<Key, Value> instance = newInstance(data.a());
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

            instance = instance.put(oldEntry.getKey(), newValue);
            expected.set(i, newEntry);
            assertEqualSequence(expected, instance, "put " + i + " oldValue: " + oldEntry + " newValue: " + newEntry);
        }
    }

    protected <K, V> void assertEqualSequence(Collection<Map.Entry<K, V>> expected, ImmutableSequencedMap<K, V> actual, String message) {
        ArrayList<Map.Entry<K, V>> expectedList = new ArrayList<>(expected);
        ArrayList<Map.Entry<K, V>> actualList = new ArrayList<>(actual.readOnlyEntrySet().asSet());
        assertEquals(expectedList, actualList, message);

        if (!expected.isEmpty()) {
            assertEquals(expectedList.get(0), actual.firstEntry(), message);
            assertEquals(expectedList.get(0), actual.readOnlyEntrySet().iterator().next(), message);
            assertEquals(expectedList.get(expectedList.size() - 1), actual.lastEntry(), message);
            assertEquals(expectedList.get(expectedList.size() - 1), actual.readOnlyReversed().readOnlyEntrySet().iterator().next(), message);
        }

        LinkedHashMap<Object, Object> x = new LinkedHashMap<>();
        for (Map.Entry<K, V> e : expected) {
            x.put(e.getKey(), e.getValue());
        }
        assertEquals(x.toString(), actual.toString(), message);
    }


    @Test
    public void spliteratorShouldHaveSequencedMapCharacteristics() throws Exception {
        ImmutableSequencedMap<Key, Value> instance = newInstance();

        assertEquals(Spliterator.NONNULL | Spliterator.IMMUTABLE | Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.SIZED,
                (Spliterator.NONNULL | Spliterator.IMMUTABLE | Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.SIZED) & instance.spliterator().characteristics());
        assertEquals(Spliterator.IMMUTABLE | Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.SIZED,
                (Spliterator.IMMUTABLE | Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.SIZED) & instance.readOnlyKeySet().spliterator().characteristics());
        assertEquals(Spliterator.NONNULL | Spliterator.IMMUTABLE | Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.SIZED,
                (Spliterator.NONNULL | Spliterator.IMMUTABLE | Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.SIZED) & instance.readOnlyEntrySet().spliterator().characteristics());
        assertEquals(Spliterator.IMMUTABLE | Spliterator.ORDERED | Spliterator.SIZED,
                (Spliterator.IMMUTABLE | Spliterator.ORDERED | Spliterator.SIZED) & instance.readOnlyValues().spliterator().characteristics());
    }


}
