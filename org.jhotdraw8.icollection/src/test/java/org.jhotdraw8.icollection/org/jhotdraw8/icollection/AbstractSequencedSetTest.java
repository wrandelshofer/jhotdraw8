package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.readonly.ReadOnlySequencedSet;
import org.jhotdraw8.icollection.readonly.ReadOnlySet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.SequencedSet;
import java.util.Set;
import java.util.Spliterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class AbstractSequencedSetTest extends AbstractSetTest {
    protected abstract <E> SequencedSet<E> newInstance();

    /**
     * Creates a new instance with the specified expected number of elements
     * and load factor.
     */
    protected abstract <E> SequencedSet<E> newInstance(int numElements, float loadFactor);

    /**
     * Creates a new instance with the specified map.
     */
    protected abstract <E> SequencedSet<E> newInstance(SequencedSet<E> m);

    protected abstract <E> SequencedSet<E> newInstance(ReadOnlySequencedSet<E> m);


    protected abstract <E> SequencedSet<E> toClonedInstance(SequencedSet<E> m);


    /**
     * Creates a new instance with the specified map.
     */
    protected abstract <E> SequencedSet<E> newInstance(Set<E> m);

    protected abstract <E> SequencedSet<E> newInstance(ReadOnlySet<E> m);


    protected abstract <E> SequencedSet<E> toClonedInstance(Set<E> m);

    /**
     * Creates a new instance with the specified map.
     */
    protected abstract <E> SequencedSet<E> newInstance(Iterable<E> m);

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void addFirstWithContainedElementShouldMoveElementToFirst(SetData data) throws Exception {
        SequencedSet<Key> instance = newInstance(data.a());
        List<Key> expected = new ArrayList<>(data.a().asSet());
        ArrayList<Key> shuffled = new ArrayList<>(data.a().asSet());
        Collections.shuffle(shuffled, new Random(0));
        for (Key e : shuffled) {
            instance.addFirst(e);
            assertEquals(e, instance.getFirst());
            assertEquals(e, instance.reversed().getLast());
            assertEquals(e, instance.reversed().reversed().getFirst());
            expected.remove(e);
            expected.add(0, e);
            assertEqualSequence(expected, instance, "addFirst");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void reversedAddLastWithContainedElementShouldMoveElementToFirst(SetData data) throws Exception {
        SequencedSet<Key> instance = newInstance(data.a());
        List<Key> expected = new ArrayList<>(data.a().asSet());
        ArrayList<Key> shuffled = new ArrayList<>(data.a().asSet());
        Collections.shuffle(shuffled, new Random(0));
        for (Key e : shuffled) {
            instance.reversed().addLast(e);
            assertEquals(e, instance.getFirst());
            assertEquals(e, instance.reversed().getLast());
            assertEquals(e, instance.reversed().reversed().getFirst());
            expected.remove(e);
            expected.add(0, e);
            assertEqualSequence(expected, instance, "addFirst");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void addFirstWithNewElementShouldMoveElementToFirst(SetData data) throws Exception {
        SequencedSet<Key> instance = newInstance(data.a());
        List<Key> expected = new ArrayList<>(data.a().asSet());
        for (Key e : data.c()) {
            assertFalse(instance.contains(e));
            assertFalse(instance.reversed().contains(e));
            instance.addFirst(e);
            assertEquals(e, instance.getFirst());
            assertEquals(e, instance.reversed().getLast());
            assertEquals(e, instance.reversed().reversed().getFirst());
            expected.remove(e);
            expected.add(0, e);
            assertEqualSequence(expected, instance, "addFirst");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void reversedAddLastWithNewElementShouldMoveElementToFirst(SetData data) throws Exception {
        SequencedSet<Key> instance = newInstance(data.a());
        List<Key> expected = new ArrayList<>(data.a().asSet());
        for (Key e : data.c()) {
            assertFalse(instance.contains(e));
            assertFalse(instance.reversed().contains(e));
            instance.reversed().addLast(e);
            assertEquals(e, instance.getFirst());
            assertEquals(e, instance.reversed().getLast());
            assertEquals(e, instance.reversed().reversed().getFirst());
            expected.remove(e);
            expected.add(0, e);
            assertEqualSequence(expected, instance, "addFirst");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void addLastWithContainedElementShouldMoveElementToLast(SetData data) throws Exception {
        SequencedSet<Key> instance = newInstance(data.a());
        List<Key> expected = new ArrayList<>(data.a().asSet());
        ArrayList<Key> shuffled = new ArrayList<>(data.a().asSet());
        Collections.shuffle(shuffled, new Random(0));
        for (Key e : shuffled) {
            instance.addLast(e);
            assertEquals(e, instance.getLast());
            assertEquals(e, instance.reversed().getFirst());
            assertEquals(e, instance.reversed().reversed().getLast());
            expected.remove(e);
            expected.add(e);
            assertEqualSequence(expected, instance, "addLast");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void addLastWithLastElementShouldBeIdempotent(SetData data) throws Exception {
        SequencedSet<Key> instance = newInstance(data.a());
        List<Key> expected = new ArrayList<>(data.a().asSet());
        Key e = expected.get(expected.size() - 1);
        instance.addLast(e);
        assertEqualSequence(expected, instance, "addLastIdempotent");

    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void addFirstWithFirstElementShouldBeIdempotent(SetData data) throws Exception {
        SequencedSet<Key> instance = newInstance(data.a());
        List<Key> expected = new ArrayList<>(data.a().asSet());
        Key e = expected.get(0);
        instance.addFirst(e);
        assertEqualSequence(expected, instance, "addFirstIdempotent");

    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void reversedAddFirstWithContainedElementShouldMoveElementToLast(SetData data) throws Exception {
        SequencedSet<Key> instance = newInstance(data.a());
        List<Key> expected = new ArrayList<>(data.a().asSet());
        ArrayList<Key> shuffled = new ArrayList<>(data.a().asSet());
        Collections.shuffle(shuffled, new Random(0));
        for (Key e : shuffled) {
            instance.reversed().addFirst(e);
            assertEquals(e, instance.getLast());
            assertEquals(e, instance.reversed().getFirst());
            assertEquals(e, instance.reversed().reversed().getLast());
            expected.remove(e);
            expected.add(e);
            assertEqualSequence(expected, instance, "addLast");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void addLastWithNewElementShouldMoveElementToLast(SetData data) throws Exception {
        SequencedSet<Key> instance = newInstance(data.a());
        List<Key> expected = new ArrayList<>(data.a().asSet());
        for (Key e : data.c()) {
            instance.addLast(e);
            assertEquals(e, instance.getLast());
            expected.remove(e);
            expected.add(e);
            assertEqualSequence(expected, instance, "addLast");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void reversedAddFirstWithNewElementShouldMoveElementToLast(SetData data) throws Exception {
        SequencedSet<Key> instance = newInstance(data.a());
        List<Key> expected = new ArrayList<>(data.a().asSet());
        for (Key e : data.c()) {
            instance.reversed().addFirst(e);
            assertEquals(e, instance.getLast());
            expected.remove(e);
            expected.add(e);
            assertEqualSequence(expected, instance, "addLast");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void addWithContainedElementShouldNotMoveElementToLast(SetData data) throws Exception {
        SequencedSet<Key> instance = newInstance(data.a());
        List<Key> expected = new ArrayList<>(data.a().asSet());
        ArrayList<Key> shuffled = new ArrayList<>(data.a().asSet());
        Collections.shuffle(shuffled, new Random(0));
        for (Key e : shuffled) {
            instance.add(e);
            assertEquals(expected.get(expected.size() - 1), instance.getLast());
            assertEqualSequence(expected, instance, "add");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void removeWithLastElementShouldNotChangeSequence(SetData data) throws Exception {
        SequencedSet<Key> instance = newInstance(data.a());
        List<Key> expected = new ArrayList<>(data.a().asSet());
        while (!expected.isEmpty()) {
            assertTrue(instance.remove(expected.remove(expected.size() - 1)));
            assertEqualSequence(expected, instance, "remove(lastElement)");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void removeFirstShouldNotChangeSequence(SetData data) throws Exception {
        SequencedSet<Key> instance = newInstance(data.a());
        List<Key> expected = new ArrayList<>(data.a().asSet());
        while (!expected.isEmpty()) {
            assertEquals(instance.removeFirst(), expected.remove(0));
            assertEqualSequence(expected, instance, "removeFirst");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void reversedRemoveLastShouldNotChangeSequence(SetData data) throws Exception {
        SequencedSet<Key> instance = newInstance(data.a());
        List<Key> expected = new ArrayList<>(data.a().asSet());
        while (!expected.isEmpty()) {
            assertEquals(instance.reversed().removeLast(), expected.remove(0));
            assertEqualSequence(expected, instance, "removeFirst");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void removeFirstWithEmptySetShouldThrowNoSuchElementException(SetData data) throws Exception {
        SequencedSet<Key> instance = newInstance(data.a());
        instance.removeAll(data.a().asSet());
        assertThrows(NoSuchElementException.class, instance::removeFirst);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void removeLastWithEmptySetShouldThrowNoSuchElementException(SetData data) throws Exception {
        SequencedSet<Key> instance = newInstance(data.a());
        instance.removeAll(data.a().asSet());
        assertThrows(NoSuchElementException.class, instance::removeLast);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void removeLastShouldNotChangeSequence(SetData data) throws Exception {
        SequencedSet<Key> instance = newInstance(data.a());
        List<Key> expected = new ArrayList<>(data.a().asSet());
        while (!expected.isEmpty()) {
            assertEquals(instance.removeLast(), expected.remove(expected.size() - 1));
            assertEqualSequence(expected, instance, "removeLast");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void removeLastStartingWithEmptySetShouldNotChangeSequence(SetData data) throws Exception {
        SequencedSet<Key> instance = newInstance();
        instance.addAll(data.a.asSet());
        List<Key> expected = new ArrayList<>(data.a().asSet());
        while (!expected.isEmpty()) {
            assertEquals(instance.removeLast(), expected.remove(expected.size() - 1));
            assertEqualSequence(expected, instance, "removeLast");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void reversedRemoveFirstShouldNotChangeSequence(SetData data) throws Exception {
        SequencedSet<Key> instance = newInstance(data.a());
        List<Key> expected = new ArrayList<>(data.a().asSet());
        while (!expected.isEmpty()) {
            assertEquals(instance.reversed().removeFirst(), expected.remove(expected.size() - 1));
            assertEqualSequence(expected, instance, "removeLast");
        }
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void reversedContainsShouldYieldExpectedValue(SetData data) {
        SequencedSet<Key> instance = newInstance(data.a());
        for (Key k : data.a()) {
            assertTrue(instance.reversed().contains(k));
        }
        for (Key k : data.c()) {
            assertFalse(instance.reversed().contains(k));
        }
        assertFalse(instance.reversed().contains(new Object()));

        instance.addAll(data.someAPlusSomeB().asSet());
        for (Key k : data.a()) {
            assertTrue(instance.reversed().contains(k));
        }
        for (Key k : data.someAPlusSomeB()) {
            assertTrue(instance.reversed().contains(k));
        }
        for (Key k : data.c()) {
            assertFalse(instance.reversed().contains(k));
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void removeWithFirstElementShouldNotChangeSequence(SetData data) throws Exception {
        SequencedSet<Key> instance = newInstance(data.a());
        List<Key> expected = new ArrayList<>(data.a().asSet());
        while (!expected.isEmpty()) {
            assertTrue(instance.remove(expected.remove(0)));
            assertEqualSequence(expected, instance, "remove(firstElement)");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void removeWithMiddleElementShouldNotChangeSequenc(SetData data) throws Exception {
        SequencedSet<Key> instance = newInstance(data.a());
        List<Key> expected = new ArrayList<>(data.a().asSet());
        while (!expected.isEmpty()) {
            Key removedElement = expected.remove(expected.size() / 2);
            boolean hasRemoved = instance.remove(removedElement);
            assertTrue(hasRemoved);
            assertEqualSequence(expected, instance, "removeMiddle");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void addWithNewElementShouldMoveElementToLast(SetData data) throws Exception {
        SequencedSet<Key> instance = newInstance(data.a());
        List<Key> expected = new ArrayList<>(data.a().asSet());
        for (Key e : data.c()) {
            instance.add(e);
            assertEquals(e, instance.getLast());
            expected.remove(e);
            expected.add(e);
            assertEqualSequence(expected, instance, "add");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void reversedAddWithNewElementShouldMoveElementToFirst(SetData data) throws Exception {
        SequencedSet<Key> instance = newInstance(data.a());
        List<Key> expected = new ArrayList<>(data.a().asSet());
        for (Key e : data.c()) {
            instance.reversed().add(e);
            assertEquals(e, instance.reversed().getLast());
            expected.remove(e);
            expected.add(0, e);
            assertEqualSequence(expected, instance, "add");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void reversedOfReversedShouldHaveSameSequence(SetData data) throws Exception {
        SequencedSet<Key> instance = newInstance(data.a());
        List<Key> expected = new ArrayList<>(data.a().asSet());
        ArrayList<Key> actual = new ArrayList<>(instance.reversed().reversed());
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void reversedShouldHaveReversedSequence(SetData data) throws Exception {
        SequencedSet<Key> instance = newInstance(data.a());
        List<Key> expected = new ArrayList<>(data.a().asSet());
        Collections.reverse(expected);
        ArrayList<Key> actual = new ArrayList<>(instance.reversed());
        assertEquals(expected, actual);
    }

    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void readOnlyReversedOfReadOnlyReversedShouldHaveSameSequence(SetData data) throws Exception {
        SequencedSet<Key> instance = newInstance(data.a());
        if (instance instanceof ReadOnlySequencedSet<?>) {
            ReadOnlySequencedSet<Key> readOnlyInstance = (ReadOnlySequencedSet<Key>) instance;
            List<Key> expected = new ArrayList<>(data.a().asSet());
            ArrayList<Key> actual = new ArrayList<>(readOnlyInstance.readOnlyReversed().readOnlyReversed().asSet());
            assertEquals(expected, actual);
        }
    }

    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void readOnlyReversedShouldHaveReversedSequence(SetData data) throws Exception {
        SequencedSet<Key> instance = newInstance(data.a());
        if (instance instanceof ReadOnlySequencedSet<?>) {
            ReadOnlySequencedSet<Key> readOnlyInstance = (ReadOnlySequencedSet<Key>) instance;
            List<Key> expected = new ArrayList<>(data.a().asSet());
            Collections.reverse(expected);
            ArrayList<Key> actual = new ArrayList<>(readOnlyInstance.readOnlyReversed().asSet());
            assertEquals(expected, actual);
        }
    }

    protected <E> void assertEqualSequence(Collection<E> expected, SequencedSet<E> actual, String message) {
        ArrayList<E> expectedList = new ArrayList<>(expected);
        assertEquals(expectedList, new ArrayList<>(actual), message);
        if (!expected.isEmpty()) {
            assertEquals(expectedList.get(0), actual.getFirst(), message);
            assertEquals(expectedList.get(0), actual.iterator().next(), message);
            assertEquals(expectedList.get(expectedList.size() - 1), actual.getLast(), message);
            assertEquals(expectedList.get(expectedList.size() - 1), actual.reversed().iterator().next(), message);
        }
        assertEquals(expected.toString(), actual.toString(), message);
    }

    @Test
    public void spliteratorCharacteristicsShouldHaveOrdered() throws Exception {
        SequencedSet<Key> instance = newInstance();
        assertTrue(instance.spliterator().hasCharacteristics(Spliterator.ORDERED), "spliterator should be ordered");
        assertTrue(instance.reversed().spliterator().hasCharacteristics(Spliterator.ORDERED), "spliterator should be ordered");
    }

}
