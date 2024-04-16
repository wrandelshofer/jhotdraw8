/*
 * @(#)CssStrokeConverterTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.draw.css.converter;

import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.shape.StrokeType;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.base.converter.IdFactory;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.draw.css.value.CssStrokeStyle;
import org.jhotdraw8.icollection.VectorList;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * CssStrokeConverterTest.
 *
 * @author Werner Randelshofer
 */
public class CssStrokeConverterTest {

    /**
     * Test of fromString method, of class CssStrokeStyleConverter.
     */
    public static void doTestFromString(CssStrokeStyle expected, @NonNull String string) throws Exception {
        CharBuffer buf = CharBuffer.wrap(string);
        IdFactory idFactory = null;
        StrokeStyleCssConverter instance = new StrokeStyleCssConverter(false);
        CssStrokeStyle actual = instance.fromString(buf, idFactory);
        assertEquals(expected, actual);
    }

    /**
     * Test of toString method, of class CssStrokeStyleConverter.
     */
    public static void doTestToString(CssStrokeStyle value, String expected) throws Exception {
        StrokeStyleCssConverter instance = new StrokeStyleCssConverter(false);
        String actual = instance.toString(value);
        assertEquals(expected, actual);
    }

    /**
     * Test of fromString and toString methods, of class CssStrokeStyleConverter.
     */
    public static void testStrokeStyle(CssStrokeStyle value, @NonNull String str) throws Exception {
        doTestFromString(value, str);
        doTestToString(value, str);
    }


    @TestFactory
    public @NonNull List<DynamicTest> dynamicTestsStrokeStyle() {
        return Arrays.asList(
                dynamicTest("1", () -> testStrokeStyle(
                        new CssStrokeStyle(),
                        "type(centered) linecap(butt) linejoin(miter) miterlimit(4) dashoffset(0) dasharray()")),
                dynamicTest("2", () -> testStrokeStyle(
                        new CssStrokeStyle(),
                        "type(centered) linecap(butt) linejoin(miter) miterlimit(4) dashoffset(0) dasharray()")),
                dynamicTest("3", () -> testStrokeStyle(
                        new CssStrokeStyle(StrokeType.CENTERED, StrokeLineCap.ROUND, StrokeLineJoin.MITER, CssSize.of(3)
                                , CssSize.of(4), VectorList.of(CssSize.of(5), CssSize.of(6))),
                        "type(centered) linecap(round) linejoin(miter) miterlimit(3) dashoffset(4) dasharray(5 6)")),
                dynamicTest("4", () -> testStrokeStyle(
                        new CssStrokeStyle(StrokeType.CENTERED, StrokeLineCap.BUTT, StrokeLineJoin.MITER, CssSize.of(3)
                                , CssSize.of(4), VectorList.of(CssSize.of(5), CssSize.of(6))),
                        "type(centered) linecap(butt) linejoin(miter) miterlimit(3) dashoffset(4) dasharray(5 6)")),
                dynamicTest("5", () -> testStrokeStyle(
                        new CssStrokeStyle(StrokeType.INSIDE, StrokeLineCap.ROUND, StrokeLineJoin.MITER, CssSize.of(3)
                                , CssSize.of(4), VectorList.of(CssSize.of(5), CssSize.of(6))),
                        "type(inside) linecap(round) linejoin(miter) miterlimit(3) dashoffset(4) dasharray(5 6)")),
                dynamicTest("6", () -> testStrokeStyle(
                        new CssStrokeStyle(StrokeType.CENTERED, StrokeLineCap.BUTT, StrokeLineJoin.MITER, CssSize.of(4)
                                , CssSize.of(0), VectorList.of()),
                        "type(centered) linecap(butt) linejoin(miter) miterlimit(4) dashoffset(0) dasharray()"))
        );
    }

}