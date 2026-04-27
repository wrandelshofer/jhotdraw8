/*
 * @(#)IntArrayEnumeratorTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection.enumerator;

import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Spliterator;
import java.util.function.IntConsumer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IntArrayEnumeratorTest {

    /// Tests if it is possible to iterate over a given array
    /// using the [IntArrayEnumerator#moveNext()] method.
    ///
    /// Tests methods: [IntArrayEnumerator#estimateSize()] ,
    /// [IntArrayEnumerator#moveNext()],
    /// [IntArrayEnumerator#current()],
    /// [IntArrayEnumerator#currentAsInt()].
    @Test
    public void testMoveNext() {
        int[] a = {1, 2, 3, 4, 5};
        final IntArrayEnumerator instance = new IntArrayEnumerator(a, 0, a.length);
        assertEquals(a.length, instance.estimateSize());
        for (int j : a) {
            assertTrue(instance.moveNext());
            assertEquals(j, instance.current());
            assertEquals(j, instance.currentAsInt());
        }
        assertFalse(instance.moveNext());
    }

    /// Tests if it is possible to iterate over a given array
    /// using the [IntArrayEnumerator#tryAdvance(IntConsumer)] ()} method.
    ///
    /// Tests methods: [IntArrayEnumerator#estimateSize()] ,
    /// [IntArrayEnumerator#tryAdvance(IntConsumer)]}.
    @Test
    public void testTryAdvance() {
        int[] a = {1, 2, 3, 4, 5};
        final IntArrayEnumerator instance = new IntArrayEnumerator(a, 0, a.length);
        assertEquals(a.length, instance.estimateSize());
        int[] element = new int[1];
        for (int j : a) {
            assertTrue(instance.tryAdvance((IntConsumer) e -> element[0] = e));
            assertEquals(j, element[0]);
        }
        assertFalse(instance.tryAdvance((IntConsumer) e -> element[0] = e));
    }

    /// Tests if it is possible to iterate over a given sub-array.
    ///
    /// Tests methods: [IntArrayEnumerator#estimateSize()] ,
    /// [IntArrayEnumerator#moveNext()], [IntArrayEnumerator#current()].
    @Test
    public void testSubArray() {
        final int toExclusive = 4;
        final int from = 2;
        int[] a = {1, 2, 3, 4, 5};
        final IntArrayEnumerator instance = new IntArrayEnumerator(a, from, toExclusive);
        assertEquals(toExclusive - from, instance.estimateSize());
        for (int i = from; i < toExclusive; i++) {
            assertTrue(instance.moveNext());
            assertEquals(a[i], instance.current());
        }
    }

    /// Tests if it is possible to split the iterator once.
    ///
    /// Tests methods: [IntArrayEnumerator#trySplit()].
    @Test
    public void testTrySplit() {
        int[] a = {1, 2, 3, 4, 5};
        final IntArrayEnumerator instance = new IntArrayEnumerator(a, 0, a.length);
        final IntArrayEnumerator prefix = instance.trySplit();
        assertNotNull(prefix);
        assertEquals(a.length, instance.estimateSize() + prefix.estimateSize());

        int[] actual = new int[a.length];
        int i = 0;
        while (prefix.moveNext()) {
            actual[i++] = prefix.current;
        }
        while (instance.moveNext()) {
            actual[i++] = instance.current;
        }

        assertArrayEquals(a, actual);
    }

    /// Tests if it is possible to split the repeated times until it
    /// is too small.
    ///
    /// Tests methods: [IntArrayEnumerator#trySplit()].
    @Test
    public void testTrySplitUnlessTooSmall() {
        int[] a = {1, 2, 3, 4, 5};
        final IntArrayEnumerator instance = new IntArrayEnumerator(a, 0, a.length);
        Deque<IntArrayEnumerator> stack = new ArrayDeque<>();
        stack.push(instance);

        IntArrayEnumerator it = instance.trySplit();
        int maxIterations = a.length;
        int counter = 0;
        while (it != null && counter < maxIterations) {
            stack.push(it);
            it = it.trySplit();
            counter++;
        }
        assertTrue(counter < maxIterations);

        assertEquals(a.length, stack.stream().mapToLong(Spliterator::estimateSize).sum());

        int[] actual = new int[a.length];
        int i = 0;
        while (!stack.isEmpty()) {
            it = stack.pop();
            while (it.moveNext()) {
                actual[i++] = it.current;
            }
        }

        assertArrayEquals(a, actual);
    }
}