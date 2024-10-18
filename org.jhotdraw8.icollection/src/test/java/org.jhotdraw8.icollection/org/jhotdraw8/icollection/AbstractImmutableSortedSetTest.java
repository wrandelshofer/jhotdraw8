package org.jhotdraw8.icollection;

import org.jhotdraw8.icollection.persistent.PersistentSet;
import org.jhotdraw8.icollection.persistent.PersistentSortedSet;
import org.jhotdraw8.icollection.readable.ReadableSequencedSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.SequencedSet;
import java.util.Set;
import java.util.Spliterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public abstract class AbstractImmutableSortedSetTest extends AbstractImmutableSetTest {
    @Override
    protected abstract <E> PersistentSortedSet<E> newInstance();

    @Override
    protected abstract <E> SequencedSet<E> toMutableInstance(PersistentSet<E> m);

    @Override
    protected abstract <E> PersistentSortedSet<E> toImmutableInstance(Set<E> m);

    @Override
    protected abstract <E> PersistentSortedSet<E> toClonedInstance(PersistentSet<E> m);

    @Override
    protected abstract <E> PersistentSortedSet<E> newInstance(Iterable<E> m);


    @ParameterizedTest
    @MethodSource("dataProvider")
    public void copyRemoveLastWithEmptySetShouldThrowNoSuchElementException(SetData data) throws Exception {
        PersistentSortedSet<Key> instance = newInstance(data.a());
        instance = instance.removeAll(data.a().asSet());
        assertThrows(NoSuchElementException.class, instance::removeLast);
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
    public void spliteratorShouldHaveSortedSetCharacteristics() throws Exception {
        PersistentSortedSet<Key> instance = newInstance();

        assertEquals(Spliterator.ORDERED | Spliterator.SORTED | Spliterator.DISTINCT | Spliterator.SIZED, (Spliterator.ORDERED | Spliterator.SORTED | Spliterator.DISTINCT | Spliterator.SIZED) & instance.spliterator().characteristics());
        assertNull(instance.spliterator().getComparator());
    }

}
