/*
 * @(#)CssWordConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.converter;

import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.base.converter.IdResolver;
import org.jhotdraw8.base.converter.IdSupplier;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.CharBuffer;
import java.text.ParseException;

/**
 * CssWordConverter.
 *
 * @author Werner Randelshofer
 */
public class WordCssConverter implements Converter<String> {

    public WordCssConverter() {
    }

    @Override
    public void toString(Appendable out, @Nullable IdSupplier idSupplier, @Nullable String value) throws IOException {
        if (value == null) {
            out.append("none");
            return;
        }
        for (char ch : value.toCharArray()) {
            if (Character.isWhitespace(ch)) {
                break;
            }
            out.append(ch);
        }
    }

    @Override
    public String fromString(CharBuffer in, @Nullable IdResolver idResolver) throws ParseException {
        int pos = in.position();
        StringBuilder out = new StringBuilder();
        while (in.remaining() > 0 && !Character.isWhitespace(in.charAt(0))) {
            out.append(in.get());
        }
        if (out.isEmpty()) {
            in.position(pos);
            throw new ParseException("Could not convert the string=\"" + in.toString() + "\" to a word that contains no whitespace.", pos);
        }
        return out.toString();
    }

    @Override
    public @Nullable String getDefaultValue() {
        return "";
    }
}
