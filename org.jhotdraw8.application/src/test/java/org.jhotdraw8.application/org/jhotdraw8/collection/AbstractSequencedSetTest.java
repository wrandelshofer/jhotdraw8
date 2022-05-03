/*
 * @(#)AbstractLinkedSetTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * Provides tests for sets which keep elements ordered by their insertion order.
 */
public abstract class AbstractSequencedSetTest extends AbstractSetTest {
    @SuppressWarnings({"SlowAbstractSetRemoveAll", "unchecked", "SuspiciousMethodCalls"})
    public void doTestIterationSequence(int mask, int... elements) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Set<HashCollider> instance = create(0, 0.75f);

        // Add all in order
        List<HashCollider> list = new ArrayList<>();
        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>();
        for (int e : elements) {
            HashCollider e1 = new HashCollider(e, mask);
            expected.add(e1);
            instance.add(e1);
            list.add(e1);
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

    protected <E> void assertEqualSequence(Set<E> expected, Set<E> actual, String message) {
        assertEquals(new ArrayList<>(expected), new ArrayList<>(actual), message);
        assertEquals(expected.toString(), actual.toString(), message);
    }

    @TestFactory
    public @NonNull List<DynamicTest> dynamicTestsIterationSequenceByInsertionOrder() {
        return Arrays.asList(
                dynamicTest("full mask 1..10", () -> doTestIterationSequence(-1, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)),
                dynamicTest("full mask 10..1", () -> doTestIterationSequence(-1, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1)),
                dynamicTest("full mask 1..1_000_000_000", () -> doTestIterationSequence(-1, 1, 10, 100, 1_000, 10_000, 100_000, 1_000_000, 10_000_000, 100_000_000, 1_000_000_000)),
                dynamicTest("some collisions 1..10", () -> doTestIterationSequence(1, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)),
                dynamicTest("some collisions 10..1", () -> doTestIterationSequence(1, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1)),
                dynamicTest("some collisions 1..1_000_000_000", () -> doTestIterationSequence(1, 1, 10, 100, 1_000, 10_000, 100_000, 1_000_000, 10_000_000, 100_000_000, 1_000_000_000)),
                dynamicTest("all collisions 1..10", () -> doTestIterationSequence(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)),
                dynamicTest("all collisions 10..1", () -> doTestIterationSequence(0, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1)),
                dynamicTest("all collisions 1..1_000_000_000", () -> doTestIterationSequence(0, 1, 10, 100, 1_000, 10_000, 100_000, 1_000_000, 10_000_000, 100_000_000, 1_000_000_000))
        );
    }


}
