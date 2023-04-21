/*
 * @(#)CssStringConverter.java
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
 * Converts an {@code String} to a quoted CSS {@code String}.
 *
 * @author Werner Randelshofer
 */
public class CssStringConverter extends AbstractCssConverter<String> {
    private final String helpText;
    private final char quoteChar;
    private final @NonNull String defaultValue;

    public CssStringConverter() {
        this(false, '\"', null);
    }

    public CssStringConverter(boolean nullable) {
        this(nullable, '\"', null);
    }

    public CssStringConverter(boolean nullable, char quoteChar, String helpText) {
        super(nullable);
        this.quoteChar = quoteChar;
        this.helpText = helpText;
        defaultValue = "" + quoteChar + quoteChar;
    }


    @Override
    public String getHelpText() {
        return helpText;
    }

    @Override
    public @NonNull String parseNonNull(@NonNull CssTokenizer tt, @Nullable IdResolver idResolver) throws ParseException, IOException {
        if (tt.next() != CssTokenType.TT_STRING) {
            throw new ParseException("Css String expected. " + tt.getToken(), tt.getStartPosition());
        }
        return tt.currentStringNonNull();
    }

    @Override
    protected <TT extends String> void produceTokensNonNull(@NonNull TT value, @Nullable IdSupplier idSupplier, @NonNull Consumer<CssToken> out) {
        out.accept(new CssToken(CssTokenType.TT_STRING, value, quoteChar));
    }

    @Override
    public @NonNull String getDefaultValue() {
        return defaultValue;
    }


}
