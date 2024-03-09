/*
 * @(#)CssStringOrIdentConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.converter;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.converter.Converter;
import org.jhotdraw8.base.converter.IdResolver;
import org.jhotdraw8.base.converter.IdSupplier;
import org.jhotdraw8.base.io.CharBufferReader;
import org.jhotdraw8.css.parser.CssTokenType;
import org.jhotdraw8.css.parser.StreamCssTokenizer;

import java.io.IOException;
import java.nio.CharBuffer;
import java.text.ParseException;

/**
 * Converts an {@code String} from/to a CSS ident-token or a CSS string-token.
 *
 * @author Werner Randelshofer
 */
public class StringOrIdentCssConverter implements Converter<String> {

    public StringOrIdentCssConverter() {
    }

    @Override
    public @Nullable String fromString(@NonNull CharBuffer buf, @Nullable IdResolver idResolver) throws ParseException {
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
    public void toString(@NonNull Appendable out, @Nullable IdSupplier idSupplier, @Nullable String value) throws IOException {
        if (value == null) {
            out.append("none");
            return;
        }
        StringBuilder buf = new StringBuilder();
        boolean isIdent = true;
        buf.append('"');
        for (char ch : value.toCharArray()) {
            switch (ch) {
                case '"':
                    buf.append('\\');
                    buf.append('"');
                    isIdent = false;
                    break;
            case ' ':
                buf.append(ch);
                isIdent = false;
                break;
            case '\n':
                buf.append('\\');
                buf.append('\n');
                isIdent = false;
                break;
            default:
                if (Character.isISOControl(ch) || Character.isWhitespace(ch)) {
                    buf.append('\\');
                    String hex = Integer.toHexString(ch);
                    for (int i = 0, n = 6 - hex.length(); i < n; i++) {
                        buf.append('0');
                    }
                    buf.append(hex);
                } else {
                    buf.append(ch);
                }
                break;
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
