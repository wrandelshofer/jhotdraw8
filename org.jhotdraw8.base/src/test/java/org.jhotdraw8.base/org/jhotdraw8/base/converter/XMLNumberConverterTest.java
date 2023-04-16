/*
 * @(#)XMLNumberConverterTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jhotdraw8.base.converter;

import org.jhotdraw8.annotation.NonNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

/**
 * @author werni
 */
public class XMLNumberConverterTest {

    /**
     * Test of toString method, of class XmlNumberConverter.
     */
    public static void testToString(Double inputValue, String expectedValue) {
        XmlNumberConverter c = new XmlNumberConverter();

        String actualValue = c.toString(inputValue);

        Assertions.assertEquals(expectedValue, actualValue);
    }

    /**
     * Test of toString method, of class XmlNumberConverter.
     */
    public static void testFromString(Double expectedValue, @NonNull String inputValue) throws ParseException, IOException {
        XmlNumberConverter c = new XmlNumberConverter();

        Number actualValue = c.fromString(inputValue);

        Assertions.assertEquals(expectedValue, actualValue);
    }

    public static void testToFromString(Double doubleValue, @NonNull String stringValue) throws Exception {
        testToString(doubleValue, stringValue);
        testFromString(doubleValue, stringValue);
    }

    @TestFactory
    public @NonNull List<DynamicTest> dynamicTestsFromString() {
        return Arrays.asList(
                DynamicTest.dynamicTest("1", () -> testToFromString(-0.0, "-0")),
                DynamicTest.dynamicTest("2", () -> testToFromString(0.0, "0")),
                DynamicTest.dynamicTest("3", () -> testToFromString(1.0, "1")),
                DynamicTest.dynamicTest("4", () -> testToFromString(12.0, "12")),
                DynamicTest.dynamicTest("5", () -> testToFromString(123.0, "123")),
                DynamicTest.dynamicTest("6", () -> testToFromString(1234.0, "1234")),
                DynamicTest.dynamicTest("7", () -> testToFromString(12345.0, "12345")),
                DynamicTest.dynamicTest("8", () -> testToFromString(123456.0, "123456")),
                DynamicTest.dynamicTest("9", () -> testToFromString(1234567.0, "1234567")),
                DynamicTest.dynamicTest("10", () -> testToFromString(12345678.0, "1.2345678E7")),
                DynamicTest.dynamicTest("11", () -> testToFromString(123456789.0, "1.23456789E8")),
                DynamicTest.dynamicTest("12", () -> testToFromString(1234567890.0, "1.23456789E9")),
                DynamicTest.dynamicTest("13", () -> testToFromString(12345678901.0, "1.2345678901E10")),
                DynamicTest.dynamicTest("14", () -> testToFromString(Math.PI, "3.141592653589793")),
                DynamicTest.dynamicTest("15", () -> testToFromString(-Math.PI, "-3.141592653589793")),
                DynamicTest.dynamicTest("16", () -> testToFromString(0.1, "0.1")),
                DynamicTest.dynamicTest("17", () -> testToFromString(0.02, "0.02")),
                DynamicTest.dynamicTest("18", () -> testToFromString(0.003, "0.003")),
                DynamicTest.dynamicTest("19", () -> testToFromString(0.0004, "4.0E-4")),
                DynamicTest.dynamicTest("20", () -> testToFromString(0.00005, "5.0E-5")),
                DynamicTest.dynamicTest("21", () -> testToFromString(0.000006, "6.0E-6")),
                DynamicTest.dynamicTest("22", () -> testToFromString(0.0000007, "7.0E-7")),
                DynamicTest.dynamicTest("23", () -> testToFromString(0.00000008, "8.0E-8")),
                DynamicTest.dynamicTest("24", () -> testToFromString(0.000000009, "9.0E-9")),
                DynamicTest.dynamicTest("25", () -> testToFromString(0.00000000987654321, "9.87654321E-9")),
                DynamicTest.dynamicTest("26", () -> testToFromString(0.000000009876543210, "9.87654321E-9")),
                DynamicTest.dynamicTest("27", () -> testToFromString(0.0000000098765432109, "9.8765432109E-9")),
                DynamicTest.dynamicTest("28", () -> testToFromString(-0.0000000098765432109, "-9.8765432109E-9")),
                DynamicTest.dynamicTest("29", () -> testToFromString(1.000000009, "1.000000009")),
                DynamicTest.dynamicTest("30", () -> testToFromString(20.000000009, "20.000000009")),
                DynamicTest.dynamicTest("31", () -> testToFromString(300.000000009, "300.000000009")),
                DynamicTest.dynamicTest("32", () -> testToFromString(4000.000000009, "4000.000000009")),
                DynamicTest.dynamicTest("33", () -> testToFromString(50000.000000009, "50000.000000009")),
                DynamicTest.dynamicTest("34", () -> testToFromString(600000.000000009, "600000.000000009")),
                DynamicTest.dynamicTest("35", () -> testToFromString(7000000.000000009, "7000000.000000009")),
                DynamicTest.dynamicTest("36", () -> testToFromString(80000000.000000009, "8.000000000000001E7")),
                DynamicTest.dynamicTest("37", () -> testToFromString(900000000.000000009, "9.0E8")),
                DynamicTest.dynamicTest("38", () -> testToFromString(1.00000000001, "1.00000000001")),
                DynamicTest.dynamicTest("39", () -> testToFromString(1.000000000002, "1.000000000002")),
                DynamicTest.dynamicTest("40", () -> testToFromString(1.0000000000003, "1.0000000000003")),
                DynamicTest.dynamicTest("41", () -> testToFromString(1.00000000000004, "1.00000000000004")),
                DynamicTest.dynamicTest("42", () -> testToFromString(1.000000000000005, "1.000000000000005")),
                DynamicTest.dynamicTest("43", () -> testToFromString(1.0000000000000006, "1.0000000000000007")),
                DynamicTest.dynamicTest("44", () -> testToFromString(1.00000000000000007, "1")),
                DynamicTest.dynamicTest("45", () -> testToFromString(1.000000000000000008, "1")),
                DynamicTest.dynamicTest("46", () -> testToFromString(1.0000000000000000009, "1")),
                DynamicTest.dynamicTest("47", () -> testToFromString(Double.MAX_VALUE, "1.7976931348623157E308")),
                DynamicTest.dynamicTest("48", () -> testToFromString(Double.MIN_VALUE, "4.9E-324")),
                DynamicTest.dynamicTest("49", () -> testToFromString(-Double.MAX_VALUE, "-1.7976931348623157E308")),
                DynamicTest.dynamicTest("50", () -> testToFromString(-Double.MIN_VALUE, "-4.9E-324")),
                DynamicTest.dynamicTest("51", () -> testToFromString(Double.NEGATIVE_INFINITY, "-INF")),
                DynamicTest.dynamicTest("52", () -> testToFromString(Double.POSITIVE_INFINITY, "INF")),
                DynamicTest.dynamicTest("53", () -> testToFromString(Double.NaN, "NaN"))
        );
    }
}
