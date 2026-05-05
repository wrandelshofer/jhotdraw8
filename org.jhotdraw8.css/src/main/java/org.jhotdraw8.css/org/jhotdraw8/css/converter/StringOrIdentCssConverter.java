/*
 * @(#)CssStringOrIdentConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.converter;

import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.base.converter.IdResolver;
import org.jhotdraw8.base.converter.IdSupplier;
import org.jhotdraw8.base.io.CharBufferReader;
import org.jhotdraw8.css.parser.CssTokenType;
import org.jhotdraw8.css.parser.StreamCssTokenizer;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.CharBuffer;
import java.text.ParseException;

/// Converts an `String` from/to a CSS ident-token or a CSS string-token.
public class StringOrIdentCssConverter implements Converter<String> {

    public StringOrIdentCssConverter() {
    }

    @Override
    public @Nullable String fromString(CharBuffer buf, @Nullable IdResolver idResolver) throws ParseException {
        StreamCssTokenizer tt = new StreamCssTokenizer(new CharBufferReader(buf), null);
        try {
            if (tt.next() != CssTokenType.TT_STRING && tt.current() != CssTokenType.TT_IDENT) {
                throw new ParseException("Could not convert \"" + tt.getToken() + "\" to a string value.", buf.position());
            }
        } catch (IOException e) {
            ParseException parseException = new ParseException(e.getMessage(), 0);
            parseException.initCause(e);
            throw parseException;
        }
        return tt.currentString();
    }

    @Override
    public void toString(Appendable out, @Nullable IdSupplier idSupplier, @Nullable String value) throws IOException {
        if (value == null) {
            out.append("none");
            return;
        }
        StringBuilder buf = new StringBuilder();
        boolean isIdent = true;
        buf.append('"');
        char[] charArray = value.toCharArray();
        for (int j = 0; j < charArray.length; j++) {
            char ch = charArray[j];
            switch (ch) {
                case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                    buf.append(ch);
                    isIdent &= j > 0;
                }
                case ' ', '\'' -> {
                    buf.append(ch);
                    isIdent = false;
                }
                case '"', '\n', '@', '#' -> {
                    buf.append('\\');
                    buf.append(ch);
                    isIdent = false;
                }
                default -> {
                    if (ch == '_' || 'a' <= ch && ch <= 'z' || 'A' <= ch && ch <= 'Z' || 0xA0 <= ch) {
                        buf.append(ch);
                    } else if (Character.isISOControl(ch) || Character.isWhitespace(ch)) {
                        buf.append('\\');
                        String hex = Integer.toHexString(ch);
                        for (int i = 0, n = 6 - hex.length(); i < n; i++) {
                            buf.append('0');
                        }
                        buf.append(hex);
                        isIdent = false;
                    } else {
                        buf.append(ch);
                        isIdent = false;
                    }
                }
            }
        }
        buf.append('"');
        if (isIdent) {
            out.append(value);
        } else {
            out.append(buf.toString());
        }
    }

    @Override
    public @Nullable String getDefaultValue() {
        return "";
    }
}
