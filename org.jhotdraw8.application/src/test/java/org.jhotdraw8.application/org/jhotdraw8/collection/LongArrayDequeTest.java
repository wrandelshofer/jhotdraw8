/* @(#)LongArrayDequeTest
 *  Copyright © The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.collection;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * @author werni
 */
public class LongArrayDequeTest {

    public LongArrayDequeTest() {
    }

    /**
     * Test of addFirst method, of class LongArrayDeque.
     */
    @Test
    public void testAddFirst() {
        int e = 1;
        LongArrayDeque instance = new LongArrayDeque();
        instance.addFirstLong(e);
        assertFalse(instance.isEmpty());

        assertEquals(1, instance.getFirstLong());

        instance.addFirstLong(2);
        assertEquals(2, instance.getFirstLong());
        assertEquals(2, instance.size());
    }

    /**
     * Test of addLast method, of class LongArrayDeque.
     */
    @Test
    public void testAddLast() {
        int e = 1;
        LongArrayDeque instance = new LongArrayDeque();
        instance.addLastLong(e);
        assertFalse(instance.isEmpty());

        assertEquals(1, instance.getLastLong());

        instance.addLastLong(2);
        assertEquals(2, instance.getLastLong());
        assertEquals(2, instance.size());
    }

    @Test
    public void testAddAll() {
        LongArrayDeque instance = new LongArrayDeque(4);
        instance.addLastAll(new long[]{1, 2, 3});
        instance.addLastAll(new long[]{4, 5});
        instance.addLastAll(new long[]{0, 6, 7}, 1, 2);
        assertEquals(1, instance.removeFirstLong());
        assertEquals(2, instance.removeFirstLong());
        instance.addLastAll(new long[]{0, 8, 9, 0}, 1, 2);
        instance.addLastAll(new long[]{0, 10, 11, 0}, 1, 2);

        LongArrayDeque expected = new LongArrayDeque(0);
        expected.addLastAll(new long[]{3, 4, 5, 6, 7, 8, 9, 10, 11});
        assertEquals(expected, instance);
        assertEquals(expected.hashCode(), instance.hashCode());

        assertEquals(3, instance.removeFirstLong());
        assertEquals(4, instance.removeFirstLong());
        assertEquals(5, instance.removeFirstLong());
        assertEquals(11, instance.removeLastLong());
        assertEquals(10, instance.removeLastLong());
        instance.addLastAll(new long[]{10, 11, 12, 13, 14, 15, 16, 17, 18, 19});

        expected.clear();
        expected.addLastAll(new long[]{6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19});
        assertEquals(expected, instance);
        assertEquals(expected.hashCode(), instance.hashCode());
    }
}
