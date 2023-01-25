/*
 * @(#)CssPoint3DConverterTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.draw.css.value.text;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.base.converter.IdFactory;
import org.jhotdraw8.draw.css.converter.CssPoint3DConverter;
import org.jhotdraw8.draw.css.value.CssPoint3D;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class CssPoint3DConverterTest {
    /**
     * Test of fromString method, of class CssPoint3DConverter.
     */
    public static void doTestFromString(CssPoint3D expected, @NonNull String string) throws Exception {
        CharBuffer buf = CharBuffer.wrap(string);
        IdFactory idFactory = null;
        CssPoint3DConverter instance = new CssPoint3DConverter(false);
        CssPoint3D actual = instance.fromString(buf, idFactory);
        assertEquals(actual, expected);
    }

    /**
     * Test of toString method, of class CssPoint3DConverter.
     */
    public static void doTestToString(CssPoint3D value, String expected) throws Exception {
        CssPoint3DConverter instance = new CssPoint3DConverter(false);
        String actual = instance.toString(value);
        assertEquals(expected, actual);
    }

    /**
     * Test of fromString and toString methods, of class CssPoint3DConverter.
     */
    public static void doTest(CssPoint3D value, @NonNull String str) throws Exception {
        doTestFromString(value, str);
        doTestToString(value, str);
    }

    @TestFactory
    public @NonNull List<DynamicTest> dynamicTestsFromString() {
        return Arrays.asList(
                dynamicTest("1", () -> doTest(new CssPoint3D(1, 2, 3), "1 2 3")),
                dynamicTest("1", () -> doTest(new CssPoint3D(1, 2, 0), "1 2"))
        );
    }

}