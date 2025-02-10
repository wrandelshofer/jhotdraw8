/*
 * @(#)AbstractCssScannerTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css;

import org.jhotdraw8.css.parser.CssScanner;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

/**
 * CssScannerNGTest.
 *
 */
public abstract class AbstractCssScannerTest {

    public AbstractCssScannerTest() {
    }

    protected abstract CssScanner createScanner(String inputData);

    /**
     * Test of nextChar method, of class CssScanner.
     */
    public void testScanner(String inputData, String expectedValue) throws Exception {
        CssScanner s = createScanner(inputData);
        //
        StringBuilder buf = new StringBuilder();
        while (s.nextChar() != -1) {
            buf.append((char) s.currentChar());
        }
        String actualValue = buf.toString();

        assertEquals(actualValue, expectedValue);
    }


    @TestFactory
    public List<DynamicTest> dynamicTestsScanner() {
        return Arrays.asList(
                dynamicTest("abcd abcd", () -> testScanner("abcd", "abcd")),
                //
                dynamicTest("ab\ncd ab\ncd", () -> testScanner("ab\ncd", "ab\ncd")),
                dynamicTest("ab\r\ncd ab\ncd", () -> testScanner("ab\r\ncd", "ab\ncd")),
                dynamicTest("ab\fcd ab\ncd", () -> testScanner("ab\fcd", "ab\ncd")),
                dynamicTest("ab\rcd ab\ncd", () -> testScanner("ab\rcd", "ab\ncd")),
                //
                dynamicTest("abcd\n abcd\n", () -> testScanner("abcd\n", "abcd\n")),
                dynamicTest("abcd\r\n abcd\n", () -> testScanner("abcd\r\n", "abcd\n")),
                dynamicTest("abcd\f abcd\n", () -> testScanner("abcd\f", "abcd\n")),
                dynamicTest("abcd\r abcd\n", () -> testScanner("abcd\r", "abcd\n"))
        );
    }

}
