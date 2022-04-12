/*
 * @(#)CharSequenceCssScannerTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.css;

public class CharSequenceCssScannerTest extends AbstractCssScannerTest {
    @Override
    protected CssScanner createScanner(String inputData) {
        return new CharSequenceCssScanner(inputData);
    }
}
