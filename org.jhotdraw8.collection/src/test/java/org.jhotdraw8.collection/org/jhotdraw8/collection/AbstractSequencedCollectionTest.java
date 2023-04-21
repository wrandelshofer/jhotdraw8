/*
 * @(#)AbstractIntSequencedCollectionTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;


import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.sequenced.SequencedCollection;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Tests classes that implement the interface {@link SequencedCollection<Integer>}.
 */

public abstract class AbstractSequencedCollectionTest {

    public AbstractSequencedCollectionTest() {
    }

    /**
     * Test of addFirst method, of class SequencedCollection<Integer>.
     */
    @Test
    public void shouldAddFirst() {
        int e = 1;
        SequencedCollection<Integer> instance = newInstance();
        instance.addFirst(e);
        assertFalse(instance.isEmpty());

        assertEquals(1, instance.getFirst());

        instance.addFirst(2);
        assertEquals(2, instance.getFirst());
        assertEquals(2, instance.size());
    }

    @NonNull
    protected abstract SequencedCollection<Integer> newInstance();

    /**
     * Test of addLast method, of class SequencedCollection<Integer>.
     */
    @Test
    public void shouldAddLast() {
        int e = 1;
        SequencedCollection<Integer> instance = newInstance();
        instance.addLast(e);
        assertFalse(instance.isEmpty());

        assertEquals(1, instance.getLast());

        instance.addLast(2);
        assertEquals(2, instance.getLast());
        assertEquals(2, instance.size());
    }

    @Test
    public void shouldAddAll() {
        SequencedCollection<Integer> instance = newInstance();
        instance.addAll(Arrays.<Integer>asList(1, 2, 3));
        instance.addAll(Arrays.<Integer>asList(4, 5));
        instance.addAll(Arrays.<Integer>asList(6, 7));
        assertEquals(1, instance.removeFirst());
        assertEquals(2, instance.removeFirst());
        instance.addAll(Arrays.<Integer>asList(8, 9));
        instance.addAll(Arrays.<Integer>asList(10, 11));

        SequencedCollection<Integer> expected = newInstance();
        expected.addAll(Arrays.<Integer>asList(3, 4, 5, 6, 7, 8, 9, 10, 11));
        assertEquals(expected, instance);
        assertEquals(expected.hashCode(), instance.hashCode());

        assertEquals(3, instance.removeFirst());
        assertEquals(4, instance.removeFirst());
        assertEquals(5, instance.removeFirst());
        assertEquals(11, instance.removeLast());
        assertEquals(10, instance.removeLast());
        instance.addAll(Arrays.<Integer>asList(10, 11, 12, 13, 14, 15, 16, 17, 18, 19));

        expected.clear();
        expected.addAll(Arrays.<Integer>asList(6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19));
        assertEquals(expected, instance);
        assertEquals(expected.hashCode(), instance.hashCode());
    }
}
