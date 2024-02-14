/*
 * @(#)CssNumberConverter.java
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
 * CssNumberConverter.
 * <p>
 * Parses the following EBNF:
 * <pre>
 * Number := number-token | "-INF" | "INF" | "NaN";
 * number-token = (* CSS number-token *)
 * </pre>
 * <p>
 * References:
 * <dl>
 *     <dt>CSS Syntax Module Level 3, 4. Token Railroad Diagrams, Number Token Diagram</dt>
 *     <dd><a href="https://www.w3.org/TR/css-syntax-3/#number-token-diagram">w3.org</a></dd>
 * </dl>
 *
 * @author Werner Randelshofer
 */
public class NumberCssConverter extends AbstractCssConverter<Number> {
    private final @NonNull Class<? extends Number> clazz;

    public NumberCssConverter(boolean nullable) {
        this(Double.class, nullable);
    }

    public NumberCssConverter(@NonNull Class<? extends Number> clazz, boolean nullable) {
        super(nullable);
        this.clazz = clazz;
    }

    @Override
    public @NonNull Number parseNonNull(@NonNull CssTokenizer tt, @Nullable IdResolver idResolver) throws ParseException, IOException {
        switch (tt.next()) {
        case CssTokenType.TT_NUMBER:
            return tt.currentNumberNonNull();
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
                throw new ParseException("number expected:" + tt.currentString(), tt.getStartPosition());
            }
            return value;
        }
        default:
            throw new ParseException("⟨Double⟩: number expected.", tt.getStartPosition());
        }
    }

    @Override
    public <TT extends Number> void produceTokensNonNull(@NonNull TT value, @Nullable IdSupplier idSupplier, @NonNull Consumer<CssToken> out) {
        out.accept(new CssToken(CssTokenType.TT_NUMBER, value));
    }

    @Override
    public @NonNull String getHelpText() {
        return "Format of ⟨Number⟩: ⟨number⟩";
    }

}
