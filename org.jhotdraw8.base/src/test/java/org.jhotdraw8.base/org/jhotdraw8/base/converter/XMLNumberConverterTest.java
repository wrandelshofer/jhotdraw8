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

        Assertions.assertEquals(actualValue, expectedValue);
    }

    /**
     * Test of toString method, of class XmlNumberConverter.
     */
    public static void testFromString(Double expectedValue, @NonNull String inputValue) throws ParseException, IOException {
        XmlNumberConverter c = new XmlNumberConverter();

        Number actualValue = c.fromString(inputValue);

        Assertions.assertEquals(actualValue, expectedValue);
    }

    public static void testToFromString(Double doubleValue, @NonNull String stringValue) throws Exception {
        testToString(doubleValue, stringValue);
        testFromString(doubleValue, stringValue);
    }

    @TestFactory
    public @NonNull List<DynamicTest> dynamicTestsFromString() {
        return Arrays.asList(
                DynamicTest.dynamicTest("1", () -> testToFromString(-0.0, "-0")),
                DynamicTest.dynamicTest("1", () -> testToFromString(0.0, "0")),
                DynamicTest.dynamicTest("1", () -> testToFromString(1.0, "1")),
                DynamicTest.dynamicTest("1", () -> testToFromString(12.0, "12")),
                DynamicTest.dynamicTest("1", () -> testToFromString(123.0, "123")),
                DynamicTest.dynamicTest("1", () -> testToFromString(1234.0, "1234")),
                DynamicTest.dynamicTest("1", () -> testToFromString(12345.0, "12345")),
                DynamicTest.dynamicTest("1", () -> testToFromString(123456.0, "123456")),
                DynamicTest.dynamicTest("1", () -> testToFromString(1234567.0, "1234567")),
                DynamicTest.dynamicTest("1", () -> testToFromString(12345678.0, "1.2345678E7")),
                DynamicTest.dynamicTest("1", () -> testToFromString(123456789.0, "1.23456789E8")),
                DynamicTest.dynamicTest("1", () -> testToFromString(1234567890.0, "1.23456789E9")),
                DynamicTest.dynamicTest("1", () -> testToFromString(12345678901.0, "1.2345678901E10")),
                DynamicTest.dynamicTest("1", () -> testToFromString(Math.PI, "3.141592653589793")),
                DynamicTest.dynamicTest("1", () -> testToFromString(-Math.PI, "-3.141592653589793")),
                DynamicTest.dynamicTest("1", () -> testToFromString(0.1, "0.1")),
                DynamicTest.dynamicTest("1", () -> testToFromString(0.02, "0.02")),
                DynamicTest.dynamicTest("1", () -> testToFromString(0.003, "0.003")),
                DynamicTest.dynamicTest("1", () -> testToFromString(0.0004, "4.0E-4")),
                DynamicTest.dynamicTest("1", () -> testToFromString(0.00005, "5.0E-5")),
                DynamicTest.dynamicTest("1", () -> testToFromString(0.000006, "6.0E-6")),
                DynamicTest.dynamicTest("1", () -> testToFromString(0.0000007, "7.0E-7")),
                DynamicTest.dynamicTest("1", () -> testToFromString(0.00000008, "8.0E-8")),
                DynamicTest.dynamicTest("1", () -> testToFromString(0.000000009, "9.0E-9")),
                DynamicTest.dynamicTest("1", () -> testToFromString(0.00000000987654321, "9.87654321E-9")),
                DynamicTest.dynamicTest("1", () -> testToFromString(0.000000009876543210, "9.87654321E-9")),
                DynamicTest.dynamicTest("1", () -> testToFromString(0.0000000098765432109, "9.8765432109E-9")),
                DynamicTest.dynamicTest("1", () -> testToFromString(-0.0000000098765432109, "-9.8765432109E-9")),
                DynamicTest.dynamicTest("1", () -> testToFromString(1.000000009, "1.000000009")),
                DynamicTest.dynamicTest("1", () -> testToFromString(20.000000009, "20.000000009")),
                DynamicTest.dynamicTest("1", () -> testToFromString(300.000000009, "300.000000009")),
                DynamicTest.dynamicTest("1", () -> testToFromString(4000.000000009, "4000.000000009")),
                DynamicTest.dynamicTest("1", () -> testToFromString(50000.000000009, "50000.000000009")),
                DynamicTest.dynamicTest("1", () -> testToFromString(600000.000000009, "600000.000000009")),
                DynamicTest.dynamicTest("1", () -> testToFromString(7000000.000000009, "7000000.000000009")),
                DynamicTest.dynamicTest("1", () -> testToFromString(80000000.000000009, "8.000000000000001E7")),
                DynamicTest.dynamicTest("1", () -> testToFromString(900000000.000000009, "9.0E8")),
                DynamicTest.dynamicTest("1", () -> testToFromString(1.00000000001, "1.00000000001")),
                DynamicTest.dynamicTest("1", () -> testToFromString(1.000000000002, "1.000000000002")),
                DynamicTest.dynamicTest("1", () -> testToFromString(1.0000000000003, "1.0000000000003")),
                DynamicTest.dynamicTest("1", () -> testToFromString(1.00000000000004, "1.00000000000004")),
                DynamicTest.dynamicTest("1", () -> testToFromString(1.000000000000005, "1.000000000000005")),
                DynamicTest.dynamicTest("1", () -> testToFromString(1.0000000000000006, "1.0000000000000007")),
                DynamicTest.dynamicTest("1", () -> testToFromString(1.00000000000000007, "1")),
                DynamicTest.dynamicTest("1", () -> testToFromString(1.000000000000000008, "1")),
                DynamicTest.dynamicTest("1", () -> testToFromString(1.0000000000000000009, "1")),
                DynamicTest.dynamicTest("1", () -> testToFromString(Double.MAX_VALUE, "1.7976931348623157E308")),
                DynamicTest.dynamicTest("1", () -> testToFromString(Double.MIN_VALUE, "4.9E-324")),
                DynamicTest.dynamicTest("1", () -> testToFromString(-Double.MAX_VALUE, "-1.7976931348623157E308")),
                DynamicTest.dynamicTest("1", () -> testToFromString(-Double.MIN_VALUE, "-4.9E-324")),
                DynamicTest.dynamicTest("1", () -> testToFromString(Double.NEGATIVE_INFINITY, "-INF")),
                DynamicTest.dynamicTest("1", () -> testToFromString(Double.POSITIVE_INFINITY, "INF")),
                DynamicTest.dynamicTest("1", () -> testToFromString(Double.NaN, "NaN"))
        );
    }
}
