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


}
