/*
 * @(#)CssFontConverterTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.draw.css.converter;

import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import org.jhotdraw8.base.converter.IdFactory;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.css.value.CssFont;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public class FontCssConverterTest {
    /**
     * Test of fromString method, of class CssFontConverter.
     */
    public static void doTestFromString(CssFont expected, String string) throws Exception {
        CharBuffer buf = CharBuffer.wrap(string);
        IdFactory idFactory = null;
        FontCssConverter instance = new FontCssConverter(false);
        CssFont actual = instance.fromString(buf, idFactory);
        assertEquals(expected, actual);
    }

    /**
     * Test of toString method, of class CssFontConverter.
     */
    public static void doTestToString(CssFont value, String expected) throws Exception {
        FontCssConverter instance = new FontCssConverter(false);
        String actual = instance.toString(value);
        assertEquals(expected, actual);
    }

    /**
     * Test of fromString and toString methods, of class CssFontConverter.
     */
    public static void testFont(CssFont value, String str) throws Exception {
        doTestFromString(value, str);
        doTestToString(value, str);
    }

    @TestFactory
    public List<DynamicTest> dynamicTestsFont() {
        return Arrays.asList(
                dynamicTest("1", () -> testFont(
                        new CssFont("Arial", FontWeight.NORMAL, FontPosture.REGULAR, CssSize.of(12)),
                        "12 Arial")),
                dynamicTest("2", () -> testFont(
                        new CssFont("Arial", FontWeight.NORMAL, FontPosture.REGULAR, CssSize.of(12, "pt")),
                        "12pt Arial")),
                dynamicTest("3", () -> testFont(
                        new CssFont("Arial", FontWeight.SEMI_BOLD, FontPosture.REGULAR, CssSize.of(12, "pt")),
                        "600 12pt Arial"))
        );
    }

}