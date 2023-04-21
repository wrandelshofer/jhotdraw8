/*
 * @(#)CssIntegerConverter.java
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
 * CssIntegerConverter.
 * <p>
 * Parses an attribute value of type integer.
 *
 * @author Werner Randelshofer
 */
public class CssIntegerConverter extends AbstractCssConverter<Integer> {

    public CssIntegerConverter(boolean nullable) {
        super(nullable);
    }

    @Override
    public @NonNull Integer parseNonNull(@NonNull CssTokenizer tt, @Nullable IdResolver idResolver) throws ParseException, IOException {
        switch (tt.next()) {
        case CssTokenType.TT_NUMBER:
            return tt.currentNumberNonNull().intValue();
        default:
            throw tt.createParseException("⟨Integer⟩: integer expected.");
        }
    }

    @Override
    public <TT extends Integer> void produceTokensNonNull(@NonNull TT value, @Nullable IdSupplier idSupplier, @NonNull Consumer<CssToken> out) {
        out.accept(new CssToken(CssTokenType.TT_NUMBER, value));
    }

    @Override
    public @NonNull String getHelpText() {
        return "Format of ⟨Integer⟩: ⟨integer⟩";
    }

}
