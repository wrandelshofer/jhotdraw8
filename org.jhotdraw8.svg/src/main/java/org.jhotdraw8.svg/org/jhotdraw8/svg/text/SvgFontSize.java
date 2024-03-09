/*
 * @(#)SvgFontSize.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */

package org.jhotdraw8.svg.text;

import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.css.value.UnitConverter;
import org.jhotdraw8.draw.figure.Figure;

/**
 * SVG font size.
 * <p>
 * References:
 * <dl>
 *     <dt>CSS-Fonts-3</dt>
 *     <dd><a href="https://www.w3.org/TR/css-fonts-3/#font-size-prop">w3.org</a></dd>
 * </dl>
 */
public record SvgFontSize(SvgFontSize.@Nullable SizeKeyword keyword, @Nullable CssSize length) {
    public enum SizeKeyword {
        XX_SMALL, X_SMALL, SMALL, MEDIUM, LARGE, X_LARGE, XX_LARGE,
        SMALLER, LARGER
    }

    public SvgFontSize(@Nullable SizeKeyword keyword, @Nullable CssSize length) {
        this.keyword = keyword;
        this.length = length;
    }

    public double getConvertedValue(Figure figure, UnitConverter converter) {
        if (keyword != null) {
            double value = 12;
            return switch (keyword) {
                case XX_SMALL -> value * 3d / 5d;
                case X_SMALL -> value * 3d / 4d;
                case SMALL, SMALLER ->// FIXME should use size of parent element
                        value * 8d / 9d;
                default -> value;
                case LARGE, LARGER ->// FIXME should use size of parent element
                        value * 6d / 5d;
                case X_LARGE -> value * 3d / 2d;
                case XX_LARGE -> value * 2d;
            };
        } else if (length != null) {
            return length.getConvertedValue();
        } else {
            return 12;
        }
    }
}
