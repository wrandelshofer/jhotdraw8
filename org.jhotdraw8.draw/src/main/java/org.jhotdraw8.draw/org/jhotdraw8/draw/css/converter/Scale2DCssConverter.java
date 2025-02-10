/*
 * @(#)CssScale2DConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.css.converter;

import javafx.geometry.Point2D;
import org.jhotdraw8.base.converter.IdResolver;
import org.jhotdraw8.base.converter.IdSupplier;
import org.jhotdraw8.css.converter.AbstractCssConverter;
import org.jhotdraw8.css.parser.CssToken;
import org.jhotdraw8.css.parser.CssTokenType;
import org.jhotdraw8.css.parser.CssTokenizer;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.text.ParseException;
import java.util.function.Consumer;

/**
 * Converts a {@code javafx.geometry.Point2D} into a {@code String} and vice
 * versa.
 *
 */
public class Scale2DCssConverter extends AbstractCssConverter<Point2D> {

    private final boolean withSpace;
    private final boolean withComma = false;

    public Scale2DCssConverter() {
        this(false, true);
    }

    public Scale2DCssConverter(boolean nullable) {
        this(nullable, true);
    }

    public Scale2DCssConverter(boolean nullable, boolean withSpace) {
        super(nullable);
        this.withSpace = withSpace;
    }

    @Override
    public Point2D parseNonNull(CssTokenizer tt, @Nullable IdResolver idResolver) throws ParseException, IOException {
        final double x, y;
        tt.requireNextToken(CssTokenType.TT_NUMBER, " ⟨Scale2D⟩: ⟨x⟩ expected.");
        x = tt.currentNumberNonNull().doubleValue();
        if (tt.next() == CssTokenType.TT_EOF) {
            y = x;
        } else {
            tt.skipIfPresent(CssTokenType.TT_COMMA);
            tt.requireNextToken(CssTokenType.TT_NUMBER, " ⟨Scale2D⟩: ⟨y⟩ expected.");
            y = tt.currentNumberNonNull().doubleValue();
        }

        return new Point2D(x, y);
    }

    @Override
    protected <TT extends Point2D> void produceTokensNonNull(TT value, @Nullable IdSupplier idSupplier, Consumer<CssToken> out) {
        double x = value.getX();
        double y = value.getY();
        out.accept(new CssToken(CssTokenType.TT_NUMBER, x));
        if (x != y) {
            if (withComma) {
                out.accept(new CssToken(CssTokenType.TT_COMMA));
            }
            if (withSpace) {
                out.accept(new CssToken(CssTokenType.TT_S, " "));
            }
            out.accept(new CssToken(CssTokenType.TT_NUMBER, y));
        }
    }

    @Override
    public @Nullable String getHelpText() {
        return "Format of ⟨Scale2D⟩: ⟨s⟩ ｜ ⟨xs⟩ ⟨ys⟩";
    }
}
