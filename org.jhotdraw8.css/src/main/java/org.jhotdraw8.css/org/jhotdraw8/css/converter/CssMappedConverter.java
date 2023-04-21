/*
 * @(#)CssMappedConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.converter;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.converter.IdResolver;
import org.jhotdraw8.base.converter.IdSupplier;
import org.jhotdraw8.collection.readonly.ReadOnlyMap;
import org.jhotdraw8.css.parser.CssToken;
import org.jhotdraw8.css.parser.CssTokenType;
import org.jhotdraw8.css.parser.CssTokenizer;

import java.io.IOException;
import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * This converter uses a map to convert an object from/to String.
 *
 * @param <E> the object type
 */
public class CssMappedConverter<E> implements CssConverter<E> {

    private final @NonNull Map<String, E> fromStringMap;
    private final @NonNull Map<E, String> toStringMap;
    private final boolean nullable;
    private final String name;


    public CssMappedConverter(String name, @NonNull Map<String, E> fromStringMap) {
        this(name, fromStringMap, false);
    }

    public CssMappedConverter(String name, @NonNull ReadOnlyMap<String, E> fromStringMap) {
        this(name, fromStringMap, false);
    }

    public CssMappedConverter(String name, @NonNull Map<String, E> fromStringMap, boolean nullable) {
        this.fromStringMap = new LinkedHashMap<>();
        this.toStringMap = new LinkedHashMap<>();
        for (Map.Entry<String, E> entry : fromStringMap.entrySet()) {
            this.fromStringMap.putIfAbsent(entry.getKey(), entry.getValue());
            this.toStringMap.putIfAbsent(entry.getValue(), entry.getKey());
        }
        this.name = name;
        this.nullable = nullable;
    }

    public CssMappedConverter(String name, @NonNull ReadOnlyMap<String, E> fromStringMap, boolean nullable) {
        this(name, fromStringMap.asMap(), nullable);
    }

    @Override
    public @Nullable E parse(@NonNull CssTokenizer tt, @Nullable IdResolver idResolver) throws ParseException, IOException {
        if (tt.next() != CssTokenType.TT_IDENT) {
            throw new ParseException("identifier expected", tt.getStartPosition());
        }

        String identifier = tt.currentString();
        if (nullable && CssTokenType.IDENT_NONE.equals(identifier)) {
            return null;
        }
        E e = fromStringMap.get(identifier);
        if (e == null) {
            throw new ParseException("Illegal value=\"" + identifier + "\"", 0);
        }
        return e;
    }


    @Override
    public @NonNull String getHelpText() {
        StringBuilder buf = new StringBuilder("Format of ⟨");
        buf.append(name).append("⟩: ");
        boolean first = true;
        if (nullable) {
            buf.append(CssTokenType.IDENT_NONE);
            first = false;
        }
        for (String f : toStringMap.values()) {
            if (first) {
                first = false;
            } else {
                buf.append('｜');
            }
            buf.append(f);
        }

        return buf.toString();
    }

    @Override
    public <TT extends E> void produceTokens(@Nullable TT value, @Nullable IdSupplier idSupplier, @NonNull Consumer<CssToken> consumer) {
        if (value == null) {
            if (!nullable) {
                throw new IllegalArgumentException("Value is not nullable.");
            }
            consumer.accept(new CssToken(CssTokenType.TT_IDENT, CssTokenType.IDENT_NONE));
        } else {
            String s = toStringMap.get(value);
            if (s == null) {
                throw new IllegalArgumentException("Unsupported value: " + value);
            }
            consumer.accept(new CssToken(CssTokenType.TT_IDENT, s));
        }
    }


    @Override
    public @Nullable E getDefaultValue() {
        return null;
    }

    @Override
    public boolean isNullable() {
        return nullable;
    }

}
