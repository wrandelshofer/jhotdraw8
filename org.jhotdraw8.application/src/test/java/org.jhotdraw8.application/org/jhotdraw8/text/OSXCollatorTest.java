/* @(#)OSXCollatorTest.java
 * Copyright (c) 2017 The authors and contributors of JHotDraw.
 * You may only use this file in compliance with the accompanying license terms.
 */

package org.jhotdraw8.text;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * OSXCollatorTest.
 *
 * @author Werner Randelshofer
 */
public class OSXCollatorTest {

    public OSXCollatorTest() {
    }

    /**
     * Test of compare method, of class OSXCollator.
     */
    @Test
    public void testExpandNumbers() {
        OSXCollator instance = new OSXCollator();
        String input = "a1b34";
        String expected = "a001b0134";
        String actual = instance.expandNumbers(input);
        assertEquals(actual, expected, actual + " == " + expected);
    }

}