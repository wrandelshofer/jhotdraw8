/*
 * @(#)Rectangle2DConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.css.converter;

import javafx.geometry.Rectangle2D;
import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.converter.IdResolver;
import org.jhotdraw8.base.converter.IdSupplier;
import org.jhotdraw8.css.converter.AbstractCssConverter;
import org.jhotdraw8.css.parser.CssToken;
import org.jhotdraw8.css.parser.CssTokenType;
import org.jhotdraw8.css.parser.CssTokenizer;

import java.io.IOException;
import java.text.ParseException;
import java.util.function.Consumer;

/**
 * Converts a {@code javafx.geometry.Rectangle2D} into a {@code String} and vice
 * versa.
 *
 * @author Werner Randelshofer
 */
public class Rectangle2DConverter extends AbstractCssConverter<Rectangle2D> {
    private final boolean withSpace;
    private final boolean withComma;

    public Rectangle2DConverter(boolean nullable) {
        this(nullable, true, false);
    }

    public Rectangle2DConverter(boolean nullable, boolean withSpace, boolean withComma) {
        super(nullable);
        this.withSpace = withSpace || !withComma;
        this.withComma = withComma;
    }

    @Override
    public @NonNull Rectangle2D parseNonNull(@NonNull CssTokenizer tt, @Nullable IdResolver idResolver) throws ParseException, IOException {
        final double x, y, width, height;
        tt.requireNextToken(CssTokenType.TT_NUMBER, " ⟨Rectangle2D⟩: ⟨x⟩ expected.");
        x = tt.currentNumberNonNull().doubleValue();
        tt.skipIfPresent(CssTokenType.TT_COMMA);
        tt.requireNextToken(CssTokenType.TT_NUMBER, " ⟨Rectangle2D⟩: ⟨y⟩ expected.");
        y = tt.currentNumberNonNull().doubleValue();
        tt.skipIfPresent(CssTokenType.TT_COMMA);
        tt.requireNextToken(CssTokenType.TT_NUMBER, " ⟨Rectangle2D⟩: ⟨width⟩ expected.");
        width = tt.currentNumberNonNull().doubleValue();
        tt.skipIfPresent(CssTokenType.TT_COMMA);
        tt.requireNextToken(CssTokenType.TT_NUMBER, " ⟨Rectangle2D⟩: ⟨height⟩ expected.");
        height = tt.currentNumberNonNull().doubleValue();

        return new Rectangle2D(x, y, width, height);
    }

    @Override
    protected <TT extends Rectangle2D> void produceTokensNonNull(@NonNull TT value, @Nullable IdSupplier idSupplier, @NonNull Consumer<CssToken> out) {
        out.accept(new CssToken(CssTokenType.TT_NUMBER, value.getMinX()));
        produceDelimiter(out);
        out.accept(new CssToken(CssTokenType.TT_NUMBER, value.getMinY()));
        produceDelimiter(out);
        out.accept(new CssToken(CssTokenType.TT_NUMBER, value.getWidth()));
        produceDelimiter(out);
        out.accept(new CssToken(CssTokenType.TT_NUMBER, value.getHeight()));
    }

    private void produceDelimiter(@NonNull Consumer<CssToken> out) {
        if (withComma) {
            out.accept(new CssToken(CssTokenType.TT_COMMA));
        }
        if (withSpace) {
            out.accept(new CssToken(CssTokenType.TT_S, " "));
        }
    }

    @Override
    public String getHelpText() {
        return "Format of ⟨Rectangle2D⟩: ⟨x⟩ ⟨y⟩ ⟨width⟩ ⟨height⟩";
    }
}
