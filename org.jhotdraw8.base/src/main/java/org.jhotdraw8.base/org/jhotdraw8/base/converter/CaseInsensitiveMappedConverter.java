/*
 * @(#)CaseInsensitiveMappedConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.base.converter;

import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.CharBuffer;
import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.Map;

public class CaseInsensitiveMappedConverter<E> implements Converter<E> {
    private final Map<String, E> fromStringMap;
    private final Map<E, String> toStringMap;

    public CaseInsensitiveMappedConverter(Map<String, E> fromStringMap) {
        this.fromStringMap = new LinkedHashMap<>();
        this.toStringMap = new LinkedHashMap<>();
        for (Map.Entry<String, E> entry : fromStringMap.entrySet()) {
            this.fromStringMap.putIfAbsent(entry.getKey().toLowerCase(), entry.getValue());
            this.toStringMap.putIfAbsent(entry.getValue(), entry.getKey());
        }
    }

    @Override
    public @Nullable E fromString(CharBuffer in, @Nullable IdResolver idResolver) throws ParseException {
        String str = in.toString();
        in.position(in.length());
        E e = fromStringMap.get(str.toLowerCase());
        if (e == null) {
            throw new ParseException("Could not find string=\"" + str + "\" in the set " + fromStringMap.keySet() + ".", 0);
        }
        return e;
    }

    @Override
    public @Nullable E getDefaultValue() {
        return null;
    }

    @Override
    public <TT extends E> void toString(Appendable out, @Nullable IdSupplier idSupplier, @Nullable TT value) throws IOException {
        if (value == null) {
            throw new IOException("Could not generate a string for value=null.");
        }
        String s = toStringMap.get(value);
        if (s == null) {
            throw new IOException("Could not find value=\"" + value + "\" in the set " + toStringMap.keySet() + ".");
        }
        out.append(s);
    }

}
