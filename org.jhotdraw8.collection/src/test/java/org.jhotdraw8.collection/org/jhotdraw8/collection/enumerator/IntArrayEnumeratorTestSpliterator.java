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

public class IntArrayEnumeratorTestSpliterator {

    /**
     * Tests if it is possible to iterate over a given array
     * using the {@link IntArrayEnumeratorSpliterator#moveNext()} method.
     * <p>
     * Tests methods: {@link IntArrayEnumeratorSpliterator#estimateSize()} ,
     * {@link IntArrayEnumeratorSpliterator#moveNext()},
     * {@link IntArrayEnumeratorSpliterator#current()},
     * {@link IntArrayEnumeratorSpliterator#currentAsInt()}.
     */
    @Test
    public void testMoveNext() {
        int[] a = {1, 2, 3, 4, 5};
        final IntArrayEnumeratorSpliterator instance = new IntArrayEnumeratorSpliterator(a, 0, a.length);
        assertEquals(a.length, instance.estimateSize());
        for (int i = 0; i < a.length; i++) {
            assertTrue(instance.moveNext());
            assertEquals(a[i], instance.current());
            assertEquals(a[i], instance.currentAsInt());
        }
        assertFalse(instance.moveNext());
    }

    /**
     * Tests if it is possible to iterate over a given array
     * using the {@link IntArrayEnumeratorSpliterator#tryAdvance(IntConsumer)} ()} method.
     * <p>
     * Tests methods: {@link IntArrayEnumeratorSpliterator#estimateSize()} ,
     * {@link IntArrayEnumeratorSpliterator#tryAdvance(IntConsumer)}}.
     */
    @Test
    public void testTryAdvance() {
        int[] a = {1, 2, 3, 4, 5};
        final IntArrayEnumeratorSpliterator instance = new IntArrayEnumeratorSpliterator(a, 0, a.length);
        assertEquals(a.length, instance.estimateSize());
        int[] element = new int[1];
        for (int i = 0; i < a.length; i++) {
            assertTrue(instance.tryAdvance((IntConsumer) e -> element[0] = e));
            assertEquals(a[i], element[0]);
        }
        assertFalse(instance.tryAdvance((IntConsumer) e -> element[0] = e));
    }

    /**
     * Tests if it is possible to iterate over a given sub-array.
     * <p>
     * Tests methods: {@link IntArrayEnumeratorSpliterator#estimateSize()} ,
     * {@link IntArrayEnumeratorSpliterator#moveNext()}, {@link IntArrayEnumeratorSpliterator#current()}.
     */
    @Test
    public void testSubArray() {
        final int toExclusive = 4;
        final int from = 2;
        int[] a = {1, 2, 3, 4, 5};
        final IntArrayEnumeratorSpliterator instance = new IntArrayEnumeratorSpliterator(a, from, toExclusive);
        assertEquals(toExclusive - from, instance.estimateSize());
        for (int i = from; i < toExclusive; i++) {
            assertTrue(instance.moveNext());
            assertEquals(a[i], instance.current());
        }
    }

    /**
     * Tests if it is possible to split the iterator once.
     * <p>
     * Tests methods: {@link IntArrayEnumeratorSpliterator#trySplit()}.
     */
    @Test
    public void testTrySplit() {
        int[] a = {1, 2, 3, 4, 5};
        final IntArrayEnumeratorSpliterator instance = new IntArrayEnumeratorSpliterator(a, 0, a.length);
        final IntArrayEnumeratorSpliterator prefix = instance.trySplit();
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

    /**
     * Tests if it is possible to split the repeated times until it
     * is too small.
     * <p>
     * Tests methods: {@link IntArrayEnumeratorSpliterator#trySplit()}.
     */
    @Test
    public void testTrySplitUnlessTooSmall() {
        int[] a = {1, 2, 3, 4, 5};
        final IntArrayEnumeratorSpliterator instance = new IntArrayEnumeratorSpliterator(a, 0, a.length);
        Deque<IntArrayEnumeratorSpliterator> stack = new ArrayDeque<>();
        stack.push(instance);

        IntArrayEnumeratorSpliterator it = instance.trySplit();
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