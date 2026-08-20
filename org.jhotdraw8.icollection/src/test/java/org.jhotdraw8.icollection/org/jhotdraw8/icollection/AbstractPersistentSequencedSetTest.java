package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.persistent.PersistentSequencedSet;
import org.jhotdraw8.icollection.persistent.PersistentSet;
import org.jhotdraw8.icollection.readable.ReadableSequencedSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.SequencedSet;
import java.util.Set;
import java.util.Spliterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class AbstractPersistentSequencedSetTest extends AbstractPersistentSetTest {
    @Override
    protected abstract <E> PersistentSequencedSet<E> newInstance();

    @Override
    protected abstract <E> SequencedSet<E> toMutableInstance(PersistentSet<E> m);

    @Override
    protected abstract <E> PersistentSequencedSet<E> toImmutableInstance(Set<E> m);

    @Override
    protected abstract <E> PersistentSequencedSet<E> toClonedInstance(PersistentSet<E> m);

    @Override
    protected abstract <E> PersistentSequencedSet<E> newInstance(Iterable<E> m);

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void addFirstWithContainedElementShouldMoveElementToFirst(SetData data) throws Exception {
        PersistentSequencedSet<Key> instance = newInstance(data.a());
        List<Key> expected = new ArrayList<>(data.a().asSet());
        assertEqualSequence(expected, instance, "initial");
        for (Key e : data.a()) {
            instance = instance.addingFirst(e);
            expected.remove(e);
            expected.addFirst(e);
            assertEqualSequence(expected, instance, "addFirst");
            assertEquals(e, instance.getFirst());
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void addLastWithContainedElementShouldMoveElementToLast(SetData data) throws Exception {
        PersistentSequencedSet<Key> instance = newInstance(data.a());
        List<Key> expected = new ArrayList<>(data.a().asSet());
        List<Key> listA = new ArrayList<>(data.a().asSet());
        assertEqualSequence(expected, instance, "initial");
        for (Key e : listA.reversed()) {
            instance = instance.addingLast(e);
            expected.remove(e);
            expected.addLast(e);
            assertEqualSequence(expected, instance, "addLast");
            assertEquals(e, instance.getLast());
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void removeLastWithEmptySetShouldThrowNoSuchElementException(SetData data) throws Exception {
        PersistentSequencedSet<Key> instance = newInstance(data.a());
        instance = instance.removingAll(data.a().asSet());
        assertThrows(NoSuchElementException.class, instance::removingLast);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void removeLastShouldNotChangeSequence(SetData data) throws Exception {
        PersistentSequencedSet<Key> instance = newInstance(data.a());
        List<Key> expected = new ArrayList<>(data.a().asSet());
        assertEqualSequence(expected, instance, "new instance(data.a())");
        while (!expected.isEmpty()) {
            PersistentSequencedSet<Key> instance2 = instance.removingLast();
            assertNotSame(instance, instance2);
            expected.removeLast();
            assertEqualSequence(expected, instance2, "removeLast");
            instance = instance2;
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void shouldIterateOverElementsInSequence(SetData data) {
        PersistentSequencedSet<Key> instance = newInstance(data.a());
        Iterator<Key> actual = instance.iterator();
        Iterator<Key> expected = data.a().iterator();
        assertEquals(actual.hasNext(), expected.hasNext());
        while (expected.hasNext()) {
            assertTrue(actual.hasNext());
            Key actualKey = actual.next();
            Key expectedKey = expected.next();
            assertEquals(expectedKey, actualKey);
        }
        assertFalse(actual.hasNext());
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void removeLastStartingWithEmptySetShouldNotChangeSequence(SetData data) throws Exception {
        PersistentSequencedSet<Key> instance = newInstance();
        instance = instance.addingAll(data.a.asSet());
        List<Key> expected = new ArrayList<>(data.a().asSet());
        while (!expected.isEmpty()) {
            PersistentSequencedSet<Key> instance2 = instance.removingLast();
            assertNotSame(instance, instance2);
            //noinspection SequencedCollectionMethodCanBeUsed
            expected.remove(expected.size() - 1);
            assertEqualSequence(expected, instance2, "removeLast");
            instance = instance2;
        }
    }


    protected <E> void assertEqualSequence(Collection<E> expected, ReadableSequencedSet<E> actual, String message) {
        ArrayList<E> expectedList = new ArrayList<>(expected);
        assertEquals(expectedList, new ArrayList<>(actual.asSet()), message);
        if (!expected.isEmpty()) {
            assertEquals(expectedList.get(0), actual.getFirst(), message);
            assertEquals(expectedList.get(0), actual.iterator().next(), message);
            assertEquals(expectedList.get(expectedList.size() - 1), actual.getLast(), message);
            //assertEquals(expectedList.get(expectedList.size() - 1), actual.reversed().iterator().next(), message);
        }
        assertEquals(expected.toString(), actual.toString(), message);
    }

    @Test
    public void spliteratorShouldSupportEncounterOrder() throws Exception {
        PersistentSet<Key> instance = newInstance();
        assertEquals(instance.spliterator().characteristics() & Spliterator.ORDERED, Spliterator.ORDERED, "set should be ordered");
    }

}
