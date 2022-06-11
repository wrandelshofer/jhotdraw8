/*
 * @(#)AbstractSequencedSetTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * Provides tests for sets which keep elements ordered by their insertion order.
 */
public abstract class AbstractSequencedSetTest extends AbstractSetTest {
    @Override
    protected abstract <T> @NonNull SequencedSet<T> create(int expectedMaxSize, float maxLoadFactor);

    protected void assertEqualSets(LinkedHashSet<HashCollider> expected, Set<HashCollider> instance) {
        super.assertEqualSets(expected, instance);
        assertEquals(expected.toString(), instance.toString());
    }

    public void doTest(int mask, int... elements) throws Exception {
        List<HashCollider> list = new ArrayList<>();
        for (int e : elements) {
            HashCollider e1 = new HashCollider(e, mask);
            list.add(e1);
        }
        super.doTest(mask, elements);
        doTestIterationSequence(list);
        doTestAddFirst(list);
        doTestAddLast(list);
    }

    public void doTestAddFirst(List<HashCollider> list) throws Exception {
        SequencedSet<HashCollider> instance = create(0, 0.75f);
        ArrayList<HashCollider> reversed = new ArrayList<>(list);
        Collections.reverse(reversed);

        // WHEN getFirst is invoked
        // THEN throws NoSuchElementException
        assertThrows(NoSuchElementException.class, instance::getFirst);

        // WHEN new elements are added with addFirst
        // THEN the set must have the reversed sequence
        for (HashCollider e : list) {
            instance.addFirst(e);
            assertEquals(e, instance.getFirst());
        }
        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>(reversed);
        assertEqualSequence(expected, instance, "after addFirst of new elements");

        // WHEN existing elements are added in reverse with addFirst
        // THEN the set must have the non-reversed sequence
        for (HashCollider e : reversed) {
            instance.addFirst(e);
        }
        expected = new LinkedHashSet<>(list);
        assertEqualSequence(expected, instance, "after addFirst of existing elements");
    }

    public void doTestAddLast(List<HashCollider> list) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        SequencedSet<HashCollider> instance = create(0, 0.75f);
        ArrayList<HashCollider> reversed = new ArrayList<>(list);
        Collections.reverse(reversed);

        // WHEN getLast is invoked
        // THEN throws NoSuchElementException
        assertThrows(NoSuchElementException.class, instance::getLast);

        // WHEN new elements are added with addFirst
        // THEN the set must have the list sequence
        for (HashCollider e : list) {
            instance.addLast(e);
            assertEquals(e, instance.getLast());
        }
        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>(list);
        assertEqualSequence(expected, instance, "after addFirst of new elements");

        // WHEN existing elements are added in reverse with addFirst
        // THEN the set must have the reversed sequence
        for (HashCollider e : reversed) {
            instance.addLast(e);
        }
        expected = new LinkedHashSet<>(reversed);
        assertEqualSequence(expected, instance, "after addFirst of existing elements");
    }

    public void doTestIterationSequence(List<HashCollider> list) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Set<HashCollider> instance = create(0, 0.75f);

        // Add all in order
        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>();
        for (HashCollider e1 : list) {
            expected.add(e1);
            instance.add(e1);
        }

        assertEqualSequence(expected, instance, "after adding all");

        // Remove one element in the middle
        HashCollider middle = list.get(list.size() / 2);
        list.remove(list.size() / 2);
        expected.remove(middle);
        instance.remove(middle);
        assertEqualSequence(expected, instance, "after removing " + middle + " from the middle of the sequence");

        // Add the removed element to the end
        list.add(middle);
        instance.add(middle);
        expected.add(middle);
        assertEqualSequence(expected, instance, "after adding " + middle + " to the end");

        // Get another element from the middle
        // Add the element from the middle - this must not reorder the instance,
        // because the element is already present
        middle = list.get(list.size() / 2);
        expected.add(middle);
        instance.add(middle);
        assertEqualSequence(expected, instance, "after adding " + middle + " which is already in the set");
    }

    protected <E> void assertEqualSequence(Collection<E> expected, Set<E> actual, String message) {
        ArrayList<E> expectedList = new ArrayList<>(expected);
        assertEquals(expectedList, new ArrayList<>(actual), message);
        assertEquals(expected.toString(), actual.toString(), message);

        Collections.reverse(expectedList);
        //assertEquals(expectedList, new ArrayList<>(actual.reversed()), message);
    }

    @TestFactory
    public @NonNull List<DynamicTest> dynamicTestsIterationSequenceByInsertionOrder() {
        return Arrays.asList(
                dynamicTest("full mask 1..10", () -> doTest(-1, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)),
                dynamicTest("full mask 10..1", () -> doTest(-1, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1)),
                dynamicTest("full mask 1..1_000_000_000", () -> doTest(-1, 1, 10, 100, 1_000, 10_000, 100_000, 1_000_000, 10_000_000, 100_000_000, 1_000_000_000)),
                dynamicTest("some collisions 1..10", () -> doTest(1, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)),
                dynamicTest("some collisions 10..1", () -> doTest(1, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1)),
                dynamicTest("some collisions 1..1_000_000_000", () -> doTest(1, 1, 10, 100, 1_000, 10_000, 100_000, 1_000_000, 10_000_000, 100_000_000, 1_000_000_000)),
                dynamicTest("all collisions 1..10", () -> doTest(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)),
                dynamicTest("all collisions 10..1", () -> doTest(0, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1)),
                dynamicTest("all collisions 1..1_000_000_000", () -> doTest(0, 1, 10, 100, 1_000, 10_000, 100_000, 1_000_000, 10_000_000, 100_000_000, 1_000_000_000))
        );
    }


}
