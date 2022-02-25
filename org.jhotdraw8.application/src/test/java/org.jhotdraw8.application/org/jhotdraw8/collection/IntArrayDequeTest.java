/* @(#)IntArrayDequeTest
 *  Copyright © The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * @author werni
 */
public class IntArrayDequeTest {

    public IntArrayDequeTest() {
    }

    /**
     * Test of addFirst method, of class IntArrayDeque.
     */
    @Test
    public void testAddFirst() {
        int e = 1;
        IntArrayDeque instance = new IntArrayDeque();
        instance.addFirstInt(e);
        assertFalse(instance.isEmpty());

        assertEquals(1, instance.getFirstInt());

        instance.addFirstInt(2);
        assertEquals(2, instance.getFirstInt());
        assertEquals(2, instance.size());
    }

    /**
     * Test of addLast method, of class IntArrayDeque.
     */
    @Test
    public void testAddLast() {
        int e = 1;
        IntArrayDeque instance = new IntArrayDeque();
        instance.addLastInt(e);
        assertFalse(instance.isEmpty());

        assertEquals(1, instance.getLastInt());

        instance.addLastInt(2);
        assertEquals(2, instance.getLastInt());
        assertEquals(2, instance.size());
    }

    @Test
    public void testAddAll() {
        IntArrayDeque instance = new IntArrayDeque(4);
        instance.addLastAll(new int[]{1, 2, 3});
        instance.addLastAll(new int[]{4, 5});
        instance.addLastAll(new int[]{0, 6, 7}, 1, 2);
        assertEquals(1, instance.removeFirstInt());
        assertEquals(2, instance.removeFirstInt());
        instance.addLastAll(new int[]{0, 8, 9, 0}, 1, 2);
        instance.addLastAll(new int[]{0, 10, 11, 0}, 1, 2);

        IntArrayDeque expected = new IntArrayDeque(0);
        expected.addLastAll(new int[]{3, 4, 5, 6, 7, 8, 9, 10, 11});
        assertEquals(expected, instance);
        assertEquals(expected.hashCode(), instance.hashCode());

        assertEquals(3, instance.removeFirstInt());
        assertEquals(4, instance.removeFirstInt());
        assertEquals(5, instance.removeFirstInt());
        assertEquals(11, instance.removeLastInt());
        assertEquals(10, instance.removeLastInt());
        instance.addLastAll(new int[]{10, 11, 12, 13, 14, 15, 16, 17, 18, 19});

        expected.clear();
        expected.addLastAll(new int[]{6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19});
        assertEquals(expected, instance);
        assertEquals(expected.hashCode(), instance.hashCode());
    }
}
