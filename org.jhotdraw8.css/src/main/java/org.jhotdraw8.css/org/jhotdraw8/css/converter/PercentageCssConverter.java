/*
 * @(#)CssPercentageConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.converter;

import org.jhotdraw8.base.converter.IdResolver;
import org.jhotdraw8.base.converter.IdSupplier;
import org.jhotdraw8.css.parser.CssToken;
import org.jhotdraw8.css.parser.CssTokenType;
import org.jhotdraw8.css.parser.CssTokenizer;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.text.ParseException;
import java.util.function.Consumer;

/**
 * CssDoubleConverter.
 * <p>
 * Parses an attribute value of type double.
 *
 */
public class PercentageCssConverter extends AbstractCssConverter<Double> {

    public PercentageCssConverter(boolean nullable) {
        super(nullable);
    }

    @Override
    public Double parseNonNull(CssTokenizer tt, @Nullable IdResolver idResolver) throws ParseException, IOException {
        return switch (tt.next()) {
            case CssTokenType.TT_NUMBER -> tt.currentNumberNonNull().doubleValue();
            case CssTokenType.TT_PERCENTAGE -> tt.currentNumberNonNull().doubleValue() / 100.0;
            case CssTokenType.TT_IDENT -> {
                double value = switch (tt.currentStringNonNull()) {
                    case "INF" -> Double.POSITIVE_INFINITY;
                    case "-INF" -> Double.NEGATIVE_INFINITY;
                    case "NaN" -> Double.NaN;
                    default ->
                            throw new ParseException("Could not convert " + tt.getToken() + " to a percentage value.", tt.getStartPosition());
                };
                yield value;
            }
            default ->
                    throw new ParseException("Could not convert " + tt.getToken() + " to a percentage value.", tt.getStartPosition());
        };
    }

    @Override
    public <TT extends Double> void produceTokensNonNull(TT value, @Nullable IdSupplier idSupplier, Consumer<CssToken> out) {
        double v = value;
        if (value.isInfinite()) {
            out.accept(new CssToken(CssTokenType.TT_IDENT, (v > 0) ? "INF" : "-INF"));
        } else if (value.isNaN()) {
            out.accept(new CssToken(CssTokenType.TT_IDENT, "NaN"));
        } else {
            out.accept(new CssToken(CssTokenType.TT_PERCENTAGE, v * 100.0));
        }
    }

    @Override
    public @Nullable String getHelpText() {
        if (isNullable()) {
            return "Format of ⟨NullablePercentage⟩: none｜⟨fraction⟩｜⟨percentage⟩%";
        } else {
            return "Format of ⟨Percentage⟩: ⟨fraction⟩｜⟨percentage⟩%";
        }
    }

}
