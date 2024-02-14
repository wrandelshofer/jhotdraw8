/*
 * @(#)CssPoint2DConverterTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.draw.css.converter;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.base.converter.IdFactory;
import org.jhotdraw8.draw.css.value.CssPoint2D;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class Point2DCssConverterTest {

    /**
     * Test of fromString method, of class CssPoint2DConverter.
     */
    public static void doTestFromString(CssPoint2D expected, @NonNull String string) throws Exception {
        CharBuffer buf = CharBuffer.wrap(string);
        IdFactory idFactory = null;
        Point2DCssConverter instance = new Point2DCssConverter(false);
        CssPoint2D actual = instance.fromString(buf, idFactory);
        assertEquals(actual, expected);
    }

    /**
     * Test of toString method, of class CssPoint2DConverter.
     */
    public static void doTestToString(CssPoint2D value, String expected) throws Exception {
        Point2DCssConverter instance = new Point2DCssConverter(false);
        String actual = instance.toString(value);
        assertEquals(expected, actual);
    }

    /**
     * Test of fromString and toString methods, of class CssPoint2DConverter.
     */
    public static void doTest(CssPoint2D value, @NonNull String str) throws Exception {
        doTestFromString(value, str);
        doTestToString(value, str);
    }

    @TestFactory
    public @NonNull List<DynamicTest> dynamicTestsFromString() {
        return Arrays.asList(
                dynamicTest("1", () -> doTest(new CssPoint2D(40, 40, "cm"), "40cm 40cm"))
        );
    }
}