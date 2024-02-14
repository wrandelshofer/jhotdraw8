/*
 * @(#)CssRadialGradientConverterTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.draw.css.converter;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.base.converter.IdFactory;
import org.jhotdraw8.draw.css.value.CssRadialGradient;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class RadialGradientCssConverterTest {
    /**
     * Test of fromString method, of class CssPoint2DConverterTest.
     */
    public static void doTestFromString(CssRadialGradient expected, @NonNull String string) throws Exception {
        CharBuffer buf = CharBuffer.wrap(string);
        IdFactory idFactory = null;
        RadialGradientCssConverter instance = new RadialGradientCssConverter(false);
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