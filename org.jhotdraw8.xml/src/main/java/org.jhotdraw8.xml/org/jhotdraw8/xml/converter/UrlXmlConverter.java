/*
 * @(#)XmlUrlConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.xml.converter;

import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.base.converter.IdResolver;
import org.jhotdraw8.base.converter.IdSupplier;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.CharBuffer;
import java.text.ParseException;

/**
 * XmlUrlConverter.
 *
 */
public class UrlXmlConverter implements Converter<URL> {

    public UrlXmlConverter() {
    }

    @Override
    public void toString(Appendable out, @Nullable IdSupplier idSupplier, @Nullable URL value) throws IOException {
        if (value != null) {
            out.append(value.toString());
        }
    }

    @Override
    public @Nullable URL fromString(CharBuffer in, @Nullable IdResolver idResolver) throws ParseException {
        String str = in.toString();
        if (str.isEmpty()) {
            return null;
        }
        URL value = null;
        try {
            value = new URI(str).toURL();
        } catch (MalformedURLException | URISyntaxException e) {
            ParseException parseException = new ParseException(e.getMessage(), 0);
            parseException.initCause(e);
            throw parseException;
        }
        in.position(in.limit());
        return value;
    }

    @Override
    public @Nullable URL getDefaultValue() {
        return null;
    }
}
