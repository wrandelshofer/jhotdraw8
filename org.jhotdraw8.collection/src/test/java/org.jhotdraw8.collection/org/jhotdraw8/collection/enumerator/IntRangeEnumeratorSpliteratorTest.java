/*
 * @(#)IntRangeEnumeratorTest.java
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

/**
 * Tests {@link IntRangeEnumerator}.
 *
 */
public class IntRangeEnumeratorSpliteratorTest {

    /**
     * Tests if it is possible to iterate over a given array
     * using the {@link IntRangeEnumerator#moveNext()} method.
     * <p>
     * Tests methods: {@link IntRangeEnumerator#estimateSize()} ,
     * {@link IntRangeEnumerator#moveNext()},
     * {@link IntRangeEnumerator#current()},
     * {@link IntRangeEnumerator#currentAsInt()}.
     */
    @Test
    public void testMoveNextWithRangeFrom0() {
        int[] a = {0, 1, 2, 3, 4, 5};
        final IntRangeEnumerator instance = new IntRangeEnumerator(6);
        assertEquals(a.length, instance.estimateSize());
        for (int j : a) {
            assertTrue(instance.moveNext());
            assertEquals(j, instance.current());
            assertEquals(j, instance.currentAsInt());
        }
        assertFalse(instance.moveNext());
    }

    /**
     * Tests if it is possible to iterate over a given array
     * using the {@link IntRangeEnumerator#moveNext()} method.
     * <p>
     * Tests methods: {@link IntRangeEnumerator#estimateSize()} ,
     * {@link IntRangeEnumerator#moveNext()},
     * {@link IntRangeEnumerator#current()},
     * {@link IntRangeEnumerator#currentAsInt()}.
     */
    @Test
    public void testMoveNext() {
        int[] a = {1, 2, 3, 4, 5};
        final IntRangeEnumerator instance = new IntRangeEnumerator(1, 6);
        assertEquals(a.length, instance.estimateSize());
        for (int j : a) {
            assertTrue(instance.moveNext());
            assertEquals(j, instance.current());
            assertEquals(j, instance.currentAsInt());
        }
        assertFalse(instance.moveNext());
    }

    /**
     * Tests if it is possible to iterate over a given array
     * using the {@link IntRangeEnumerator#moveNext()} method.
     * <p>
     * Tests methods: {@link IntRangeEnumerator#estimateSize()} ,
     * {@link IntRangeEnumerator#moveNext()},
     * {@link IntRangeEnumerator#current()},
     * {@link IntRangeEnumerator#currentAsInt()}.
     */
    @Test
    public void testMoveNextWithIntFunction() {
        int[] a = {1, 2, 3, 4, 5};
        final IntRangeEnumerator instance = new IntRangeEnumerator(i -> i + 1, 0, 5);
        assertEquals(a.length, instance.estimateSize());
        for (int j : a) {
            assertTrue(instance.moveNext());
            assertEquals(j, instance.current());
            assertEquals(j, instance.currentAsInt());
        }
        assertFalse(instance.moveNext());
    }

    /**
     * Tests if it is possible to iterate over a given array
     * using the {@link IntRangeEnumerator#tryAdvance(IntConsumer)} ()} method.
     * <p>
     * Tests methods: {@link IntRangeEnumerator#estimateSize()} ,
     * {@link IntRangeEnumerator#tryAdvance(IntConsumer)}}.
     */
    @Test
    public void testTryAdvance() {
        int[] a = {1, 2, 3, 4, 5};
        final IntRangeEnumerator instance = new IntRangeEnumerator(1, 6);
        assertEquals(a.length, instance.estimateSize());
        int[] element = new int[1];
        for (int j : a) {
            assertTrue(instance.tryAdvance((IntConsumer) e -> element[0] = e));
            assertEquals(j, element[0]);
        }
        assertFalse(instance.tryAdvance((IntConsumer) e -> element[0] = e));
    }


    /**
     * Tests if it is possible to split the iterator once.
     * <p>
     * Tests methods: {@link IntRangeEnumerator#trySplit()}.
     */
    @Test
    public void testTrySplit() {
        int[] a = {1, 2, 3, 4, 5};
        final IntRangeEnumerator instance = new IntRangeEnumerator(1, 6);
        final IntRangeEnumerator prefix = instance.trySplit();
        assertNotNull(prefix);
        assertEquals(a.length, instance.estimateSize() + prefix.estimateSize());

        int[] actual = new int[a.length];
        int i = 0;
        while (prefix.moveNext()) {
            actual[i++] = prefix.current();
        }
        while (instance.moveNext()) {
            actual[i++] = instance.current();
        }

        assertArrayEquals(a, actual);
    }

    /**
     * Tests if it is possible to split the iterator once.
     * <p>
     * Tests methods: {@link IntRangeEnumerator#trySplit()}.
     */
    @Test
    public void testTrySplitWithFunction() {
        int[] a = {1, 2, 3, 4, 5};
        final IntRangeEnumerator instance = new IntRangeEnumerator(i -> i + 1, 0, 5);
        final IntRangeEnumerator prefix = instance.trySplit();
        assertNotNull(prefix);
        assertEquals(a.length, instance.estimateSize() + prefix.estimateSize());

        int[] actual = new int[a.length];
        int i = 0;
        while (prefix.moveNext()) {
            actual[i++] = prefix.current();
        }
        while (instance.moveNext()) {
            actual[i++] = instance.current();
        }

        assertArrayEquals(a, actual);
    }

    /**
     * Tests if it is possible to split the repeated times until it
     * is too small.
     * <p>
     * Tests methods: {@link IntRangeEnumerator#trySplit()}.
     */
    @Test
    public void testTrySplitUnlessTooSmall() {
        int[] a = {1, 2, 3, 4, 5};
        final IntRangeEnumerator instance = new IntRangeEnumerator(1, 6);
        Deque<IntRangeEnumerator> stack = new ArrayDeque<>();
        stack.push(instance);

        IntRangeEnumerator it = instance.trySplit();
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
                actual[i++] = it.current();
            }
        }

        assertArrayEquals(a, actual);
    }
}