/*
 * @(#)TextLayoutableFigure.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.figure;

import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.Labeled;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.css.value.DefaultUnitConverter;
import org.jhotdraw8.css.value.UnitConverter;
import org.jhotdraw8.draw.key.CssSizeStyleableKey;
import org.jhotdraw8.draw.key.DoubleStyleableKey;
import org.jhotdraw8.draw.key.NonNullEnumStyleableKey;
import org.jhotdraw8.draw.render.RenderContext;
import org.jhotdraw8.draw.render.RenderingIntent;

/**
 * A figure which supports font attributes.
 *
 * @author Werner Randelshofer
 */
public interface TextLayoutableFigure extends Figure {

    /**
     * The line spacing. Default value: {@code 0.0}
     */
    @Nullable CssSizeStyleableKey LINE_SPACING = new CssSizeStyleableKey("lineSpacing", CssSize.ZERO);
    /**
     * The text alignment. Default value: {@code left}
     */
    NonNullEnumStyleableKey<TextAlignment> TEXT_ALIGNMENT = new NonNullEnumStyleableKey<>("textAlignment", TextAlignment.class, TextAlignment.LEFT);
    /**
     * The vertical position of the text. Default value: {@code baseline}
     */
    NonNullEnumStyleableKey<VPos> TEXT_VPOS = new NonNullEnumStyleableKey<>("textVPos", VPos.class, VPos.BASELINE);
    /**
     * The horizontal position of the text. Default value: {@code left}
     */
    NonNullEnumStyleableKey<HPos> TEXT_HPOS = new NonNullEnumStyleableKey<>("textHPos", HPos.class, HPos.LEFT);
    /**
     * Text wrapping width. Default value: {@code 0.0} (no wrapping).
     */
    DoubleStyleableKey WRAPPING_WIDTH = new DoubleStyleableKey("wrappingWidth", 0.0);

    /**
     * Updates a text node with fontable properties.
     *
     * @param ctx  RenderContext, can be null
     * @param text a text node
     */
    default void applyTextLayoutableFigureProperties(@Nullable RenderContext ctx, @NonNull Text text) {
        UnitConverter units = ctx == null ? DefaultUnitConverter.getInstance() : ctx.getNonNull(RenderContext.UNIT_CONVERTER_KEY);

        CssSize lineSpacing = getStyledNonNull(LINE_SPACING);
        double d;
        if (UnitConverter.PERCENTAGE.equals(lineSpacing.getUnits())) {
            CssSize fontSize = getStyled(TextFontableFigure.FONT_SIZE);
            if (fontSize != null) {
                d = lineSpacing.getValue() * units.convert(fontSize, UnitConverter.DEFAULT) / 100.0;
            } else {
                d = units.convert(lineSpacing, UnitConverter.DEFAULT);
            }
        } else {
            d = units.convert(lineSpacing, UnitConverter.DEFAULT);
        }
        if (text.getLineSpacing() != d) {
            text.setLineSpacing(d);
        }
        d = getStyledNonNull(WRAPPING_WIDTH);
        if (text.getWrappingWidth() != d) {
            text.setWrappingWidth(d);
        }
        TextAlignment ta = getStyledNonNull(TEXT_ALIGNMENT);
        if (text.getTextAlignment() != ta) {
            text.setTextAlignment(ta);
        }
        VPos vp = getStyledNonNull(TEXT_VPOS);
        if (text.getTextOrigin() != vp) {
            text.setTextOrigin(vp);
        }


        final FontSmoothingType fst = ctx == null || ctx.getNonNull(RenderContext.RENDERING_INTENT) == RenderingIntent.EDITOR
                ? FontSmoothingType.LCD : FontSmoothingType.GRAY;
        if (text.getFontSmoothingType() != fst) {
            text.setFontSmoothingType(fst);
        }

    }

    /**
     * Updates a Labeled node with fontable properties.
     *
     * @param ctx  context
     * @param text a text node
     */
    default void applyTextLayoutableFigureProperties(@Nullable RenderContext ctx, @NonNull Labeled text) {
        UnitConverter units = ctx == null ? DefaultUnitConverter.getInstance() : ctx.getNonNull(RenderContext.UNIT_CONVERTER_KEY);
        double d = units.convert(getStyledNonNull(LINE_SPACING), UnitConverter.DEFAULT);
        if (text.getLineSpacing() == d) {
            text.setLineSpacing(d);
        }
        TextAlignment ta = getStyledNonNull(TEXT_ALIGNMENT);
        if (text.getTextAlignment() == ta) {
            text.setTextAlignment(ta);
        }
    }
}
