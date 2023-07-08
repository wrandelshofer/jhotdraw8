package org.jhotdraw8.pcollection;


import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.pcollection.immutable.ImmutableSequencedSet;
import org.jhotdraw8.pcollection.readonly.ReadOnlySequencedSet;
import org.jhotdraw8.pcollection.readonly.ReadOnlySet;
import org.jhotdraw8.pcollection.sequenced.SequencedSet;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
    public void addFirstWithContainedElementShouldMoveElementToFirst(@NonNull SetData data) throws Exception {
        SequencedSet<HashCollider> instance = newInstance(data.a());
        List<HashCollider> expected = new ArrayList<>(data.a().asSet());
        ArrayList<HashCollider> shuffled = new ArrayList<>(data.a().asSet());
        Collections.shuffle(shuffled, new Random(0));
        for (HashCollider e : shuffled) {
            instance.addFirst(e);
            assertEquals(e, instance.getFirst());
            assertEquals(e, instance._reversed().getLast());
            assertEquals(e, instance._reversed()._reversed().getFirst());
            expected.remove(e);
            expected.add(0, e);
            assertEqualSequence(expected, instance, "addFirst");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void reversedAddLastWithContainedElementShouldMoveElementToFirst(@NonNull SetData data) throws Exception {
        SequencedSet<HashCollider> instance = newInstance(data.a());
        List<HashCollider> expected = new ArrayList<>(data.a().asSet());
        ArrayList<HashCollider> shuffled = new ArrayList<>(data.a().asSet());
        Collections.shuffle(shuffled, new Random(0));
        for (HashCollider e : shuffled) {
            instance._reversed().addLast(e);
            assertEquals(e, instance.getFirst());
            assertEquals(e, instance._reversed().getLast());
            assertEquals(e, instance._reversed()._reversed().getFirst());
            expected.remove(e);
            expected.add(0, e);
            assertEqualSequence(expected, instance, "addFirst");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void addFirstWithNewElementShouldMoveElementToFirst(@NonNull SetData data) throws Exception {
        SequencedSet<HashCollider> instance = newInstance(data.a());
        List<HashCollider> expected = new ArrayList<>(data.a().asSet());
        for (HashCollider e : data.c()) {
            assertFalse(instance.contains(e));
            assertFalse(instance._reversed().contains(e));
            instance.addFirst(e);
            assertEquals(e, instance.getFirst());
            assertEquals(e, instance._reversed().getLast());
            assertEquals(e, instance._reversed()._reversed().getFirst());
            expected.remove(e);
            expected.add(0, e);
            assertEqualSequence(expected, instance, "addFirst");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void reversedAddLastWithNewElementShouldMoveElementToFirst(@NonNull SetData data) throws Exception {
        SequencedSet<HashCollider> instance = newInstance(data.a());
        List<HashCollider> expected = new ArrayList<>(data.a().asSet());
        for (HashCollider e : data.c()) {
            assertFalse(instance.contains(e));
            assertFalse(instance._reversed().contains(e));
            instance._reversed().addLast(e);
            assertEquals(e, instance.getFirst());
            assertEquals(e, instance._reversed().getLast());
            assertEquals(e, instance._reversed()._reversed().getFirst());
            expected.remove(e);
            expected.add(0, e);
            assertEqualSequence(expected, instance, "addFirst");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void addLastWithContainedElementShouldMoveElementToLast(@NonNull SetData data) throws Exception {
        SequencedSet<HashCollider> instance = newInstance(data.a());
        List<HashCollider> expected = new ArrayList<>(data.a().asSet());
        ArrayList<HashCollider> shuffled = new ArrayList<>(data.a().asSet());
        Collections.shuffle(shuffled, new Random(0));
        for (HashCollider e : shuffled) {
            instance.addLast(e);
            assertEquals(e, instance.getLast());
            assertEquals(e, instance._reversed().getFirst());
            assertEquals(e, instance._reversed()._reversed().getLast());
            expected.remove(e);
            expected.add(e);
            assertEqualSequence(expected, instance, "addLast");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void addLastWithLastElementShouldBeIdempotent(@NonNull SetData data) throws Exception {
        SequencedSet<HashCollider> instance = newInstance(data.a());
        List<HashCollider> expected = new ArrayList<>(data.a().asSet());
        HashCollider e = expected.get(expected.size() - 1);
        instance.addLast(e);
        assertEqualSequence(expected, instance, "addLastIdempotent");

    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void addFirstWithFirstElementShouldBeIdempotent(@NonNull SetData data) throws Exception {
        SequencedSet<HashCollider> instance = newInstance(data.a());
        List<HashCollider> expected = new ArrayList<>(data.a().asSet());
        HashCollider e = expected.get(0);
        instance.addFirst(e);
        assertEqualSequence(expected, instance, "addFirstIdempotent");

    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void reversedAddFirstWithContainedElementShouldMoveElementToLast(@NonNull SetData data) throws Exception {
        SequencedSet<HashCollider> instance = newInstance(data.a());
        List<HashCollider> expected = new ArrayList<>(data.a().asSet());
        ArrayList<HashCollider> shuffled = new ArrayList<>(data.a().asSet());
        Collections.shuffle(shuffled, new Random(0));
        for (HashCollider e : shuffled) {
            instance._reversed().addFirst(e);
            assertEquals(e, instance.getLast());
            assertEquals(e, instance._reversed().getFirst());
            assertEquals(e, instance._reversed()._reversed().getLast());
            expected.remove(e);
            expected.add(e);
            assertEqualSequence(expected, instance, "addLast");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void addLastWithNewElementShouldMoveElementToLast(@NonNull SetData data) throws Exception {
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
    public void reversedAddFirstWithNewElementShouldMoveElementToLast(@NonNull SetData data) throws Exception {
        SequencedSet<HashCollider> instance = newInstance(data.a());
        List<HashCollider> expected = new ArrayList<>(data.a().asSet());
        for (HashCollider e : data.c()) {
            instance._reversed().addFirst(e);
            assertEquals(e, instance.getLast());
            expected.remove(e);
            expected.add(e);
            assertEqualSequence(expected, instance, "addLast");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void addWithContainedElementShouldNotMoveElementToLast(@NonNull SetData data) throws Exception {
        SequencedSet<HashCollider> instance = newInstance(data.a());
        List<HashCollider> expected = new ArrayList<>(data.a().asSet());
        ArrayList<HashCollider> shuffled = new ArrayList<>(data.a().asSet());
        Collections.shuffle(shuffled, new Random(0));
        for (HashCollider e : shuffled) {
            instance.add(e);
            assertEquals(expected.get(expected.size() - 1), instance.getLast());
            assertEqualSequence(expected, instance, "add");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void removeWithLastElementShouldNotChangeSequence(@NonNull SetData data) throws Exception {
        SequencedSet<HashCollider> instance = newInstance(data.a());
        List<HashCollider> expected = new ArrayList<>(data.a().asSet());
        while (!expected.isEmpty()) {
            assertTrue(instance.remove(expected.remove(expected.size() - 1)));
            assertEqualSequence(expected, instance, "remove(lastElement)");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void removeFirstShouldNotChangeSequence(@NonNull SetData data) throws Exception {
        SequencedSet<HashCollider> instance = newInstance(data.a());
        List<HashCollider> expected = new ArrayList<>(data.a().asSet());
        while (!expected.isEmpty()) {
            assertEquals(instance.removeFirst(), expected.remove(0));
            assertEqualSequence(expected, instance, "removeFirst");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void reversedRemoveLastShouldNotChangeSequence(@NonNull SetData data) throws Exception {
        SequencedSet<HashCollider> instance = newInstance(data.a());
        List<HashCollider> expected = new ArrayList<>(data.a().asSet());
        while (!expected.isEmpty()) {
            assertEquals(instance._reversed().removeLast(), expected.remove(0));
            assertEqualSequence(expected, instance, "removeFirst");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void removeFirstWithEmptySetShouldThrowNoSuchElementException(@NonNull SetData data) throws Exception {
        SequencedSet<HashCollider> instance = newInstance(data.a());
        instance.removeAll(data.a().asSet());
        assertThrows(NoSuchElementException.class, instance::removeFirst);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void removeLastWithEmptySetShouldThrowNoSuchElementException(@NonNull SetData data) throws Exception {
        SequencedSet<HashCollider> instance = newInstance(data.a());
        instance.removeAll(data.a().asSet());
        assertThrows(NoSuchElementException.class, instance::removeLast);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void removeLastShouldNotChangeSequence(@NonNull SetData data) throws Exception {
        SequencedSet<HashCollider> instance = newInstance(data.a());
        List<HashCollider> expected = new ArrayList<>(data.a().asSet());
        while (!expected.isEmpty()) {
            assertEquals(instance.removeLast(), expected.remove(expected.size() - 1));
            assertEqualSequence(expected, instance, "removeLast");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void removeLastStartingWithEmptySetShouldNotChangeSequence(@NonNull SetData data) throws Exception {
        SequencedSet<HashCollider> instance = newInstance();
        instance.addAll(data.a.asSet());
        List<HashCollider> expected = new ArrayList<>(data.a().asSet());
        while (!expected.isEmpty()) {
            assertEquals(instance.removeLast(), expected.remove(expected.size() - 1));
            assertEqualSequence(expected, instance, "removeLast");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void reversedRemoveFirstShouldNotChangeSequence(@NonNull SetData data) throws Exception {
        SequencedSet<HashCollider> instance = newInstance(data.a());
        List<HashCollider> expected = new ArrayList<>(data.a().asSet());
        while (!expected.isEmpty()) {
            assertEquals(instance._reversed().removeFirst(), expected.remove(expected.size() - 1));
            assertEqualSequence(expected, instance, "removeLast");
        }
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void reversedContainsShouldYieldExpectedValue(@NonNull SetData data) {
        SequencedSet<HashCollider> instance = newInstance(data.a());
        for (HashCollider k : data.a()) {
            assertTrue(instance._reversed().contains(k));
        }
        for (HashCollider k : data.c()) {
            assertFalse(instance._reversed().contains(k));
        }
        assertFalse(instance._reversed().contains(new Object()));

        instance.addAll(data.someAPlusSomeB().asSet());
        for (HashCollider k : data.a()) {
            assertTrue(instance._reversed().contains(k));
        }
        for (HashCollider k : data.someAPlusSomeB()) {
            assertTrue(instance._reversed().contains(k));
        }
        for (HashCollider k : data.c()) {
            assertFalse(instance._reversed().contains(k));
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void removeWithFirstElementShouldNotChangeSequence(@NonNull SetData data) throws Exception {
        SequencedSet<HashCollider> instance = newInstance(data.a());
        List<HashCollider> expected = new ArrayList<>(data.a().asSet());
        while (!expected.isEmpty()) {
            assertTrue(instance.remove(expected.remove(0)));
            assertEqualSequence(expected, instance, "remove(firstElement)");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void removeWithMiddleElementShouldNotChangeSequenc(@NonNull SetData data) throws Exception {
        SequencedSet<HashCollider> instance = newInstance(data.a());
        List<HashCollider> expected = new ArrayList<>(data.a().asSet());
        while (!expected.isEmpty()) {
            HashCollider removedElement = expected.remove(expected.size() / 2);
            boolean hasRemoved = instance.remove(removedElement);
            assertTrue(hasRemoved);
            assertEqualSequence(expected, instance, "removeMiddle");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void addWithNewElementShouldMoveElementToLast(@NonNull SetData data) throws Exception {
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
    public void reversedAddWithNewElementShouldMoveElementToFirst(@NonNull SetData data) throws Exception {
        SequencedSet<HashCollider> instance = newInstance(data.a());
        List<HashCollider> expected = new ArrayList<>(data.a().asSet());
        for (HashCollider e : data.c()) {
            instance._reversed().add(e);
            assertEquals(e, instance._reversed().getLast());
            expected.remove(e);
            expected.add(0, e);
            assertEqualSequence(expected, instance, "add");
        }
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void reversedOfReversedShouldHaveSameSequence(@NonNull SetData data) throws Exception {
        SequencedSet<HashCollider> instance = newInstance(data.a());
        List<HashCollider> expected = new ArrayList<>(data.a().asSet());
        ArrayList<HashCollider> actual = new ArrayList<>(instance._reversed()._reversed());
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    public void reversedShouldHaveReversedSequence(@NonNull SetData data) throws Exception {
        SequencedSet<HashCollider> instance = newInstance(data.a());
        List<HashCollider> expected = new ArrayList<>(data.a().asSet());
        Collections.reverse(expected);
        ArrayList<HashCollider> actual = new ArrayList<>(instance._reversed());
        assertEquals(expected, actual);
    }

    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void readOnlyReversedOfReadOnlyReversedShouldHaveSameSequence(@NonNull SetData data) throws Exception {
        SequencedSet<HashCollider> instance = newInstance(data.a());
        if (instance instanceof ReadOnlySequencedSet<?>) {
            ReadOnlySequencedSet<HashCollider> readOnlyInstance = (ReadOnlySequencedSet<HashCollider>) instance;
            List<HashCollider> expected = new ArrayList<>(data.a().asSet());
            ArrayList<HashCollider> actual = new ArrayList<>(readOnlyInstance.readOnlyReversed().readOnlyReversed().asSet());
            assertEquals(expected, actual);
        }
    }

    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @MethodSource("dataProvider")
    public void readOnlyReversedShouldHaveReversedSequence(@NonNull SetData data) throws Exception {
        SequencedSet<HashCollider> instance = newInstance(data.a());
        if (instance instanceof ReadOnlySequencedSet<?>) {
            ReadOnlySequencedSet<HashCollider> readOnlyInstance = (ReadOnlySequencedSet<HashCollider>) instance;
            List<HashCollider> expected = new ArrayList<>(data.a().asSet());
            Collections.reverse(expected);
            ArrayList<HashCollider> actual = new ArrayList<>(readOnlyInstance.readOnlyReversed().asSet());
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
            assertEquals(expectedList.get(expectedList.size() - 1), actual._reversed().iterator().next(), message);
        }
        assertEquals(expected.toString(), actual.toString(), message);
    }
}
