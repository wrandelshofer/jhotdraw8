/*
 * @(#)FormatConverterAdapter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.base.converter;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.io.IOException;
import java.io.Serial;
import java.nio.CharBuffer;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParseException;
import java.text.ParsePosition;

/**
 * Allows to use a {@code Converter} with the {@code java.text.Format} API.
 *
 * @author Werner Randelshofer
 */
public class FormatConverterAdapter extends Format {

    @Serial
    private static final long serialVersionUID = 1L;

    private final @NonNull Converter<Object> converter;

    public FormatConverterAdapter(Converter<?> converter) {
        @SuppressWarnings("unchecked")
        Converter<Object> temp = (Converter<Object>) converter;
        this.converter = temp;
    }

    @Override
    public @NonNull StringBuffer format(Object obj, @NonNull StringBuffer toAppendTo, @NonNull FieldPosition pos) {
        toAppendTo.append(converter.toString(obj));
        return toAppendTo;
    }

    @Override
    public @Nullable Object parseObject(@NonNull String source, @NonNull ParsePosition pos) {
        try {
            CharBuffer buf = CharBuffer.wrap(source);
            Object value = converter.fromString(buf, null);
            pos.setIndex(buf.position());
            return value;
        } catch (ParseException ex) {
            pos.setErrorIndex(ex.getErrorOffset());
            return null;
        } catch (IOException ex) {
            return null;
        }
    }
}
