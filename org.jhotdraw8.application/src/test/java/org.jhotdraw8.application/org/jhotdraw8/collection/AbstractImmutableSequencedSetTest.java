/*
 * @(#)AbstractPersistentSequencedSetTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public abstract class AbstractImmutableSequencedSetTest extends AbstractImmutableSetTest {
    @SuppressWarnings({"unchecked", "SuspiciousMethodCalls"})
    public void doTestIterationSequence(int mask, int... elements) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        // Add all in order
        List<HashCollider> list = new ArrayList<>();
        for (int e : elements) {
            HashCollider e1 = new HashCollider(e, mask);
            list.add(e1);
        }

        ImmutableSet<HashCollider> instance = copyOf(list);
        assertEquals(list, new ArrayList<>(instance.asCollection()));

        // Remove one element in the middle
        HashCollider middle = list.get(list.size() / 2);
        list.remove(list.size() / 2);
        instance = instance.copyRemove(middle);
        assertEquals(list, new ArrayList<>(instance.asSet()));

        // Add the removed element
        list.add(middle);
        instance = instance.copyAdd(middle);
        assertEquals(list, new ArrayList<>(instance.asCollection()));

        // Get another element from the middle
        // Add the element from the middle - this must not reorder the instance,
        // because the element is already present
        middle = list.get(list.size() / 2);
        instance = instance.copyAdd(middle);
        assertEquals(list, new ArrayList<>(instance.asCollection()));
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
