/*
 * @(#)OrderedPairTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.collection;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.collection.pair.SimpleOrderedPair;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrderedPairTest {
    @Test
    public void testNonNull() {
        SimpleOrderedPair<@NonNull Integer, @NonNull Integer> op = new SimpleOrderedPair<>(3, 5);
        int sum = op.first() + op.second();
        assertEquals(8, sum);
    }

    @Test
    public void testNullable() {
        SimpleOrderedPair<@Nullable Integer, @Nullable Integer> op = new SimpleOrderedPair<>(3, 5);
        int sum = op.first() + op.second();
        assertEquals(8, sum);
    }
}