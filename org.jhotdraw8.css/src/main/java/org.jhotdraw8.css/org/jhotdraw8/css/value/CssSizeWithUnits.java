/*
 * @(#)CssSizeWithUnits.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.value;

import org.jspecify.annotations.Nullable;

/// Represents a size specified in a particular unit.
///
/// A CssSize can be used to hold the value of a CSS `number-token`,
/// `percentage-token` or `dimension-token`.
///
/// Unlike [javafx.css.Size] this class supports an open ended
/// set of units.
///
/// References:
/// <dl>
/// <dt>CSS Syntax Module Level 3, Chapter 4. Tokenization</dt>
/// <dd><a href="https://www.w3.org/TR/2019/CR-css-syntax-3-20190716/#tokenization">w3.org</a></dd>
/// </dl>
public class CssSizeWithUnits extends CssSize {
    private final String units;

    CssSizeWithUnits(double value, @Nullable String units) {
        super(value);
        this.units = units;
    }

    @Override
    public String getUnits() {
        return units;
    }

}
