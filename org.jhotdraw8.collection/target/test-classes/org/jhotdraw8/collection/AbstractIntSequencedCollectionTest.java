/*
 * @(#)AbstractIntSequencedCollectionTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;


import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.collection.primitive.IntSequencedCollection;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Tests classes that implement the interface {@link IntSequencedCollection}.
 */

public abstract class AbstractIntSequencedCollectionTest {

    public AbstractIntSequencedCollectionTest() {
    }

    /**
     * Test of addFirst method, of class IntSequencedCollection.
     */
    @Test
    public void testAddFirst() {
        int e = 1;
        IntSequencedCollection instance = newInstance();
        instance.addFirstAsInt(e);
        assertFalse(instance.isEmpty());

        assertEquals(1, instance.getFirstAsInt());

        instance.addFirstAsInt(2);
        assertEquals(2, instance.getFirstAsInt());
        assertEquals(2, instance.size());
    }

    @NonNull
    protected abstract IntSequencedCollection newInstance();

    /**
     * Test of addLast method, of class IntSequencedCollection.
     */
    @Test
    public void testAddLast() {
        int e = 1;
        IntSequencedCollection instance = newInstance();
        instance.addLastAsInt(e);
        assertFalse(instance.isEmpty());

        assertEquals(1, instance.getLastAsInt());

        instance.addLastAsInt(2);
        assertEquals(2, instance.getLastAsInt());
        assertEquals(2, instance.size());
    }

    @Test
    public void testAddAll() {
        IntSequencedCollection instance = newInstance();
        instance.addLastAllAsInt(new int[]{1, 2, 3});
        instance.addLastAllAsInt(new int[]{4, 5});
        instance.addLastAllAsInt(new int[]{0, 6, 7}, 1, 2);
        assertEquals(1, instance.removeFirstAsInt());
        assertEquals(2, instance.removeFirstAsInt());
        instance.addLastAllAsInt(new int[]{0, 8, 9, 0}, 1, 2);
        instance.addLastAllAsInt(new int[]{0, 10, 11, 0}, 1, 2);

        IntSequencedCollection expected = newInstance();
        expected.addLastAllAsInt(new int[]{3, 4, 5, 6, 7, 8, 9, 10, 11});
        assertEquals(expected, instance);
        assertEquals(expected.hashCode(), instance.hashCode());

        assertEquals(3, instance.removeFirstAsInt());
        assertEquals(4, instance.removeFirstAsInt());
        assertEquals(5, instance.removeFirstAsInt());
        assertEquals(11, instance.removeLastAsInt());
        assertEquals(10, instance.removeLastAsInt());
        instance.addLastAllAsInt(new int[]{10, 11, 12, 13, 14, 15, 16, 17, 18, 19});

        expected.clear();
        expected.addLastAllAsInt(new int[]{6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19});
        assertEquals(expected, instance);
        assertEquals(expected.hashCode(), instance.hashCode());
    }
}
