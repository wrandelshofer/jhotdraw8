/*
 * @(#)CssSymmetricCssPoint2DConverterTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.draw.css.converter;

import javafx.geometry.Point2D;
import org.jhotdraw8.base.converter.IdFactory;
import org.jhotdraw8.css.value.CssPoint2D;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.nio.CharBuffer;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * CssSymmetricCssPoint2DConverter.
 *
 */
public class CssSymmetricPoint2DCssConverterTest {

    /**
     * Test of fromString method, of class CssPoint2DConverterTest.
     */
    public static void doTestFromString(CssPoint2D expected, String string) throws Exception {
        CharBuffer buf = CharBuffer.wrap(string);
        IdFactory idFactory = null;
        SymmetricCssPoint2DCssConverter instance = new SymmetricCssPoint2DCssConverter(false);
        CssPoint2D actual = instance.fromString(buf, idFactory);
        assertEquals(expected, actual);
    }

    /**
     * Test of fromString method, of class CssPoint2DConverterTest.
     */
    public static void doTestFromIllegalString(String string) throws Exception {
        CharBuffer buf = CharBuffer.wrap(string);
        IdFactory idFactory = null;
        SymmetricPoint2DConverter instance = new SymmetricPoint2DConverter(false);
        try {
            Point2D actual = instance.fromString(buf, idFactory);
            fail();
        } catch (ParseException e) {

        }
    }

    @TestFactory
    public List<DynamicTest> dynamicTestsFromString() {
        return Arrays.asList(
                dynamicTest("1", () -> doTestFromString(new CssPoint2D(10, 20), "10 20")),
                dynamicTest("2", () -> doTestFromString(new CssPoint2D(10, 20), "10 20 ")),
                dynamicTest("3", () -> doTestFromString(new CssPoint2D(10, 20), "10, 20")),
                dynamicTest("4", () -> doTestFromString(new CssPoint2D(10, 10), "10")),
                dynamicTest("5", () -> doTestFromString(new CssPoint2D(10, 10), "10 "))
        );
    }

    @TestFactory
    public List<DynamicTest> dynamicTestsFromIllegalString() {
        return Arrays.asList(
                dynamicTest("1", () -> doTestFromIllegalString("")),
                dynamicTest("2", () -> doTestFromIllegalString(",")),
                dynamicTest("3", () -> doTestFromIllegalString("10,"))
        );
    }

}