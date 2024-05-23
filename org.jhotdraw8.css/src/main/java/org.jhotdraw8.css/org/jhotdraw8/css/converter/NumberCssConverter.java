/*
 * @(#)CssNumberConverter.java
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
    private final Class<? extends Number> clazz;

    public NumberCssConverter(boolean nullable) {
        this(Double.class, nullable);
    }

    public NumberCssConverter(Class<? extends Number> clazz, boolean nullable) {
        super(nullable);
        this.clazz = clazz;
    }

    @Override
    public Number parseNonNull(CssTokenizer tt, @Nullable IdResolver idResolver) throws ParseException, IOException {
        return switch (tt.next()) {
            case CssTokenType.TT_NUMBER -> tt.currentNumberNonNull();
            case CssTokenType.TT_IDENT -> {
                double value = switch (tt.currentStringNonNull()) {
                    case "INF" -> Double.POSITIVE_INFINITY;
                    case "-INF" -> Double.NEGATIVE_INFINITY;
                    case "NaN" -> Double.NaN;
                    default ->
                            throw new ParseException("Could not convert " + tt.getToken() + " to a double value.", tt.getStartPosition());
                };
                yield value;
            }
            default ->
                    throw new ParseException("Could not convert " + tt.getToken() + " to a double value.", tt.getStartPosition());
        };
    }

    @Override
    public <TT extends Number> void produceTokensNonNull(TT value, @Nullable IdSupplier idSupplier, Consumer<CssToken> out) {
        out.accept(new CssToken(CssTokenType.TT_NUMBER, value));
    }

    @Override
    public @Nullable String getHelpText() {
        return "Format of ⟨Number⟩: ⟨number⟩";
    }

}
