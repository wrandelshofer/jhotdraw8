/*
 * @(#)CssRadialGradientConverterTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.draw.css.value.text;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.base.converter.IdFactory;
import org.jhotdraw8.draw.css.converter.CssRadialGradientConverter;
import org.jhotdraw8.draw.css.value.CssRadialGradient;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class CssRadialGradientConverterTest {
    /**
     * Test of fromString method, of class CssPoint2DConverterTest.
     */
    public static void doTestFromString(CssRadialGradient expected, @NonNull String string) throws Exception {
        CharBuffer buf = CharBuffer.wrap(string);
        IdFactory idFactory = null;
        CssRadialGradientConverter instance = new CssRadialGradientConverter(false);
        CssRadialGradient actual = instance.fromString(buf, idFactory);
        assertEquals(expected, actual);
    }

    @TestFactory
    public @NonNull List<DynamicTest> dynamicTestsFromString() {
        return Arrays.asList(
                dynamicTest("1", () -> doTestFromString(new CssRadialGradient(), "radial-gradient()"))
        );
    }

}