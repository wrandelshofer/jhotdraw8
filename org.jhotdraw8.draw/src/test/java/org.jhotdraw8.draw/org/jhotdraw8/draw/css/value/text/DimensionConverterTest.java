/*
 * @(#)DimensionConverterTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.css.value.text;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.converter.IdFactory;
import org.jhotdraw8.base.converter.SimpleIdFactory;
import org.jhotdraw8.css.converter.CssSizeConverter;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.css.value.DefaultUnitConverter;
import org.jhotdraw8.css.value.UnitConverter;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * DimensionConverterTest.
 *
 * @author Werner Randelshofer
 * @version $$Id: CssSizeConverterNGTest_1.java 1176 2016-12-11 19:48:19Z
 * rawcoder $$
 */
public class DimensionConverterTest {

    public DimensionConverterTest() {
    }

    /**
     * Test of toString method, of class CssDoubleConverter.
     */
    public static void testToString(@Nullable Double value, String expected) throws Exception {
        StringBuilder out = new StringBuilder();
        IdFactory idFactory = null;
        CssSizeConverter instance = new CssSizeConverter(true);
        instance.toString(out, idFactory, value == null ? null : CssSize.of(value, null));
        String actual = out.toString();
        assertEquals(actual, expected);
    }

    /**
     * Test of fromString method, of class CssDoubleConverter.
     */
    public static void testFromString(@Nullable Double expected, @NonNull String string) throws Exception {
        CharBuffer buf = CharBuffer.wrap(string);
        IdFactory idFactory = new SimpleIdFactory();
        CssSizeConverter instance = new CssSizeConverter(true);
        CssSize actualSize = instance.fromString(buf, idFactory);
        UnitConverter c = new DefaultUnitConverter(90);
        Double actual = actualSize == null ? null : c.convert(actualSize, UnitConverter.DEFAULT);
        if (expected == null || Double.isNaN(expected)) {
            assertEquals(expected, actual);
        } else {
            assertEquals(expected, actual, 1e-4);
        }
    }


    @TestFactory
    public @NonNull List<DynamicTest> dynamicTestsFromString() {
        return Arrays.asList(
                dynamicTest("1", () -> testFromString(null, "none")),
                dynamicTest("2", () -> testFromString(1.0, "1")),
                dynamicTest("3", () -> testFromString(3.0e30, "3e30")),
                dynamicTest("4", () -> testFromString(Double.POSITIVE_INFINITY, "INF")),
                dynamicTest("5", () -> testFromString(Double.NEGATIVE_INFINITY, "-INF")),
                dynamicTest("6", () -> testFromString(Double.NaN, "NaN")),
                dynamicTest("7", () -> testFromString(0.01, "1%")),
                dynamicTest("8", () -> testFromString(90.0, "1in")),
                dynamicTest("9", () -> testFromString(90.0 / 2.54, "1cm")),
                dynamicTest("10", () -> testFromString(90.0 / 25.4, "1mm")),
                dynamicTest("11", () -> testFromString(12.0, "1em")),
                dynamicTest("12", () -> testFromString(8.0, "1ex")),
                dynamicTest("13", () -> testFromString(90.0 / 72, "1pt")),
                dynamicTest("14", () -> testFromString(90.0 / 72, "12pc")),
                dynamicTest("15", () -> testFromString(0.01 * 3.14, "3.14%")),
                dynamicTest("16", () -> testFromString(90.0 * 3.14, "3.14in")),
                dynamicTest("17", () -> testFromString(90.0 / 2.54 * 3.14, "3.14cm")),
                dynamicTest("18", () -> testFromString(90.0 / 25.4 * 3.14, "3.14mm")),
                dynamicTest("19", () -> testFromString(12.0 * 3.14, "3.14em")),
                dynamicTest("20", () -> testFromString(8.0 * 3.14, "3.14ex")),
                dynamicTest("21", () -> testFromString(90.0 / 72 * 3.14, "3.14pt")),
                dynamicTest("22", () -> testFromString(90.0 / 72 / 12 * 3.14, "3.14pc")),
                dynamicTest("23", () -> testFromString(12.0 * 31.4, "3.14e1em")),
                dynamicTest("24", () -> testFromString(8.0 * 31.4, "3.14e1ex")),
                dynamicTest("25", () -> testFromString(1.0, "1"))
        );
    }

    @TestFactory
    public @NonNull List<DynamicTest> dynamicTestsToString() {
        return Arrays.asList(
                dynamicTest("1", () -> testToString(null, "none")),
                dynamicTest("2", () -> testToString(1.0, "1")),
                dynamicTest("3", () -> testToString(3.0e30, "3.0E30")),
                dynamicTest("4", () -> testToString(Double.POSITIVE_INFINITY, "INF")),
                dynamicTest("5", () -> testToString(Double.NEGATIVE_INFINITY, "-INF")),
                dynamicTest("6", () -> testToString(Double.NaN, "NaN"))
        );
    }
}
