/*
 * @(#)Point2DConverter.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.draw.css.converter;

import javafx.geometry.Point2D;
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
 * Converts a {@code javafx.geometry.Point2D} into a {@code String} and vice
 * versa.
 *
 * @author Werner Randelshofer
 */
public class Point2DConverter extends AbstractCssConverter<Point2D> {

    private final boolean withSpace;
    private final boolean withComma;

    public Point2DConverter(boolean nullable) {
        this(nullable, true);
    }

    public Point2DConverter(boolean nullable, boolean withSpace) {
        super(nullable);
        this.withSpace = withSpace;
        this.withComma = !withSpace;
    }

    @Override
    public @NonNull String getHelpText() {
        return "Format of ⟨Point2D⟩: ⟨x⟩, ⟨y⟩";
    }

    @Override
    public @NonNull Point2D parseNonNull(@NonNull CssTokenizer tt, @Nullable IdResolver idResolver) throws ParseException, IOException {
        final double x, y;
        tt.requireNextToken(CssTokenType.TT_NUMBER, " ⟨Point2D⟩: ⟨x⟩ expected.");
        x = tt.currentNumberNonNull().doubleValue();
        tt.skipIfPresent(CssTokenType.TT_COMMA);
        tt.requireNextToken(CssTokenType.TT_NUMBER, " ⟨Point2D⟩: ⟨y⟩ expected.");
        y = tt.currentNumberNonNull().doubleValue();

        return new Point2D(x, y);
    }

    @Override
    protected <TT extends Point2D> void produceTokensNonNull(@NonNull TT value, @Nullable IdSupplier idSupplier, @NonNull Consumer<CssToken> out) {
        out.accept(new CssToken(CssTokenType.TT_NUMBER, value.getX()));
        produceDelimiter(out);
        out.accept(new CssToken(CssTokenType.TT_NUMBER, value.getY()));
    }


    private void produceDelimiter(@NonNull Consumer<CssToken> out) {
        if (withComma) {
            out.accept(new CssToken(CssTokenType.TT_COMMA));
        }
        if (withSpace) {
            out.accept(new CssToken(CssTokenType.TT_S, " "));
        }
    }
}
