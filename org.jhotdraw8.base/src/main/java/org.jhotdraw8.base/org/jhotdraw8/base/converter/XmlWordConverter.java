/*
 * @(#)XmlWordConverter.java
 * Copyright © 2022 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.base.converter;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.io.IOException;
import java.nio.CharBuffer;
import java.text.ParseException;

/**
 * XmlWordConverter.
 *
 * @author Werner Randelshofer
 */
public class XmlWordConverter implements Converter<String> {

    public XmlWordConverter() {
    }

    @Override
    public void toString(@NonNull Appendable out, @Nullable IdSupplier idSupplier, @NonNull String value) throws IOException {
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
