/*
 * @(#)CharSequenceCssScannerTest.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.css;

import org.jhotdraw8.css.parser.CharSequenceCssScanner;
import org.jhotdraw8.css.parser.CssScanner;

public class CharSequenceCssScannerTest extends AbstractCssScannerTest {
    @Override
    protected CssScanner createScanner(String inputData) {
        return new CharSequenceCssScanner(inputData);
    }
}
