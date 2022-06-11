/*
 * @(#)LongMinHeapTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LongArrayHeapTest {

    @Test
    public void test() {

        // Creating object opf class in main() method
        final LongArrayHeap minHeap = new LongArrayHeap(15);

        // Inserting element to minHeap
        // using insert() method

        // Custom input entries
        minHeap.addAsLong(5);
        minHeap.addAsLong(3);
        minHeap.addAsLong(17);
        minHeap.addAsLong(10);
        minHeap.addAsLong(84);
        minHeap.addAsLong(19);
        minHeap.addAsLong(6);
        minHeap.addAsLong(22);
        minHeap.addAsLong(9);

        // Print all elements of the heap
        // minHeap.print();

        // Removing minimum value from above heap
        long removed = minHeap.removeAsLong();
        assertEquals(3, removed);
    }
}