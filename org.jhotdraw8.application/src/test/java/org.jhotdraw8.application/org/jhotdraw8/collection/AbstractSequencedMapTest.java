package org.jhotdraw8.collection;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public abstract class AbstractSequencedMapTest extends AbstractMapTest {
    /**
     * Creates a new empty instance.
     */
    protected abstract SequencedMap<HashCollider, HashCollider> newInstance();

    /**
     * Creates a new instance with the specified expected number of elements
     * and load factor.
     */
    protected abstract SequencedMap<HashCollider, HashCollider> newInstance(int numElements, float loadFactor);

    /**
     * Creates a new instance with the specified map.
     */
    protected abstract SequencedMap<HashCollider, HashCollider> newInstance(Map<HashCollider, HashCollider> m);

    protected abstract SequencedMap<HashCollider, HashCollider> newInstance(ReadOnlyMap<HashCollider, HashCollider> m);

    protected abstract ImmutableSequencedMap<HashCollider, HashCollider> toImmutableInstance(Map<HashCollider, HashCollider> m);

    protected abstract SequencedMap<HashCollider, HashCollider> toClonedInstance(Map<HashCollider, HashCollider> m);

    /**
     * Creates a new instance with the specified map.
     */
    abstract SequencedMap<HashCollider, HashCollider> newInstance(Iterable<Map.Entry<HashCollider, HashCollider>> m);

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testPutFirstWithContainedEntryShouldMoveEntryToFirst(Data data) throws Exception {
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
    public void testPutFirstWithNewElementShouldMoveElementToFirst(Data data) throws Exception {
        SequencedMap<HashCollider, HashCollider> instance = newInstance(data.a());
        List<Map.Entry<HashCollider, HashCollider>> expected = new ArrayList<>(data.a().asMap().entrySet());
        for (Map.Entry<HashCollider, HashCollider> e : data.c()) {
            instance.putFirst(e.getKey(), e.getValue());
            assertEquals(e, instance.firstEntry());
            expected.remove(e);
            expected.add(0, e);
            assertEqualSequence(expected, instance, "putFirst");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testPutLastWithContainedElementShouldMoveElementToLast(Data data) throws Exception {
        SequencedMap<HashCollider, HashCollider> instance = newInstance(data.a());
        List<Map.Entry<HashCollider, HashCollider>> expected = new ArrayList<>(data.a().asMap().entrySet());
        for (Map.Entry<HashCollider, HashCollider> e : data.a()) {
            instance.putLast(e.getKey(), e.getValue());
            assertEquals(e, instance.lastEntry());
            expected.remove(e);
            expected.add(e);
            assertEqualSequence(expected, instance, "putLast");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testPutLastWithNewElementShouldMoveElementToLast(Data data) throws Exception {
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
    public void testPutWithContainedElementShouldNotMoveElementToLast(Data data) throws Exception {
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
    public void testRemoveWithLastElementShouldNotChangeSequenc(Data data) throws Exception {
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
    public void testRemoveFirstShouldNotChangeSequence(Data data) throws Exception {
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
    public void testRemoveFirstWithEmptySetShouldReturnNull(Data data) throws Exception {
        SequencedMap<HashCollider, HashCollider> instance = newInstance(data.a());
        for (Map.Entry<HashCollider, HashCollider> e : data.a()) {
            instance.remove(e.getKey());
        }
        assertNull(instance.pollFirstEntry());
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testRemoveLastWithEmptySetShouldReturnNull(Data data) throws Exception {
        SequencedMap<HashCollider, HashCollider> instance = newInstance(data.a());
        for (Map.Entry<HashCollider, HashCollider> e : data.a()) {
            instance.remove(e.getKey());
        }
        assertNull(instance.pollLastEntry());
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testRemoveLastShouldNotChangeSequence(Data data) throws Exception {
        SequencedMap<HashCollider, HashCollider> instance = newInstance(data.a());
        List<Map.Entry<HashCollider, HashCollider>> expected = new ArrayList<>(data.a().asMap().entrySet());
        while (!expected.isEmpty()) {
            assertEquals(instance.pollLastEntry(), expected.remove(expected.size() - 1));
            assertEqualSequence(expected, instance, "removeLast");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testRemoveWithFirstElementShouldNotChangeSequence(Data data) throws Exception {
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
    public void testRemoveWithMiddleElementShouldNotChangeSequenc(Data data) throws Exception {
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
    public void testPutWithNewElementShouldMoveElementToLast(Data data) throws Exception {
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
            assertEquals(expectedList.get(0), actual.entrySet().iterator().next(), message);
            assertEquals(expectedList.get(expectedList.size() - 1), actual.lastEntry(), message);
            assertEquals(expectedList.get(expectedList.size() - 1), actual.reversed().entrySet().iterator().next(), message);
        }
        assertEquals(expectedList, new ArrayList<>(actual.entrySet()), message);

        LinkedHashMap<Object, Object> x = new LinkedHashMap<>();
        for (Map.Entry<K, V> e : expected) {
            x.put(e.getKey(), e.getValue());
        }
        assertEquals(x.toString(), actual.toString(), message);
    }
}
