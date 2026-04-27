/*
 * @(#)PatternStrokableFigure.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.figure;

import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.draw.key.CssRectangle2DStyleableMapAccessor;
import org.jhotdraw8.draw.key.CssSizeStyleableKey;
import org.jhotdraw8.draw.key.NullableUriStyleableKey;

/// Defines properties for figures that support a pattern in a stroke.
public interface PatternStrokableFigure extends Figure {

    /// URI of the stroke pattern image.
    ///
    /// Default value: `null`.
    NullableUriStyleableKey STROKE_PATTERN = new NullableUriStyleableKey("strokePattern", null);

    /// Height of the stroke pattern.
    ///
    /// Default value: `1`.
    CssSizeStyleableKey STROKE_PATTERN_HEIGHT = new CssSizeStyleableKey("strokePatternHeight", CssSize.ONE);

    /// Width of the stroke pattern.
    ///
    /// Default value: `1`.
    CssSizeStyleableKey STROKE_PATTERN_WIDTH = new CssSizeStyleableKey("strokePatternWidth", CssSize.ONE);

    /// X-origin of the stroke pattern.
    ///
    /// Default value: `0`.
    CssSizeStyleableKey STROKE_PATTERN_X = new CssSizeStyleableKey("strokePatternX", CssSize.ZERO);

    /// Y-origin of the stroke pattern.
    ///
    /// Default value: `0`.
    CssSizeStyleableKey STROKE_PATTERN_Y = new CssSizeStyleableKey("strokePatternY", CssSize.ZERO);

    /// Shorthand for [#STROKE_PATTERN_X], [#STROKE_PATTERN_Y], [#STROKE_PATTERN_WIDTH],
    /// [#STROKE_PATTERN_HEIGHT].
    CssRectangle2DStyleableMapAccessor STROKE_PATTERN_RECTANGLE = new CssRectangle2DStyleableMapAccessor("strokePatternRectangle", STROKE_PATTERN_X,
            STROKE_PATTERN_Y,
            STROKE_PATTERN_WIDTH, STROKE_PATTERN_HEIGHT);

}
