/*
 * @(#)StyleConverterAdapter.java
 * Copyright © 2023 The authors and contributors of JHotDraw. MIT License.
 */
package org.jhotdraw8.fxbase.text;

import javafx.css.ParsedValue;
import javafx.css.StyleConverter;
import javafx.scene.text.Font;
import org.jhotdraw8.base.converter.Converter;
import org.jspecify.annotations.Nullable;

import java.text.ParseException;

/**
 * Allows to use a {@link Converter} with the {@code javafx.css.StyleConverter}
 * API.
 *
 * rawcoder $
 */
public class StyleConverterAdapter<T> extends StyleConverter<String, T> {

    private final Converter<T> converter;

    public StyleConverterAdapter(Converter<T> converter) {
        this.converter = converter;
    }

    @Override
    public @Nullable T convert(ParsedValue<String, T> value, Font font) {
        try {
            return converter.fromString(value.getValue());
        } catch (ParseException ex) {
            return converter.getDefaultValue();
        }
    }
}
