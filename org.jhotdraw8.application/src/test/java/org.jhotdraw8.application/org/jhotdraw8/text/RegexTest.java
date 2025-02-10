/*
 * @(#)RegexTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.text;

import org.jhotdraw8.base.text.RegexReplace;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * RegexTest.
 *
 */
public class RegexTest {

    public RegexTest() {
    }

    /**
     * Test of toString method, of class RegexReplace.
     */
    @Test
    public void testToString() {
        RegexReplace instance = new RegexReplace();
        String expResult = "///";
        String result = instance.toString();
        assertEquals(result, expResult);
        // TODO review the generated test code and remove the default call to fail.
    }
}