/*
 * @(#)CssPaintConverterTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.draw.css.converter;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.converter.IdFactory;
import org.jhotdraw8.draw.css.value.CssColor;
import org.jhotdraw8.draw.css.value.Paintable;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * CssPaintConverterTest.
 *
 * @author Werner Randelshofer
 */
public class PaintCssConverterTest {

    public PaintCssConverterTest() {
    }

    /**
     * Test of fromString method, of class CssPaintConverter.
     */
    public static void testFromString(@Nullable Paintable expected, @NonNull String string) throws Exception {
        CharBuffer buf = CharBuffer.wrap(string);
        IdFactory idFactory = null;
        PaintCssConverter instance = new PaintCssConverter(true);
        Paint actual = instance.fromString(buf, idFactory);

        if (expected != null
                && expected.getPaint() instanceof Color c
                && actual instanceof Color a) {
            var expectedRgb = new float[3];
            expectedRgb[0] = (float) c.getRed();
            expectedRgb[1] = (float) c.getGreen();
            expectedRgb[2] = (float) c.getBlue();
            var actualRgb = new float[3];
            actualRgb[0] = (float) a.getRed();
            actualRgb[1] = (float) a.getGreen();
            actualRgb[2] = (float) a.getBlue();
            assertArrayEquals(expectedRgb, actualRgb, 0x1p-8f, "input: " + string + " expected: " + expected);
        } else {
            assertEquals(actual, expected == null ? null : expected.getPaint(), "input: " + string + " expected: " + expected);
        }
    }

    @TestFactory
    public @NonNull List<DynamicTest> dynamicTestsFromString() {
        return Arrays.asList(
                dynamicTest("1", () -> testFromString(null, "none")),
                dynamicTest("2", () -> testFromString(new CssColor("white", Color.WHITE), "white")),
                dynamicTest("3", () -> testFromString(new CssColor("#abc", Color.web("#abc")), "#abc")),
                dynamicTest("4", () -> testFromString(new CssColor("#abcdef", Color.web("#abcdef")), "#abcdef")),
                dynamicTest("5", () -> testFromString(new CssColor("#660033", Color.web("#660033")), "#660033")),
                dynamicTest("6", () -> testFromString(new CssColor("rgb(10,20,30)", Color.rgb(10, 20, 30, 1.0)), "rgb(10,20,30)")),
                dynamicTest("7", () -> testFromString(new CssColor("rgb(10%,20%,30%)", Color.color(0.1, 0.2, 0.3, 1.0)), "rgb(10%,20%,30%)")),
                dynamicTest("8", () -> testFromString(new CssColor("rgba(10%,20%,30%,80%)", Color.color(0.1, 0.2, 0.3, 0.8)), "rgba(10%,20%,30%,80%)")),
                dynamicTest("9", () -> testFromString(new CssColor("rgba(10%,20%,30%,0.8)", Color.color(0.1, 0.2, 0.3, 0.8)), "rgba(10%,20%,30%,0.8)")),
                dynamicTest("10", () -> testFromString(new CssColor("hsb(10,0.2,0.3)", Color.hsb(10, 0.20, 0.30)), "hsb(10,.20,.30)")),
                dynamicTest("11", () -> testFromString(new CssColor("hsb(10,20%,30%)", Color.hsb(10, 0.20, 0.30)), "hsb(10,20%,30%)")),
                dynamicTest("12", () -> testFromString(new CssColor("hsba(10,0.2,0.3,80%)", Color.hsb(10, 0.20, 0.30, 0.8)), "hsba(10,.2,.3,80%)")),
                dynamicTest("13", () -> testFromString(new CssColor("hsba(10,20%,30%,0.8)", Color.hsb(10, 0.20, 0.30, 0.8)), "hsba(10,20%,30%,0.8)"))
        );

    }

}