/*
 * @(#)CssColorConverterTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.css.converter;

import javafx.scene.paint.Color;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.base.converter.IdFactory;
import org.jhotdraw8.css.parser.CssTokenType;
import org.jhotdraw8.draw.css.value.CssColor;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * CssColorConverterTest.
 *
 * @author Werner Randelshofer
 */
public class CssColorConverterTest {

    public CssColorConverterTest() {
    }

    /**
     * Test of fromString method, of class CssColorConverter.
     */
    static void testFromString(@NonNull CssColor expected, @NonNull String string) throws Exception {
        CharBuffer buf = CharBuffer.wrap(string);
        IdFactory idFactory = null;
        Converter<CssColor> instance = new CssColorConverter(true);
        CssColor actual = instance.fromString(buf, idFactory);
        assertEquals(expected, actual);
        if (actual != null) {
            assertEquals(actual.getName(), expected.getName());
        }
    }

    @TestFactory
    public @NonNull List<DynamicTest> dynamicTestsFromString() {
        return Arrays.asList(
                dynamicTest("1", () -> testFromString(null, CssTokenType.IDENT_NONE)),
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
                dynamicTest("13", () -> testFromString(new CssColor("hsba(10,20%,30%,0.8)", Color.hsb(10, 0.20, 0.30, 0.8)), "hsba(10,20%,30%,0.8)")),
                dynamicTest("opacity 0.2 cannot be represented with a float", () -> testFromString(new CssColor("rgba(0%,0%,0%,0.2)", Color.rgb(0, 0, 0, 0.2)), "rgba(0%,0%,0%,0.2)"))
        );
    }
}
