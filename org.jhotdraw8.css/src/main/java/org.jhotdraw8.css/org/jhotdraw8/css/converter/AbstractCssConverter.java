/*
 * @(#)AbstractCssConverter.java
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

public abstract class AbstractCssConverter<T> implements CssConverter<T> {
    private final boolean nullable;

    public AbstractCssConverter(boolean nullable) {
        this.nullable = nullable;
    }


    @Override
    public final @Nullable T parse(@NonNull CssTokenizer tt, @Nullable IdResolver idResolver) throws ParseException, IOException {
        if (isNullable()) {
            if (tt.nextIsIdentNone()) {
                return null;
            }
            tt.pushBack();
        }
        return parseNonNull(tt, idResolver);
    }

    @Override
    public final <TT extends T> void produceTokens(@Nullable TT value, @Nullable IdSupplier idSupplier, @NonNull Consumer<CssToken> out) throws IOException {
        if (value == null) {
            out.accept(new CssToken(CssTokenType.TT_IDENT, CssTokenType.IDENT_NONE));
        } else {
            produceTokensNonNull(value, idSupplier, out);
        }
    }

    @Override
    public abstract @NonNull T parseNonNull(@NonNull CssTokenizer tt, @Nullable IdResolver idResolver) throws ParseException, IOException;

    protected abstract <TT extends T> void produceTokensNonNull(@NonNull TT value, @Nullable IdSupplier idSupplier, @NonNull Consumer<CssToken> out) throws IOException;

    @Override
    public @Nullable T getDefaultValue() {
        return null;
    }

    @Override
    public boolean isNullable() {
        return nullable;
    }
}
