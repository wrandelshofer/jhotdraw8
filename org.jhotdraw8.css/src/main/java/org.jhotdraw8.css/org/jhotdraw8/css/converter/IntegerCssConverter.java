/*
 * @(#)CssIntegerConverter.java
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
 * CssIntegerConverter.
 * <p>
 * Parses an attribute value of type integer.
 *
 */
public class IntegerCssConverter extends AbstractCssConverter<Integer> {

    public IntegerCssConverter(boolean nullable) {
        super(nullable);
    }

    @Override
    public Integer parseNonNull(CssTokenizer tt, @Nullable IdResolver idResolver) throws ParseException, IOException {
        return switch (tt.next()) {
            case CssTokenType.TT_NUMBER -> tt.currentNumberNonNull().intValue();
            default -> throw tt.createParseException("Could not convert " + tt.getToken() + " to an integer value.");
        };
    }

    @Override
    public <TT extends Integer> void produceTokensNonNull(TT value, @Nullable IdSupplier idSupplier, Consumer<CssToken> out) {
        out.accept(new CssToken(CssTokenType.TT_NUMBER, value));
    }

    @Override
    public @Nullable String getHelpText() {
        return "Format of ⟨Integer⟩: ⟨integer⟩";
    }

}
