/*
 * @(#)ConverterFormatAdapter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.base.converter;

import org.jspecify.annotations.Nullable;

import java.nio.CharBuffer;
import java.text.Format;
import java.text.ParseException;
import java.text.ParsePosition;

/**
 * Allows to use a {@code java.text.Format} with the {@code Converter} API.
 *
 */
public class ConverterFormatAdapter implements Converter<Object> {

    private final Format format;

    public ConverterFormatAdapter(Format format) {
        this.format = format;
    }

    @Override
    public String toString(Object value) {
        return format.format(value);
    }

    public Object fromString(String string, IdFactory idFactory, ParsePosition pp) {
        Object value = format.parseObject(string, pp);
        return value;
    }

    @Override
    public void toString(Appendable out, @Nullable IdSupplier idSupplier, Object value) {
        throw new UnsupportedOperationException("Could not generate a string for value=\"" + value + "\"." + format);
    }

    @Override
    public Object fromString(CharBuffer buf, @Nullable IdResolver idResolver) throws ParseException {
        int pos = buf.position();
        String str = buf.toString();
        ParsePosition pp = new ParsePosition(0);
        Object value = format.parseObject(str, pp);
        if (pp.getErrorIndex() != -1) {
            buf.position(pos + pp.getErrorIndex());
            throw new ParseException("Could not parse the string=\"" + str + "\".", buf.position());
        } else {
            buf.position(pos + pp.getIndex());
        }
        return value;
    }

    @Override
    public @Nullable Object getDefaultValue() {
        return null;
    }
}
