/*
 * @(#)TextFontableFigure.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.figure;

import javafx.scene.control.Labeled;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.css.value.DefaultUnitConverter;
import org.jhotdraw8.css.value.UnitConverter;
import org.jhotdraw8.draw.css.value.CssFont;
import org.jhotdraw8.draw.key.CssSizeStyleableKey;
import org.jhotdraw8.draw.key.FontStyleableMapAccessor;
import org.jhotdraw8.draw.key.NonNullBooleanStyleableKey;
import org.jhotdraw8.draw.key.NonNullEnumStyleableKey;
import org.jhotdraw8.draw.key.StringOrIdentStyleableKey;
import org.jhotdraw8.draw.render.RenderContext;
import org.jhotdraw8.draw.render.RenderingIntent;
import org.jspecify.annotations.Nullable;

/**
 * A figure which supports font attributes.
 *
 */
public interface TextFontableFigure extends Figure {

    // text properties
    /**
     * Defines the font used. Default value: {@code new Font("Arial",12)}
     */
    StringOrIdentStyleableKey FONT_FAMILY = new StringOrIdentStyleableKey("fontFamily", "Arial");
    CssSizeStyleableKey FONT_SIZE = new CssSizeStyleableKey("fontSize", CssSize.of(12.0));
    NonNullEnumStyleableKey<FontPosture> FONT_STYLE = new NonNullEnumStyleableKey<>("fontStyle", FontPosture.class, FontPosture.REGULAR);
    NonNullEnumStyleableKey<FontWeight> FONT_WEIGHT = new NonNullEnumStyleableKey<>("fontWeight", FontWeight.class, FontWeight.NORMAL);
    FontStyleableMapAccessor FONT = new FontStyleableMapAccessor("font", FONT_FAMILY, FONT_WEIGHT, FONT_STYLE, FONT_SIZE);
    /**
     * Whether to strike through the text. Default value: {@code false}
     */
    NonNullBooleanStyleableKey STRIKETHROUGH = new NonNullBooleanStyleableKey("strikethrough", false);
    /**
     * Whether to underline the text. Default value: {@code false}
     */
    NonNullBooleanStyleableKey UNDERLINE = new NonNullBooleanStyleableKey("underline", false);

    /**
     * Updates a text node with fontable properties.
     *
     * @param ctx  RenderContext, can be null
     * @param text a text node
     */
    default void applyTextFontableFigureProperties(@Nullable RenderContext ctx, Text text) {
        String family = getStyledNonNull(FONT_FAMILY);
        FontPosture style = getStyledNonNull(FONT_STYLE);
        FontWeight weight = getStyledNonNull(FONT_WEIGHT);
        UnitConverter units = ctx == null ? DefaultUnitConverter.getInstance() : ctx.getNonNull(RenderContext.UNIT_CONVERTER_KEY);
        CssSize cssSize = getStyledNonNull(FONT_SIZE);
        double size = units.convert(cssSize, UnitConverter.DEFAULT);
        CssFont f = CssFont.font(family, weight, style, size);

        Font font = f.getFont();
        if (!text.getFont().equals(font)) {
            text.setFont(font);
        }
        boolean b = getStyledNonNull(UNDERLINE);
        if (text.isUnderline() != b) {
            text.setUnderline(b);
        }
        b = getStyledNonNull(STRIKETHROUGH);
        if (text.isStrikethrough() != b) {
            text.setStrikethrough(b);
        }

        final FontSmoothingType fst = ctx == null || ctx.getNonNull(RenderContext.RENDERING_INTENT) == RenderingIntent.EDITOR
                ? FontSmoothingType.LCD : FontSmoothingType.GRAY;
        if (text.getFontSmoothingType() != fst) {
            text.setFontSmoothingType(fst);
        }

    }

    /**
     * Updates a Laeled node with fontable properties.
     *
     * @param ctx  context
     * @param text a text node
     */
    default void applyTextFontableFigureProperties(@Nullable RenderContext ctx, Labeled text) {
        UnitConverter units = ctx == null ? DefaultUnitConverter.getInstance() : ctx.getNonNull(RenderContext.UNIT_CONVERTER_KEY);
        Font font = getStyledNonNull(FONT).getFont();
        if (!text.getFont().equals(font)) {
            text.setFont(font);
        }
        boolean b = getStyledNonNull(UNDERLINE);
        if (text.isUnderline() == b) {
            text.setUnderline(b);
        }
    }
}
