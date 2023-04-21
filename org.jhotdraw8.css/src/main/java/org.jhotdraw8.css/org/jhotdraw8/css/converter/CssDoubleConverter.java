/*
 * @(#)CssDoubleConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.converter;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.converter.IdResolver;
import org.jhotdraw8.base.converter.IdSupplier;
import org.jhotdraw8.css.parser.CssToken;
import org.jhotdraw8.css.parser.CssTokenType;
import org.jhotdraw8.css.parser.CssTokenizer;

import java.io.IOException;
import java.text.ParseException;
import java.util.function.Consumer;

/**
 * CssDoubleConverter.
 * <p>
 * Parses an attribute value of type double.
 *
 * @author Werner Randelshofer
 */
public class CssDoubleConverter extends AbstractCssConverter<Double> {

    public CssDoubleConverter(boolean nullable) {
        super(nullable);
    }

    @Override
    public @NonNull Double parseNonNull(@NonNull CssTokenizer tt, @Nullable IdResolver idResolver) throws ParseException, IOException {
        switch (tt.next()) {
        case CssTokenType.TT_NUMBER:
            return tt.currentNumberNonNull().doubleValue();
        case CssTokenType.TT_IDENT: {
            double value;
            switch (tt.currentStringNonNull()) {
            case "INF":
                value = Double.POSITIVE_INFINITY;
                break;
            case "-INF":
                value = Double.NEGATIVE_INFINITY;
                break;
            case "NaN":
                value = Double.NaN;
                break;
            default:
                throw tt.createParseException("⟨Double⟩: number expected.");
            }
            return value;
        }
        default:
            throw tt.createParseException("⟨Double⟩: number expected.");
        }
    }

    @Override
    public <TT extends Double> void produceTokensNonNull(@NonNull TT value, @Nullable IdSupplier idSupplier, @NonNull Consumer<CssToken> out) {
        double v = value;
        if (value.isInfinite()) {
            out.accept(new CssToken(CssTokenType.TT_IDENT, (v > 0) ? "INF" : "-INF"));
        } else if (value.isNaN()) {
            out.accept(new CssToken(CssTokenType.TT_IDENT, "NaN"));
        } else {
            out.accept(new CssToken(CssTokenType.TT_NUMBER, value));
        }
    }

    @Override
    public @NonNull String getHelpText() {
        return "Format of ⟨Double⟩: ⟨double⟩";
    }

}
