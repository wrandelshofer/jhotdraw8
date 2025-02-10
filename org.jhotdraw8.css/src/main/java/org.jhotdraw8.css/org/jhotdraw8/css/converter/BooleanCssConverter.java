/*
 * @(#)BooleanCssConverter.java
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
 * Converts a {@code Boolean} into the CSS String representation.
 *
 */
public class BooleanCssConverter extends AbstractCssConverter<Boolean> {


    private final String trueString = "true";
    private final String falseString = "false";

    public BooleanCssConverter(boolean nullable) {
        super(nullable);
    }

    @Override
    public Boolean parseNonNull(CssTokenizer tt, @Nullable IdResolver idResolver) throws ParseException, IOException {
        tt.requireNextToken(CssTokenType.TT_IDENT, "⟨Boolean⟩ identifier expected.");
        String s = tt.currentStringNonNull();
        return switch (s) {
            case trueString -> Boolean.TRUE;
            case falseString -> Boolean.FALSE;
            default ->
                    throw new ParseException("Could not convert " + tt.getToken() + " to a boolean value.", tt.getStartPosition());
        };
    }

    @Override
    public <TT extends Boolean> void produceTokensNonNull(TT value, @Nullable IdSupplier idSupplier, Consumer<CssToken> out) {
        out.accept(new CssToken(CssTokenType.TT_IDENT, value.booleanValue() ? trueString : falseString));
    }

    @Override
    public @Nullable String getHelpText() {
        if (isNullable()) {
            return "Format of ⟨NullableBoolean⟩: none｜true｜false";
        } else {
            return "Format of ⟨Boolean⟩: true｜false";
        }
    }

}
