/*
 * @(#)CssListConverterTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.draw.css.converter;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.converter.IdFactory;
import org.jhotdraw8.css.converter.DoubleCssConverter;
import org.jhotdraw8.css.converter.ListCssConverter;
import org.jhotdraw8.css.converter.StringCssConverter;
import org.jhotdraw8.icollection.VectorList;
import org.jhotdraw8.icollection.immutable.ImmutableList;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * CssListConverterTest.
 *
 * @author Werner Randelshofer
 */
public class ListCssConverterTest {

    public ListCssConverterTest() {
    }

    /**
     * Test of toString method.
     */
    public void testToString(@Nullable List<Double> value, String expected) throws Exception {
        StringBuilder out = new StringBuilder();
        IdFactory idFactory = null;
        ListCssConverter<Double> instance = new ListCssConverter<>(new DoubleCssConverter(false), null);
        instance.toString(out, idFactory, value == null ? null : VectorList.copyOf(value));
        String actual = out.toString();
        assertEquals(expected, actual);
    }

    /**
     * Test of fromString method with a {@code Double} element type.
     */
    public void testDoubleFromString(List<Double> expected, @NonNull String string) throws Exception {
        CharBuffer buf = CharBuffer.wrap(string);
        IdFactory idFactory = null;
        ListCssConverter<Double> instance = new ListCssConverter<>(new DoubleCssConverter(false));
        ImmutableList<Double> actual = instance.fromString(buf, idFactory);
        assertEquals(expected, actual.toMutable());
    }

    /**
     * Test of fromString method with a {@code Double} element type and "=>" delimiter.
     */
    public void testDoubleArrowFromString(List<Double> expected, @NonNull String string) throws Exception {
        CharBuffer buf = CharBuffer.wrap(string);
        IdFactory idFactory = null;
        ListCssConverter<Double> instance = new ListCssConverter<>(new DoubleCssConverter(false), "=>");
        ImmutableList<Double> actual = instance.fromString(buf, idFactory);
        assertEquals(expected, actual.toMutable());
    }


    /**
     * Test of fromString method with a {@code String} element type.
     */
    public void testStringFromString(List<String> expected, @NonNull String string) throws Exception {
        CharBuffer buf = CharBuffer.wrap(string);
        IdFactory idFactory = null;
        ListCssConverter<String> instance = new ListCssConverter<>(new StringCssConverter(false));
        ImmutableList<String> actual = instance.fromString(buf, idFactory);
        assertEquals(expected, actual.toMutable());
    }

    @TestFactory
    public @NonNull List<DynamicTest> dynamicTestsDoubleFromString() {
        return Arrays.asList(
                dynamicTest("1", () -> testDoubleFromString(Collections.emptyList(), "none")),
                dynamicTest("2", () -> testDoubleFromString(Arrays.asList(1.0, 2.0, 3.0), "1 2 3")),
                dynamicTest("3", () -> testDoubleFromString(Arrays.asList(1.0, 3.0e30, 3.0), "1 3e30 3")),
                dynamicTest("4", () -> testDoubleFromString(Arrays.asList(1.0, 2.0, Double.POSITIVE_INFINITY), "1 2 INF")),
                dynamicTest("5", () -> testDoubleFromString(Arrays.asList(1.0, Double.NEGATIVE_INFINITY, 3.0), "1 -INF 3")),
                dynamicTest("6", () -> testDoubleFromString(Arrays.asList(1.0, Double.NaN, 3.0), "1 NaN 3")),
                //
                dynamicTest("12", () -> testDoubleFromString(Arrays.asList(1.0, 2.0, 3.0), "1, 2, 3")),
                //
                // should stop at semicolon and at right brackets:
                dynamicTest("21", () -> testDoubleFromString(Arrays.asList(1.0, 2.0, 3.0), "1, 2, 3; 4")),
                dynamicTest("22", () -> testDoubleFromString(Arrays.asList(1.0, 2.0, 3.0), "1, 2, 3) 4")),
                dynamicTest("23", () -> testDoubleFromString(Arrays.asList(1.0, 2.0, 3.0), "1, 2, 3} 4")),
                dynamicTest("24", () -> testDoubleFromString(Arrays.asList(1.0, 2.0, 3.0), "1, 2, 3] 4"))
        );
    }

    @TestFactory
    public @NonNull List<DynamicTest> dynamicTestsDoubleArrowFromString() {
        return Arrays.asList(
                dynamicTest("1", () -> testDoubleArrowFromString(Collections.emptyList(), "none")),
                dynamicTest("2", () -> testDoubleArrowFromString(Arrays.asList(1.0, 2.0, 3.0), "1 2 3")),
                dynamicTest("3", () -> testDoubleArrowFromString(Arrays.asList(1.0, 3.0e30, 3.0), "1 3e30 3")),
                dynamicTest("4", () -> testDoubleArrowFromString(Arrays.asList(1.0, 2.0, Double.POSITIVE_INFINITY), "1 2 INF")),
                dynamicTest("5", () -> testDoubleArrowFromString(Arrays.asList(1.0, Double.NEGATIVE_INFINITY, 3.0), "1 -INF 3")),
                dynamicTest("6", () -> testDoubleArrowFromString(Arrays.asList(1.0, Double.NaN, 3.0), "1 NaN 3")),
                //
                dynamicTest("12", () -> testDoubleArrowFromString(Arrays.asList(1.0, 2.0, 3.0), "1 => 2 => 3")),
                //
                // should stop at semicolon and at right brackets:
                dynamicTest("21", () -> testDoubleArrowFromString(Arrays.asList(1.0, 2.0, 3.0), "1=>2=>3; 4")),
                dynamicTest("22", () -> testDoubleArrowFromString(Arrays.asList(1.0, 2.0, 3.0), "1=> 2=> 3) 4")),
                dynamicTest("23", () -> testDoubleArrowFromString(Arrays.asList(1.0, 2.0, 3.0), "1=> 2 => 3} 4")),
                dynamicTest("24", () -> testDoubleArrowFromString(Arrays.asList(1.0, 2.0, 3.0), "1=> 2=> 3] 4"))
        );
    }

    @TestFactory
    public @NonNull List<DynamicTest> dynamicTestsStringFromString() {
        return Arrays.asList(
                dynamicTest("1", () -> testStringFromString(Collections.emptyList(), "none")),
                dynamicTest("2", () -> testStringFromString(Arrays.asList("a", "b", "c"), "'a' 'b' 'c'")),
                dynamicTest("3", () -> testStringFromString(Arrays.asList("a", "b", "c"), "'a''b''c'")),
                dynamicTest("4", () -> testStringFromString(Arrays.asList("a", "b", "c"), "'a','b','c'")),
                dynamicTest("5", () -> testStringFromString(Arrays.asList("a", "b", "c"), "'a', 'b', 'c'")),
                dynamicTest("5", () -> testStringFromString(Arrays.asList("a", "b", "c"), "'a',,'b',,'c'"))
        );
    }

    @TestFactory
    public @NonNull List<DynamicTest> dynamicTestsToString() {
        return Arrays.asList(
                dynamicTest("1", () -> testToString(null, "none")),
                dynamicTest("2", () -> testToString(Collections.emptyList(), "none")),
                dynamicTest("3", () -> testToString(Arrays.asList(1.0, 2.0, 3.0), "1 2 3")),
                dynamicTest("4", () -> testToString(Arrays.asList(1.0, 3.0e30, 3.0), "1 3.0E30 3")),
                dynamicTest("5", () -> testToString(Arrays.asList(1.0, 2.0, Double.POSITIVE_INFINITY), "1 2 INF")),
                dynamicTest("6", () -> testToString(Arrays.asList(1.0, Double.NEGATIVE_INFINITY, 3.0), "1 -INF 3")),
                dynamicTest("7", () -> testToString(Arrays.asList(1.0, Double.NaN, 3.0), "1 NaN 3"))
        );
    }
}