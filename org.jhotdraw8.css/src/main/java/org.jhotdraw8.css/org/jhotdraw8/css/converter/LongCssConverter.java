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
public class LongCssConverter extends AbstractCssConverter<Long> {

    public LongCssConverter(boolean nullable) {
        super(nullable);
    }

    @Override
    public Long parseNonNull(CssTokenizer tt, @Nullable IdResolver idResolver) throws ParseException, IOException {
        return switch (tt.next()) {
            case CssTokenType.TT_NUMBER -> tt.currentNumberNonNull().longValue();
            default -> throw tt.createParseException("Could not convert " + tt.getToken() + " to a long value.");
        };
    }

    @Override
    public <TT extends Long> void produceTokensNonNull(TT value, @Nullable IdSupplier idSupplier, Consumer<CssToken> out) {
        out.accept(new CssToken(CssTokenType.TT_NUMBER, value));
    }

    @Override
    public @Nullable String getHelpText() {
        return "Format of ⟨Long⟩: ⟨integer⟩";
    }

}
