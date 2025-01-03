package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.persistent.PersistentSequencedSet;
import org.jhotdraw8.icollection.persistent.PersistentSet;
import org.jhotdraw8.icollection.readable.ReadableSequencedSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.SequencedSet;
import java.util.Set;
import java.util.Spliterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public abstract class AbstractImmutableSequencedSetTest extends AbstractImmutableSetTest {
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
    public void copyRemoveLastWithEmptySetShouldThrowNoSuchElementException(SetData data) throws Exception {
        PersistentSequencedSet<Key> instance = newInstance(data.a());
        instance = instance.removeAll(data.a().asSet());
        assertThrows(NoSuchElementException.class, instance::removeLast);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void removeLastShouldNotChangeSequence(SetData data) throws Exception {
        PersistentSequencedSet<Key> instance = newInstance(data.a());
        List<Key> expected = new ArrayList<>(data.a().asSet());
        assertEqualSequence(expected, instance, "new instance(data.a())");
        while (!expected.isEmpty()) {
            PersistentSequencedSet<Key> instance2 = instance.removeLast();
            assertNotSame(instance, instance2);
            expected.remove(expected.size() - 1);
            assertEqualSequence(expected, instance2, "removeLast");
            instance = instance2;
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void removeLastStartingWithEmptySetShouldNotChangeSequence(SetData data) throws Exception {
        PersistentSequencedSet<Key> instance = newInstance();
        instance = instance.addAll(data.a.asSet());
        List<Key> expected = new ArrayList<>(data.a().asSet());
        while (!expected.isEmpty()) {
            PersistentSequencedSet<Key> instance2 = instance.removeLast();
            assertNotSame(instance, instance2);
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
