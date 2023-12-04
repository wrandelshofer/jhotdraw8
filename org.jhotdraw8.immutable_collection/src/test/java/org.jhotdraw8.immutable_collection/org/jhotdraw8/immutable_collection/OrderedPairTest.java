/*
 * @(#)OrderedPairTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.immutable_collection;

import org.jhotdraw8.immutable_collection.impl.champ.OrderedPair;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrderedPairTest {
    @Test
    public void testNonNull() {
        OrderedPair<Integer, Integer> op = new OrderedPair<>(3, 5);
        int sum = op.first() + op.second();
        assertEquals(8, sum);
    }

    @Test
    public void testNullable() {
        OrderedPair<Integer, Integer> op = new OrderedPair<>(3, 5);
        int sum = op.first() + op.second();
        assertEquals(8, sum);
    }
}