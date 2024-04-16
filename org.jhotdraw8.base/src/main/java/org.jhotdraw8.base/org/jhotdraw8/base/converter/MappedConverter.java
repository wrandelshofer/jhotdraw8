/*
 * @(#)MappedConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.base.converter;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;

import java.io.IOException;
import java.nio.CharBuffer;
import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This converter uses a map to convert a data type from/to String.
 *
 * @param <E> the data type
 */
public class MappedConverter<E> implements Converter<E> {
    private final @NonNull Map<String, E> fromStringMap;
    private final @NonNull Map<E, String> toStringMap;
    private final @NonNull String nullValue;


    public MappedConverter(@NonNull Map<String, E> fromStringMap) {
        this(fromStringMap, false);
    }

    public MappedConverter(@NonNull Map<String, E> fromStringMap, boolean nullable) {
        this(fromStringMap, nullable ? "none" : null);
    }

    public MappedConverter(@NonNull Map<String, E> fromStringMap, @NonNull String nullValue) {
        this.fromStringMap = new LinkedHashMap<>();
        this.toStringMap = new LinkedHashMap<>();
        for (Map.Entry<String, E> entry : fromStringMap.entrySet()) {
            this.fromStringMap.putIfAbsent(entry.getKey(), entry.getValue());
            this.toStringMap.putIfAbsent(entry.getValue(), entry.getKey());
        }
        this.nullValue = nullValue;
    }

    @Override
    public @Nullable E fromString(@NonNull CharBuffer in, @Nullable IdResolver idResolver) throws ParseException {
        String identifier = in.toString();
        in.position(in.length());
        if (nullValue != null && nullValue.equals(identifier)) {
            return null;
        }
        E e = fromStringMap.get(identifier);
        if (e == null) {
            throw new ParseException("Could not convert the string=\"" + identifier + "\" to a value.", 0);
        }
        return e;
    }

    @Override
    public @Nullable E getDefaultValue() {
        return null;
    }

    @Override
    public <TT extends E> void toString(@NonNull Appendable out, @Nullable IdSupplier idSupplier, @Nullable TT value) throws IOException {
        if (value == null) {
            if (nullValue != null) {
                throw new IOException("Could not convert the value=null to a non-null string.");
            }
            out.append(null);
        }
        String s = toStringMap.get(value);
        if (s == null) {
            throw new IOException("Could not convert the value=\"" + value + "\" to a string.");
        }
        out.append(s);
    }
}
