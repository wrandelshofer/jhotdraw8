/*
 * @(#)CssDimension2DConverter.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.css.converter;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.converter.IdResolver;
import org.jhotdraw8.base.converter.IdSupplier;
import org.jhotdraw8.css.converter.AbstractCssConverter;
import org.jhotdraw8.css.parser.CssToken;
import org.jhotdraw8.css.parser.CssTokenType;
import org.jhotdraw8.css.parser.CssTokenizer;
import org.jhotdraw8.css.value.CssSize;
import org.jhotdraw8.draw.css.value.CssDimension2D;

import java.io.IOException;
import java.text.ParseException;
import java.util.function.Consumer;

import static org.jhotdraw8.css.converter.CssSizeConverter.parseSize;

/**
 * Converts a {@code javafx.geometry.CssDimension2D} into a {@code String} and vice
 * versa.
 *
 * @author Werner Randelshofer
 */
public class CssDimension2DConverter extends AbstractCssConverter<CssDimension2D> {
    private final boolean withSpace;
    private final boolean withComma;

    public CssDimension2DConverter(boolean nullable) {
        this(nullable, true, false);
    }

    public CssDimension2DConverter(boolean nullable, boolean withSpace, boolean withComma) {
        super(nullable);
        this.withSpace = withSpace || !withComma;
        this.withComma = withComma;
    }

    @Override
    public @NonNull CssDimension2D parseNonNull(@NonNull CssTokenizer tt, @Nullable IdResolver idResolver) throws ParseException, IOException {
        final CssSize x, y;
        x = parseSize(tt, "x");
        tt.skipIfPresent(CssTokenType.TT_COMMA);
        y = parseSize(tt, "y");

        return new CssDimension2D(x, y);
    }

    @Override
    protected <TT extends CssDimension2D> void produceTokensNonNull(@NonNull TT value, @Nullable IdSupplier idSupplier, @NonNull Consumer<CssToken> out) {
        CssSize x = value.getWidth();
        out.accept(new CssToken(CssTokenType.TT_DIMENSION, x.getValue(), x.getUnits()));
        if (withComma) {
            out.accept(new CssToken(CssTokenType.TT_COMMA));
        }
        if (withSpace) {
            out.accept(new CssToken(CssTokenType.TT_S, " "));
        }
        CssSize y = value.getHeight();
        out.accept(new CssToken(CssTokenType.TT_DIMENSION, y.getValue(), y.getUnits()));
    }

    @Override
    public String getHelpText() {
        return "Format of ⟨CssDimension2D⟩: ⟨x⟩ ⟨y⟩";
    }
}
