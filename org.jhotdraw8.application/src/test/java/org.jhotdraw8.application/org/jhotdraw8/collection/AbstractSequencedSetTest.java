package org.jhotdraw8.collection;


import org.jhotdraw8.annotation.NonNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class AbstractSequencedSetTest extends AbstractSetTest {
    protected abstract <E> @NonNull SequencedSet<E> newInstance();

    /**
     * Creates a new instance with the specified expected number of elements
     * and load factor.
     */
    protected abstract <E> @NonNull SequencedSet<E> newInstance(int numElements, float loadFactor);

    /**
     * Creates a new instance with the specified map.
     */
    protected abstract <E> @NonNull SequencedSet<E> newInstance(@NonNull SequencedSet<E> m);

    protected abstract <E> @NonNull SequencedSet<E> newInstance(@NonNull ReadOnlySequencedSet<E> m);

    protected abstract <E> @NonNull ImmutableSequencedSet<E> toImmutableInstance(@NonNull SequencedSet<E> m);

    protected abstract <E> @NonNull SequencedSet<E> toClonedInstance(@NonNull SequencedSet<E> m);


    /**
     * Creates a new instance with the specified map.
     */
    protected abstract <E> @NonNull SequencedSet<E> newInstance(Set<E> m);

    protected abstract <E> @NonNull SequencedSet<E> newInstance(ReadOnlySet<E> m);

    protected abstract <E> @NonNull ImmutableSequencedSet<E> toImmutableInstance(Set<E> m);

    protected abstract <E> @NonNull SequencedSet<E> toClonedInstance(Set<E> m);

    /**
     * Creates a new instance with the specified map.
     */
    protected abstract <E> @NonNull SequencedSet<E> newInstance(Iterable<E> m);

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testAddFirstWithContainedElementShouldMoveElementToFirst(Data data) throws Exception {
        SequencedSet<HashCollider> instance = newInstance(data.a());
        List<HashCollider> expected = new ArrayList<>(data.a().asSet());
        for (HashCollider e : data.a()) {
            instance.addFirst(e);
            assertEquals(e, instance.getFirst());
            expected.remove(e);
            expected.add(0, e);
            assertEqualSequence(expected, instance, "addFirst");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testAddFirstWithNewElementShouldMoveElementToFirst(Data data) throws Exception {
        SequencedSet<HashCollider> instance = newInstance(data.a());
        List<HashCollider> expected = new ArrayList<>(data.a().asSet());
        for (HashCollider e : data.c()) {
            instance.addFirst(e);
            assertEquals(e, instance.getFirst());
            expected.remove(e);
            expected.add(0, e);
            assertEqualSequence(expected, instance, "addFirst");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testAddLastWithContainedElementShouldMoveElementToLast(Data data) throws Exception {
        SequencedSet<HashCollider> instance = newInstance(data.a());
        List<HashCollider> expected = new ArrayList<>(data.a().asSet());
        for (HashCollider e : data.a()) {
            instance.addLast(e);
            assertEquals(e, instance.getLast());
            expected.remove(e);
            expected.add(e);
            assertEqualSequence(expected, instance, "addLast");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testAddLastWithNewElementShouldMoveElementToLast(Data data) throws Exception {
        SequencedSet<HashCollider> instance = newInstance(data.a());
        List<HashCollider> expected = new ArrayList<>(data.a().asSet());
        for (HashCollider e : data.c()) {
            instance.addLast(e);
            assertEquals(e, instance.getLast());
            expected.remove(e);
            expected.add(e);
            assertEqualSequence(expected, instance, "addLast");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testAddWithContainedElementShouldNotMoveElementToLast(Data data) throws Exception {
        SequencedSet<HashCollider> instance = newInstance(data.a());
        List<HashCollider> expected = new ArrayList<>(data.a().asSet());
        for (HashCollider e : data.a()) {
            instance.add(e);
            assertEquals(expected.get(expected.size() - 1), instance.getLast());
            assertEqualSequence(expected, instance, "add");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testRemoveWithLastElementShouldNotChangeSequenc(Data data) throws Exception {
        SequencedSet<HashCollider> instance = newInstance(data.a());
        List<HashCollider> expected = new ArrayList<>(data.a().asSet());
        while (!expected.isEmpty()) {
            assertTrue(instance.remove(expected.remove(expected.size() - 1)));
            assertEqualSequence(expected, instance, "remove(lastElement)");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testRemoveFirstShouldNotChangeSequence(Data data) throws Exception {
        SequencedSet<HashCollider> instance = newInstance(data.a());
        List<HashCollider> expected = new ArrayList<>(data.a().asSet());
        while (!expected.isEmpty()) {
            assertEquals(instance.removeFirst(), expected.remove(0));
            assertEqualSequence(expected, instance, "removeFirst");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testRemoveFirstWithEmptySetShouldThrowNoSuchElementException(Data data) throws Exception {
        SequencedSet<HashCollider> instance = newInstance(data.a());
        instance.removeAll(data.a().asSet());
        assertThrows(NoSuchElementException.class, instance::removeFirst);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testRemoveLastWithEmptySetShouldThrowNoSuchElementException(Data data) throws Exception {
        SequencedSet<HashCollider> instance = newInstance(data.a());
        instance.removeAll(data.a().asSet());
        assertThrows(NoSuchElementException.class, instance::removeLast);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testRemoveLastShouldNotChangeSequence(Data data) throws Exception {
        SequencedSet<HashCollider> instance = newInstance(data.a());
        List<HashCollider> expected = new ArrayList<>(data.a().asSet());
        while (!expected.isEmpty()) {
            assertEquals(instance.removeLast(), expected.remove(expected.size() - 1));
            assertEqualSequence(expected, instance, "removeLast");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testRemoveWithFirstElementShouldNotChangeSequence(Data data) throws Exception {
        SequencedSet<HashCollider> instance = newInstance(data.a());
        List<HashCollider> expected = new ArrayList<>(data.a().asSet());
        while (!expected.isEmpty()) {
            assertTrue(instance.remove(expected.remove(0)));
            assertEqualSequence(expected, instance, "remove(firstElement)");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testRemoveWithMiddleElementShouldNotChangeSequenc(Data data) throws Exception {
        SequencedSet<HashCollider> instance = newInstance(data.a());
        List<HashCollider> expected = new ArrayList<>(data.a().asSet());
        while (!expected.isEmpty()) {
            assertTrue(instance.remove(expected.remove(expected.size() / 2)));
            assertEqualSequence(expected, instance, "removeMiddle");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testAddWithNewElementShouldMoveElementToLast(Data data) throws Exception {
        SequencedSet<HashCollider> instance = newInstance(data.a());
        List<HashCollider> expected = new ArrayList<>(data.a().asSet());
        for (HashCollider e : data.c()) {
            instance.add(e);
            assertEquals(e, instance.getLast());
            expected.remove(e);
            expected.add(e);
            assertEqualSequence(expected, instance, "add");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testReversedOfReversedShouldHaveSameSequence(Data data) throws Exception {
        SequencedSet<HashCollider> instance = newInstance(data.a());
        List<HashCollider> expected = new ArrayList<>(data.a().asSet());
        ArrayList<HashCollider> actual = new ArrayList<>(instance.reversed().reversed());
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void testReversedShouldHaveReversedSequence(Data data) throws Exception {
        SequencedSet<HashCollider> instance = newInstance(data.a());
        List<HashCollider> expected = new ArrayList<>(data.a().asSet());
        Collections.reverse(expected);
        ArrayList<HashCollider> actual = new ArrayList<>(instance.reversed());
        assertEquals(expected, actual);
    }

    protected <E> void assertEqualSequence(Collection<E> expected, SequencedSet<E> actual, String message) {
        ArrayList<E> expectedList = new ArrayList<>(expected);
        if (!expected.isEmpty()) {
            assertEquals(expectedList.get(0), actual.getFirst(), message);
            assertEquals(expectedList.get(0), actual.iterator().next(), message);
            assertEquals(expectedList.get(expectedList.size() - 1), actual.getLast(), message);
            assertEquals(expectedList.get(expectedList.size() - 1), actual.reversed().iterator().next(), message);
        }
        assertEquals(expectedList, new ArrayList<>(actual), message);
        assertEquals(expected.toString(), actual.toString(), message);
    }
}
