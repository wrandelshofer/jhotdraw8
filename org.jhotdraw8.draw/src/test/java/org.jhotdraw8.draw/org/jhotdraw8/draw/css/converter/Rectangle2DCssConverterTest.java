/*
 * @(#)CssRectangle2DConverterTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.draw.css.converter;

import org.jhotdraw8.base.converter.IdFactory;
import org.jhotdraw8.base.converter.SimpleIdFactory;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.draw.css.value.CssRectangle2D;
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
public class Rectangle2DCssConverterTest {

    public Rectangle2DCssConverterTest() {
    }


    /**
     * Test of fromString method, of class CssDoubleConverter.
     */
    public static void testFromString(CssRectangle2D expected, String string) throws Exception {
        CharBuffer buf = CharBuffer.wrap(string);
        IdFactory idFactory = new SimpleIdFactory();
        Rectangle2DCssConverter instance = new Rectangle2DCssConverter(false);
        CssRectangle2D actual = instance.fromString(buf, idFactory);
        String actualString = instance.toString(expected);
        assertEquals(expected, actual);
        assertEquals(string, actualString);
    }

    @TestFactory
    public List<DynamicTest> dynamicTestsFromString() {
        return Arrays.asList(
                dynamicTest("1", () -> testFromString(new CssRectangle2D(11, 22, 33, 44), "11 22 33 44")),
                dynamicTest("1", () -> testFromString(new CssRectangle2D(CssSize.of(11, "cm"),
                        CssSize.of(22, "cm"),
                        CssSize.of(33, "cm"),
                        CssSize.of(44, "cm")), "11cm 22cm 33cm 44cm"))
        );
    }
}