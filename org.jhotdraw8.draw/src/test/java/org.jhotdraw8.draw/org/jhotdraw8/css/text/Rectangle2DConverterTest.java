/* @(#)CssRectangle2DConverterTest.java
 * Copyright (c) 2016 The authors and contributors of JHotDraw.
 * You may only use this file in compliance with the accompanying license terms.
 */

package org.jhotdraw8.css.text;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.css.CssRectangle2D;
import org.jhotdraw8.io.IdFactory;
import org.jhotdraw8.io.SimpleIdFactory;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * CssRectangle2DConverterTest.
 *
 * @author Werner Randelshofer
 */
public class Rectangle2DConverterTest {

    public Rectangle2DConverterTest() {
    }


    /**
     * Test of fromString method, of class CssDoubleConverter.
     */
    public static void testFromString(CssRectangle2D expected, @NonNull String string) throws Exception {
        CharBuffer buf = CharBuffer.wrap(string);
        IdFactory idFactory = new SimpleIdFactory();
        CssRectangle2DConverter instance = new CssRectangle2DConverter(false);
        CssRectangle2D actual = instance.fromString(buf, idFactory);
        String actualString = instance.toString(expected);
        assertEquals(expected, actual);
        assertEquals(string, actualString);
    }

    @TestFactory
    public @NonNull List<DynamicTest> dynamicTestsFromString() {
        return Arrays.asList(
                dynamicTest("1", () -> testFromString(new CssRectangle2D(11, 22, 33, 44), "11 22 33 44"))
        );
    }
}