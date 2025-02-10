/*
 * @(#)NaturalSortCollatorTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.base.text;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * OSXCollatorTest.
 *
 */
public class NaturalSortCollatorTest {

    public NaturalSortCollatorTest() {
    }

    /**
     * Test of compare method, of class OSXCollator.
     */
    @Test
    public void testExpandNumbers() {
        NaturalSortCollator instance = new NaturalSortCollator();
        String input = "a1b34";
        String expected = "a001b0134";
        String actual = instance.expandNumbers(input);
        assertEquals(actual, expected, actual + " == " + expected);
    }

}