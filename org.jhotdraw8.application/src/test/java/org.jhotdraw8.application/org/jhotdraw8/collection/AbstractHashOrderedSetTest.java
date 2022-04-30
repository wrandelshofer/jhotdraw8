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
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * Provides tests for sets which keep elements ordered by their hash code.
 */
public abstract class AbstractHashOrderedSetTest extends AbstractSetTest {
    @TestFactory
    public @NonNull List<DynamicTest> dynamicTestsIterationSequenceByHashCode() {
        return Arrays.asList(
                dynamicTest("full mask 1..10", () -> doTestIterationSequence(-1, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)),
                dynamicTest("full mask 1..MAX_VALUE", () -> doTestIterationSequence(-1, 1, 20, 300, 4000, 50000, 600000, 7000000, 80000000, 90000000, Integer.MAX_VALUE)),
                dynamicTest("some collisions 1..10", () -> doTestIterationSequence(1, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)),
                dynamicTest("all collisions 1..10", () -> doTestIterationSequence(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)),
                dynamicTest("full mask 10..1", () -> doTestIterationSequence(-1, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1)),
                dynamicTest("some collisions 10..1", () -> doTestIterationSequence(1, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1)),
                dynamicTest("all collisions 10..1", () -> doTestIterationSequence(0, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1))
        );
    }

    @TestFactory
    public @NonNull List<DynamicTest> dynamicTestsIterationSequenceByCustomizedHashCode() {
        return Arrays.asList(
                dynamicTest("1..20", () -> doTestIterationSequenceWithCustomHashFunction(1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
                        11, 12, 13, 14, 15, 16, 17, 18, 19, 20)),
                dynamicTest("1..10", () -> doTestIterationSequenceWithCustomHashFunction(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)),
                dynamicTest("10..1", () -> doTestIterationSequenceWithCustomHashFunction(10, 9, 8, 7, 6, 5, 4, 3, 2, 1)),
                dynamicTest("-10..10", () -> doTestIterationSequenceWithCustomHashFunction(-10, -5, -4, 3, 7, 10)),
                dynamicTest("10..-10", () -> doTestIterationSequenceWithCustomHashFunction(10, 5, 4, -3, -7, -10)),
                dynamicTest("-Inf..Inf", () -> doTestIterationSequenceWithCustomHashFunction(Float.NEGATIVE_INFINITY, Float.MIN_VALUE, Float.MAX_VALUE, Float.POSITIVE_INFINITY)),
                dynamicTest("Inf..-Inf", () -> doTestIterationSequenceWithCustomHashFunction(Float.POSITIVE_INFINITY, Float.MAX_VALUE, Float.MIN_VALUE, -Float.MAX_VALUE, Float.NEGATIVE_INFINITY))
        );
    }

    @SuppressWarnings({"SlowAbstractSetRemoveAll", "unchecked"})
    public void doTestIterationSequence(int mask, int... elements) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Set<HashCollider> instance = create(0, 0.75f);

        // Add all in order
        LinkedHashSet<HashCollider> expected = new LinkedHashSet<>();
        for (int e : elements) {
            HashCollider e1 = new HashCollider(e, mask);
            expected.add(e1);
            instance.add(e1);
        }

        assertEquals(sortByHashCode(expected), new ArrayList<>(instance));


        // Remove one element in the middle
        expected.remove(elements[elements.length / 2]);
        instance.remove(elements[elements.length / 2]);
        assertEquals(sortByHashCode(expected), new ArrayList<>(instance));
    }

    private static List<HashCollider> sortByHashCode(Set<HashCollider> set) {
        ArrayList<HashCollider> list = new ArrayList<>(set);
        list.sort(Comparator.comparing(Object::hashCode));
        return list;
    }

    private static List<Float> sortByCustomHashCode(Set<Float> set) {
        ArrayList<Float> list = new ArrayList<>(set);
        list.sort(Comparator.comparing(Object::hashCode));
        return list;
    }


    @SuppressWarnings({"SlowAbstractSetRemoveAll", "unchecked"})
    public void doTestIterationSequenceWithCustomHashFunction(float... elements) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Set<Integer> instanceI = create(0, 0.75f);
        Set<Float> instance = create(0, 0.75f);

        // Add all in order
        LinkedHashSet<Float> expected = new LinkedHashSet<>();
        for (float e : elements) {
            Float e1 = e;
            expected.add(e1);
            instance.add(e1);
            instanceI.add((int) e);
            assertTrue(instanceI.contains((int) e));
        }
        if (instanceI instanceof TrieSet) {
            System.out.println(((TrieSet<Integer>) instanceI).dump());
        }
        assertEquals(sortByCustomHashCode(expected), new ArrayList<>(instance));


        // Remove one element in the middle
        expected.remove(elements[elements.length / 2]);
        instance.remove(elements[elements.length / 2]);
        assertEquals(sortByCustomHashCode(expected), new ArrayList<>(instance));
    }

}
