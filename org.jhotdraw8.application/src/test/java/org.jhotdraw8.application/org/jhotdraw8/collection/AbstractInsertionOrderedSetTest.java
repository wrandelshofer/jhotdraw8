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
public abstract class AbstractInsertionOrderedSetTest extends AbstractSetTest {
    @TestFactory
    public @NonNull List<DynamicTest> dynamicTestsIterationSequenceByInsertionOrder() {
        return Arrays.asList(
                dynamicTest("full mask 1..10", () -> doTestIterationSequence(-1, -1, -2, -6, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100,
                        1000, 10_000, 100_000)),
                dynamicTest("full mask 1..10x", () -> doTestIterationSequence(-1, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)),
                dynamicTest("some collisions 1..10", () -> doTestIterationSequence(1, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)),
                dynamicTest("all collisions 1..10", () -> doTestIterationSequence(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)),
                dynamicTest("full mask 10..1", () -> doTestIterationSequence(-1, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1)),
                dynamicTest("some collisions 10..1", () -> doTestIterationSequence(1, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1)),
                dynamicTest("all collisions 10..1", () -> doTestIterationSequence(0, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1))
        );
    }

    @SuppressWarnings({"SlowAbstractSetRemoveAll", "unchecked", "SuspiciousMethodCalls"})
    public void doTestIterationSequence(int mask, int... elements) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Set<HashCollider> instance = create(0, 0.75f);
        TrieSet<Integer> set = new TrieSet<>();
        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>();
        for (int e : elements) {
            HashCollider e1 = new HashCollider(e, mask);
            expected.add(e1);
            instance.add(e1);
            set.add(e);
        }
        System.out.println(set.dump());
        // Add all in order
        assertEquals(new ArrayList<>(expected), new ArrayList<>(instance));

        // Remove one element in the middle
        expected.remove(elements[elements.length / 2]);
        instance.remove(elements[elements.length / 2]);
        assertEquals(new ArrayList<>(expected), new ArrayList<>(instance));
    }


}
