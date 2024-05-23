/*
 * @(#)FormatConverterAdapter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.base.converter;

import org.jspecify.annotations.Nullable;

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
@SuppressWarnings({"serial", "RedundantSuppression"})
public class FormatConverterAdapter extends Format {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Converter<Object> converter;

    public FormatConverterAdapter(Converter<?> converter) {
        @SuppressWarnings("unchecked")
        Converter<Object> temp = (Converter<Object>) converter;
        this.converter = temp;
    }

    @Override
    public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
        toAppendTo.append(converter.toString(obj));
        return toAppendTo;
    }

    @Override
    public @Nullable Object parseObject(String source, ParsePosition pos) {
        try {
            CharBuffer buf = CharBuffer.wrap(source);
            Object value = converter.fromString(buf, null);
            pos.setIndex(buf.position());
            return value;
        } catch (ParseException ex) {
            pos.setErrorIndex(ex.getErrorOffset());
            return null;
        }
    }
}
