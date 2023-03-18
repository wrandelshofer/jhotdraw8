package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.immutable.ImmutableSequencedMap;
import org.jhotdraw8.collection.sequenced.SequencedMap;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public abstract class AbstractSequencedMapTest extends AbstractMapTest {
    /**
     * Creates a new empty instance.
     */
    protected abstract <K, V> @NonNull SequencedMap<K, V> newInstance();

    /**
     * Creates a new instance with the specified expected number of elements
     * and load factor.
     */
    protected abstract <K, V> @NonNull SequencedMap<K, V> newInstance(int numElements, float loadFactor);

    /**
     * Creates a new instance with the specified map.
     */
    protected abstract <K, V> @NonNull SequencedMap<K, V> newInstance(@NonNull Map<K, V> m);


    protected abstract <K, V> @NonNull ImmutableSequencedMap<K, V> toImmutableInstance(@NonNull Map<K, V> m);

    protected abstract <K, V> @NonNull SequencedMap<K, V> toClonedInstance(@NonNull Map<K, V> m);

    protected abstract <K, V> @NonNull SequencedMap<K, V> newInstance(@NonNull Iterable<Map.Entry<K, V>> m);


    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testPutFirstWithContainedEntryShouldMoveEntryToFirst(MapData data) throws Exception {
        SequencedMap<HashCollider, HashCollider> instance = newInstance(data.a());
        List<Map.Entry<HashCollider, HashCollider>> expected = new ArrayList<>(data.a().asMap().entrySet());
        for (Map.Entry<HashCollider, HashCollider> e : data.a()) {
            instance.putFirst(e.getKey(), e.getValue());
            assertEquals(e, instance.firstEntry());
            expected.remove(e);
            expected.add(0, e);
            assertEqualSequence(expected, instance, "putFirst");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testPutFirstWithNewElementShouldMoveElementToFirst(MapData data) throws Exception {
        SequencedMap<HashCollider, HashCollider> instance = newInstance(data.a());
        List<Map.Entry<HashCollider, HashCollider>> expected = new ArrayList<>(data.a().asMap().entrySet());
        for (Map.Entry<HashCollider, HashCollider> e : data.c()) {
            instance.putFirst(e.getKey(), e.getValue());
            Map.Entry<HashCollider, HashCollider> firstEntry = instance.firstEntry();
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
    public void testPutLastWithContainedElementShouldMoveElementToLast(MapData data) throws Exception {
        SequencedMap<HashCollider, HashCollider> instance = newInstance(data.a());
        List<Map.Entry<HashCollider, HashCollider>> expected = new ArrayList<>(data.a().asMap().entrySet());
        for (Map.Entry<HashCollider, HashCollider> e : data.a()) {
            instance.putLast(e.getKey(), e.getValue());
            Map.Entry<HashCollider, HashCollider> lastEntry = instance.lastEntry();
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
    public void testPutLastWithNewElementShouldMoveElementToLast(MapData data) throws Exception {
        SequencedMap<HashCollider, HashCollider> instance = newInstance(data.a());
        List<Map.Entry<HashCollider, HashCollider>> expected = new ArrayList<>(data.a().asMap().entrySet());
        for (Map.Entry<HashCollider, HashCollider> e : data.c()) {
            instance.putLast(e.getKey(), e.getValue());
            assertEquals(e, instance.lastEntry());
            expected.remove(e);
            expected.add(e);
            assertEqualSequence(expected, instance, "putLast");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testPutWithContainedElementShouldNotMoveElementToLast(MapData data) throws Exception {
        SequencedMap<HashCollider, HashCollider> instance = newInstance(data.a());
        List<Map.Entry<HashCollider, HashCollider>> expected = new ArrayList<>(data.a().asMap().entrySet());
        for (Map.Entry<HashCollider, HashCollider> e : data.a()) {
            instance.put(e.getKey(), e.getValue());
            assertEquals(expected.get(expected.size() - 1), instance.lastEntry());
            assertEqualSequence(expected, instance, "put");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testRemoveWithLastElementShouldNotChangeSequenc(MapData data) throws Exception {
        SequencedMap<HashCollider, HashCollider> instance = newInstance(data.a());
        List<Map.Entry<HashCollider, HashCollider>> expected = new ArrayList<>(data.a().asMap().entrySet());
        while (!expected.isEmpty()) {
            Map.Entry<HashCollider, HashCollider> e = expected.remove(expected.size() - 1);
            assertEquals(e.getValue(), instance.remove(e.getKey()));
            assertEqualSequence(expected, instance, "remove(lastElement)");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testRemoveFirstShouldNotChangeSequence(MapData data) throws Exception {
        SequencedMap<HashCollider, HashCollider> instance = newInstance(data.a());
        List<Map.Entry<HashCollider, HashCollider>> expected = new ArrayList<>(data.a().asMap().entrySet());
        while (!expected.isEmpty()) {
            Map.Entry<HashCollider, HashCollider> e = expected.remove(0);
            assertEquals(instance.pollFirstEntry(), e);
            assertEqualSequence(expected, instance, "removeFirst");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testRemoveFirstWithEmptySetShouldReturnNull(MapData data) throws Exception {
        SequencedMap<HashCollider, HashCollider> instance = newInstance(data.a());
        for (Map.Entry<HashCollider, HashCollider> e : data.a()) {
            instance.remove(e.getKey());
        }
        assertNull(instance.pollFirstEntry());
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testRemoveLastWithEmptySetShouldReturnNull(MapData data) throws Exception {
        SequencedMap<HashCollider, HashCollider> instance = newInstance(data.a());
        for (Map.Entry<HashCollider, HashCollider> e : data.a()) {
            instance.remove(e.getKey());
        }
        assertNull(instance.pollLastEntry());
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testRemoveLastShouldNotChangeSequence(MapData data) throws Exception {
        SequencedMap<HashCollider, HashCollider> instance = newInstance(data.a());
        List<Map.Entry<HashCollider, HashCollider>> expected = new ArrayList<>(data.a().asMap().entrySet());
        while (!expected.isEmpty()) {
            assertEquals(instance.pollLastEntry(), expected.remove(expected.size() - 1));
            assertEqualSequence(expected, instance, "removeLast");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testRemoveWithFirstElementShouldNotChangeSequence(MapData data) throws Exception {
        SequencedMap<HashCollider, HashCollider> instance = newInstance(data.a());
        List<Map.Entry<HashCollider, HashCollider>> expected = new ArrayList<>(data.a().asMap().entrySet());
        while (!expected.isEmpty()) {
            Map.Entry<HashCollider, HashCollider> e = expected.remove(0);
            assertEquals(e.getValue(), instance.remove(e.getKey()));
            assertEqualSequence(expected, instance, "remove(firstElement)");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testRemoveWithMiddleElementShouldNotChangeSequenc(MapData data) throws Exception {
        SequencedMap<HashCollider, HashCollider> instance = newInstance(data.a());
        List<Map.Entry<HashCollider, HashCollider>> expected = new ArrayList<>(data.a().asMap().entrySet());
        while (!expected.isEmpty()) {
            Map.Entry<HashCollider, HashCollider> e = expected.remove(expected.size() / 2);
            assertEquals(e.getValue(), instance.remove(e.getKey()));
            assertEqualSequence(expected, instance, "removeMiddle");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testPutWithNewElementShouldMoveElementToLast(MapData data) throws Exception {
        SequencedMap<HashCollider, HashCollider> instance = newInstance(data.a());
        List<Map.Entry<HashCollider, HashCollider>> expected = new ArrayList<>(data.a().asMap().entrySet());
        for (Map.Entry<HashCollider, HashCollider> e : data.c()) {
            instance.put(e.getKey(), e.getValue());
            assertEquals(e, instance.lastEntry());
            expected.remove(e);
            expected.add(e);
            assertEqualSequence(expected, instance, "add");
        }
    }

    protected <K, V> void assertEqualSequence(Collection<Map.Entry<K, V>> expected, SequencedMap<K, V> actual, String message) {
        ArrayList<Map.Entry<K, V>> expectedList = new ArrayList<>(expected);
        if (!expected.isEmpty()) {
            assertEquals(expectedList.get(0), actual.firstEntry(), message);
            assertEquals(expectedList.get(0), actual.sequencedEntrySet().iterator().next(), message);
            assertEquals(expectedList.get(expectedList.size() - 1), actual.lastEntry(), message);
            assertEquals(expectedList.get(expectedList.size() - 1), actual.reversed().sequencedEntrySet().iterator().next(), message);
        }
        assertEquals(expectedList, new ArrayList<>(actual.sequencedEntrySet()), message);

        LinkedHashMap<Object, Object> x = new LinkedHashMap<>();
        for (Map.Entry<K, V> e : expected) {
            x.put(e.getKey(), e.getValue());
        }
        assertEquals(x.toString(), actual.toString(), message);
    }
}
