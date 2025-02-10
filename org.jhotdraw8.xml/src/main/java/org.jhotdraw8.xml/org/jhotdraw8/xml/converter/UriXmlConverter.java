/*
 * @(#)XmlUriConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.xml.converter;

import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.base.converter.IdResolver;
import org.jhotdraw8.base.converter.IdSupplier;
import org.jhotdraw8.css.parser.CssTokenType;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.CharBuffer;
import java.text.ParseException;

/**
 * XmlUriConverter.
 *
 */
public class UriXmlConverter implements Converter<URI> {

    public UriXmlConverter() {
    }

    @Override
    public void toString(Appendable out, @Nullable IdSupplier idSupplier, @Nullable URI value) throws IOException {
        out.append(value == null ? CssTokenType.IDENT_NONE :
                (idSupplier == null ? value : idSupplier.relativize(value)).toString());
    }

    @Override
    public @Nullable URI fromString(CharBuffer in, @Nullable IdResolver idResolver) throws ParseException {
        String str = in.toString().trim();
        in.position(in.limit());// fully consume the buffer
        if (CssTokenType.IDENT_NONE.equals(str)) {
            return null;
        }
        try {
            return idResolver == null ? new URI(str) : idResolver.absolutize(new URI(str));
        } catch (URISyntaxException e) {
            throw new ParseException(e.getMessage(), 0);
        }
    }

    @Override
    public @Nullable URI getDefaultValue() {
        return null;
    }
}
