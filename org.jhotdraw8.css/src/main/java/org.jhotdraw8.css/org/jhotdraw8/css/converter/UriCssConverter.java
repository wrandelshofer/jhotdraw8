/*
 * @(#)CssUriConverter.java
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
import java.net.URI;
import java.text.ParseException;
import java.util.function.Consumer;

/**
 * Converts an {@code URI} to a CSS {@code URI}.
 * <pre>
 * URI = uriFunction | none ;
 * none = "none" ;
 * uriFunction = "url(" , [ uri ] , ")" ;
 * uri =  (* css uri *) ;
 * </pre>
 *
 */
public class UriCssConverter extends AbstractCssConverter<URI> {
    private final @Nullable String helpText;

    public UriCssConverter() {
        this(false, null);
    }

    public UriCssConverter(boolean nullable) {
        this(nullable, null);
    }

    public UriCssConverter(boolean nullable, @Nullable String helpText) {
        super(nullable);
        this.helpText = helpText;
    }


    @Override
    public @Nullable String getHelpText() {
        return helpText;
    }

    @Override
    public URI parseNonNull(CssTokenizer tt, @Nullable IdResolver idResolver) throws ParseException, IOException {
        if (tt.next() != CssTokenType.TT_URL) {
            throw new ParseException("Could not convert " + tt.getToken() + " to a URL value.", tt.getStartPosition());
        }
        try {
            return URI.create(tt.currentStringNonNull());
        } catch (IllegalArgumentException e) {
            throw new ParseException("Could not convert " + tt.getToken() + " to a URL value.", tt.getStartPosition());
        }
    }

    @Override
    protected <TT extends URI> void produceTokensNonNull(TT value, @Nullable IdSupplier idSupplier, Consumer<CssToken> out) {
        out.accept(new CssToken(CssTokenType.TT_URL, value.toString()));
    }
}
