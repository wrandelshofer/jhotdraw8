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
public class StringCssConverter extends AbstractCssConverter<String> {
    private final @Nullable String helpText;
    private final char quoteChar;
    private final @NonNull String defaultValue;

    public StringCssConverter() {
        this(false, '\"', null);
    }

    public StringCssConverter(boolean nullable) {
        this(nullable, '\"', null);
    }

    public StringCssConverter(boolean nullable, char quoteChar, @Nullable String helpText) {
        super(nullable);
        this.quoteChar = quoteChar;
        this.helpText = helpText;
        defaultValue = "" + quoteChar + quoteChar;
    }


    @Override
    public @Nullable String getHelpText() {
        return helpText;
    }

    @Override
    public @NonNull String parseNonNull(@NonNull CssTokenizer tt, @Nullable IdResolver idResolver) throws ParseException, IOException {
        if (tt.next() != CssTokenType.TT_STRING) {
            throw tt.createParseException("Could not convert \"" + tt.getToken() + "\" to a string value.");
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
