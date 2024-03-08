/*
 * @(#)CssKebabCaseEnumConverter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.css.converter;

import org.jhotdraw8.annotation.NonNull;
import org.jhotdraw8.annotation.Nullable;
import org.jhotdraw8.base.converter.IdResolver;
import org.jhotdraw8.base.converter.IdSupplier;
import org.jhotdraw8.css.parser.CssToken;
import org.jhotdraw8.css.parser.CssTokenType;
import org.jhotdraw8.css.parser.CssTokenizer;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.function.Consumer;

/**
 * CssKebabCaseEnumConverter. Converts all enum names to kebab-case.
 * <p>
 * If you need a different mapping use {@link MappedCssConverter} or
 * {@link LiteralEnumCssConverter}.
 *
 * @param <E> the type of the enum that can be converted from/to CSS
 * @author Werner Randelshofer
 */
public class KebabCaseEnumCssConverter<E extends Enum<E>> implements CssConverter<E> {

    private final @NonNull Class<E> enumClass;
    private final @NonNull String name;
    private final boolean nullable;

    public KebabCaseEnumCssConverter(@NonNull Class<E> enumClass) {
        this(enumClass, false);
    }

    public KebabCaseEnumCssConverter(@NonNull Class<E> enumClass, boolean nullable) {
        this.enumClass = enumClass;
        this.name = enumClass.getName().substring(enumClass.getName().lastIndexOf('.') + 1);
        this.nullable = nullable;
    }


    @Override
    public @Nullable E parse(@NonNull CssTokenizer tt, @Nullable IdResolver idResolver) throws ParseException, IOException {
        if (tt.next() != CssTokenType.TT_IDENT) {
            throw new ParseException(name + ": identifier expected", tt.getStartPosition());
        }

        String identifier = tt.currentStringNonNull();
        if (nullable && CssTokenType.IDENT_NONE.equals(identifier)) {
            return null;
        }
        try {
            return Enum.valueOf(enumClass, identifier.toUpperCase().replace('-', '_'));
        } catch (IllegalArgumentException e) {
            throw new ParseException(name + ": illegal identifier:" + identifier, tt.getStartPosition());
        }
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
        for (Field f : enumClass.getDeclaredFields()) {
            if (f.isEnumConstant()) {
                if (first) {
                    first = false;
                } else {
                    buf.append('｜');
                }
                buf.append(f.getName().toLowerCase().replace('_', '-'));
            }
        }
        return buf.toString();
    }

    @Override
    public <TT extends E> void produceTokens(@Nullable TT value, @Nullable IdSupplier idSupplier, @NonNull Consumer<CssToken> consumer) {
        if (value == null) {
            if (!nullable) {
                throw new IllegalArgumentException("value is not nullable. enum type:" + enumClass + " value:" + null);
            }
            consumer.accept(new CssToken(CssTokenType.TT_IDENT, CssTokenType.IDENT_NONE));
        } else {
            StringBuilder out = new StringBuilder();
            for (char ch : value.toString().toLowerCase().replace('_', '-').toCharArray()) {
                if (Character.isWhitespace(ch)) {
                    break;
                }
                out.append(ch);
            }
            consumer.accept(new CssToken(CssTokenType.TT_IDENT, out.toString()));
        }
    }

    public @NonNull String toString(@Nullable E value) {
        StringBuilder out = new StringBuilder();
        produceTokens(value, null, token -> out.append(token.fromToken()));
        return out.toString();
    }

    @Override
    public @Nullable E getDefaultValue() {
        return null;
    }

    @Override
    public boolean nullable() {
        return nullable;
    }

}
