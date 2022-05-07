/*
 * @(#)AbstractPersistentSequencedMapTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public abstract class AbstractPersistentSequencedMapTest extends AbstractImmutableMapTest {
    @SuppressWarnings({"SlowAbstractSetRemoveAll", "unchecked", "SuspiciousMethodCalls"})
    public void doTestIterationSequence(int mask, int... elements) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        // Add all in order
        List<HashCollider> list = new ArrayList<>();
        Map<HashCollider, HashCollider> expected = new LinkedHashMap<>();
        for (int e : elements) {
            HashCollider e1 = new HashCollider(e, mask);
            list.add(e1);
            expected.put(e1, e1);
        }
        ImmutableMap<HashCollider, HashCollider> instance = copyOf(expected);

        System.out.println((((ImmutableSeqChampMap) instance).dump()));

        assertEqualSequence(expected, instance.asMap(), "after adding all");

        // Remove one element in the middle
        HashCollider middle = list.get(list.size() / 2);
        list.remove(list.size() / 2);
        expected.remove(middle);
        instance = instance.copyRemove(middle);
        assertEqualSequence(expected, instance.asMap(), "after removing " + middle + " from the middle of the sequence");

        // Add the removed element
        list.add(middle);
        expected.put(middle, middle);
        instance = instance.copyPut(middle, middle);
        assertEqualSequence(expected, instance.asMap(), "after adding " + middle + " to the end");

        // Get another element from the middle
        // Add the element from the middle - this must not reorder the instance,
        // because the element is already present
        middle = list.get(list.size() / 2);
        expected.put(middle, middle);
        instance = instance.copyPut(middle, middle);
        assertEqualSequence(expected, instance.asMap(), "after adding " + middle + " which is already in the map");

        // Get the first element:
        HashCollider firstKey1 = instance.keys().next();
        HashCollider firstKey2 = instance.iterator().next().getKey();
        assertEquals(list.get(0), firstKey1);
        assertEquals(list.get(0), firstKey2);
    }

    protected <K, V> void assertEqualSequence(Map<K, V> expected, Map<K, V> actual, String message) {
        assertEquals(new ArrayList<>(expected.keySet()), new ArrayList<>(actual.keySet()), message);
        assertEquals(new ArrayList<>(expected.entrySet()), new ArrayList<>(actual.entrySet()), message);
        assertEquals(new ArrayList<>(expected.values()), new ArrayList<>(actual.values()), message);
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
