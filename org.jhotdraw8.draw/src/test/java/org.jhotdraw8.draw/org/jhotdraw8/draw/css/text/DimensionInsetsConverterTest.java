/*
 * @(#)DimensionInsetsConverterTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.draw.css.text;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.base.converter.IdFactory;
import org.jhotdraw8.draw.css.CssInsets;
import org.jhotdraw8.draw.css.converter.CssInsetsConverter;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class DimensionInsetsConverterTest {

    /**
     * Test of fromString method, of class CssPoint2DConverterTest.
     */
    public static void doTestFromString(CssInsets expected, @NonNull String string) throws Exception {
        CharBuffer buf = CharBuffer.wrap(string);
        IdFactory idFactory = null;
        CssInsetsConverter instance = new CssInsetsConverter(false);
        CssInsets actual = instance.fromString(buf, idFactory);
        assertEquals(actual, expected);
    }

    @TestFactory
    public @NonNull List<DynamicTest> dynamicTestsFromString() {
        return Arrays.asList(
                dynamicTest("1", () -> doTestFromString(new CssInsets(10, 20, 30, 40, "mm"), "10mm 20mm 30mm 40mm")),
                dynamicTest("2", () -> doTestFromString(new CssInsets(10, 10, 20, 40, "mm"), "10mm 10mm 20mm 40mm")),
                dynamicTest("3", () -> doTestFromString(new CssInsets(10, 10, 10, 40, "mm"), "10mm 10mm 10mm 40mm")),
                dynamicTest("4", () -> doTestFromString(new CssInsets(10, 20, 10, 20, "mm"), "10mm 20mm 10mm 20mm"))
        );
    }

}