/*
 * @(#)CssWordConverter.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.converter;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.base.converter.IdResolver;
import org.jhotdraw8.base.converter.IdSupplier;

import java.io.IOException;
import java.nio.CharBuffer;
import java.text.ParseException;

/**
 * CssWordConverter.
 *
 * @author Werner Randelshofer
 */
public class CssWordConverter implements Converter<String> {

    public CssWordConverter() {
    }

    @Override
    public void toString(@NonNull Appendable out, @Nullable IdSupplier idSupplier, @Nullable String value) throws IOException {
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
    public @NonNull String fromString(@NonNull CharBuffer in, @Nullable IdResolver idResolver) throws ParseException, IOException {
        int pos = in.position();
        StringBuilder out = new StringBuilder();
        while (in.remaining() > 0 && !Character.isWhitespace(in.charAt(0))) {
            out.append(in.get());
        }
        if (out.length() == 0) {
            in.position(pos);
            throw new ParseException("word expected", pos);
        }
        return out.toString();
    }

    @Override
    public @NonNull String getDefaultValue() {
        return "";
    }
}
