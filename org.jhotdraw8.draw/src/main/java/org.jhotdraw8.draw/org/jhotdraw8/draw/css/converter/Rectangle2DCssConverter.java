/*
 * @(#)CssRectangle2DConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.css.converter;

import org.jhotdraw8.base.converter.IdResolver;
import org.jhotdraw8.base.converter.IdSupplier;
import org.jhotdraw8.css.converter.AbstractCssConverter;
import org.jhotdraw8.css.parser.CssToken;
import org.jhotdraw8.css.parser.CssTokenType;
import org.jhotdraw8.css.parser.CssTokenizer;
import org.jhotdraw8.css.value.CssRectangle2D;
import org.jhotdraw8.css.value.CssSize;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.text.ParseException;
import java.util.function.Consumer;

import static org.jhotdraw8.css.converter.SizeCssConverter.parseSize;


/**
 * Converts a {@code javafx.geometry.CssRectangle2D} into a {@code String} and vice
 * versa.
 *
 */
public class Rectangle2DCssConverter extends AbstractCssConverter<CssRectangle2D> {
    private final boolean withSpace;
    private final boolean withComma;

    public Rectangle2DCssConverter(boolean nullable) {
        this(nullable, true, false);
    }

    public Rectangle2DCssConverter(boolean nullable, boolean withSpace, boolean withComma) {
        super(nullable);
        this.withSpace = withSpace || !withComma;
        this.withComma = withComma;
    }

    @Override
    public CssRectangle2D parseNonNull(CssTokenizer tt, @Nullable IdResolver idResolver) throws ParseException, IOException {
        final CssSize x, y, width, height;
        x = parseSize(tt, "x");
        tt.skipIfPresent(CssTokenType.TT_COMMA);
        y = parseSize(tt, "y");
        tt.skipIfPresent(CssTokenType.TT_COMMA);
        width = parseSize(tt, "width");
        tt.skipIfPresent(CssTokenType.TT_COMMA);
        height = parseSize(tt, "height");

        return new CssRectangle2D(x, y, width, height);
    }

    @Override
    protected <TT extends CssRectangle2D> void produceTokensNonNull(TT value, @Nullable IdSupplier idSupplier, Consumer<CssToken> out) {
        CssSize x = value.getMinX();
        CssSize y = value.getMinY();
        CssSize width = value.getWidth();
        CssSize height = value.getHeight();
        out.accept(new CssToken(CssTokenType.TT_DIMENSION, x.getValue(), x.getUnits()));
        produceDelimiter(out);
        out.accept(new CssToken(CssTokenType.TT_DIMENSION, y.getValue(), y.getUnits()));
        produceDelimiter(out);
        out.accept(new CssToken(CssTokenType.TT_DIMENSION, width.getValue(), width.getUnits()));
        produceDelimiter(out);
        out.accept(new CssToken(CssTokenType.TT_DIMENSION, height.getValue(), height.getUnits()));
    }

    private void produceDelimiter(Consumer<CssToken> out) {
        if (withComma) {
            out.accept(new CssToken(CssTokenType.TT_COMMA));
        }
        if (withSpace) {
            out.accept(new CssToken(CssTokenType.TT_S, " "));
        }
    }

    @Override
    public @Nullable String getHelpText() {
        return "Format of ⟨CssRectangle2D⟩: ⟨x⟩ ⟨y⟩ ⟨width⟩ ⟨height⟩";
    }
}
